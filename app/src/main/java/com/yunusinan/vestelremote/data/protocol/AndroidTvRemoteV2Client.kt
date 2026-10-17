package com.yunusinan.vestelremote.data.protocol

import com.yunusinan.vestelremote.data.crypto.CertificateHelper
import com.yunusinan.vestelremote.data.crypto.PairingCrypto
import com.yunusinan.vestelremote.data.crypto.TlsSocketFactoryHelper
import com.yunusinan.vestelremote.domain.model.ConnectionDiagnostic
import com.yunusinan.vestelremote.domain.model.ConnectionStatus
import com.yunusinan.vestelremote.domain.model.TvCommand
import com.yunusinan.vestelremote.domain.model.TvDevice
import kotlinx.coroutines.*
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.security.cert.X509Certificate
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.SSLSocket

class AndroidTvRemoteV2Client(
    private val host: String,
    private val keyCertPair: CertificateHelper.KeyCertPair,
    private val pinnedServerFingerprint: String? = null,
    private val pairPort: Int = 6467,
    private val remotePort: Int = 6466,
    private val onStateChanged: ((ConnectionStatus) -> Unit)? = null,
    private val onEventLog: ((title: String, detail: String) -> Unit)? = null,
    private val diagnostics: AttemptDiagnostics? = null,
    private val onVolume: ((level: Int, maximum: Int, muted: Boolean) -> Unit)? = null,
    private val responseTimeoutMs: Int = 15000
) {
    @Volatile private var remoteSocket: SSLSocket? = null
    @Volatile private var pairingSocket: SSLSocket? = null
    @Volatile private var remoteOutputStream: OutputStream? = null
    private var readerJob: Job? = null
    private var remoteDiagnostics: AttemptDiagnostics? = null
    @Volatile var isConnected: Boolean = false
        private set

    private fun target(device: TvDevice?) = device ?: TvDevice(id = "tv-$host", ipAddress = host)

    /** Only ConfigurationAck opens the PIN prompt; only SecretAck completes pairing. */
    suspend fun pair(device: TvDevice? = null, onPinPrompt: suspend () -> String): Result<String> = withContext(Dispatchers.IO) {
        val diag = diagnostics ?: AttemptDiagnostics(ConnectionDiagnostic(host = host, port = pairPort, service = "pairing"))
        var cancellationGuard: Job? = null
        try {
            onStateChanged?.invoke(ConnectionStatus.ConnectingToPairingService(host))
            val socket = createSocket(null, diag)
            pairingSocket = socket
            cancellationGuard = launch(start = CoroutineStart.UNDISPATCHED) {
                try { awaitCancellation() } finally { runCatching { socket.close() } }
            }
            openSocket(socket, pairPort, diag)
            val cert = socket.session.peerCertificates.firstOrNull() as? X509Certificate
                ?: throw ProtocolFailure("TV X.509 sertifikası sunmadı")
            onStateChanged?.invoke(ConnectionStatus.TlsEstablished(host))
            val output = socket.outputStream
            val input = socket.inputStream
            suspend fun exchange(payload: ByteArray, expected: Int) {
                currentCoroutineContext().ensureActive()
                val expectedName = PairingProtocol.names.getValue(expected)
                diag.stage(expectedName, "PairingNegotiation")
                onStateChanged?.invoke(ConnectionStatus.PairingNegotiation(host, expectedName))
                send(output, payload, PairingProtocol.names, diag)
                val reply = receive(input, PairingProtocol.names, diag, pairing = true)
                PairingProtocol.validate(reply, expected)
            }
            exchange(PairingProtocol.request(), 11)
            exchange(PairingProtocol.options(), 20)
            exchange(PairingProtocol.configuration(), 31)
            diag.stage("AwaitingTvPin")
            onStateChanged?.invoke(ConnectionStatus.AwaitingTvPin(target(device)))
            onEventLog?.invoke("Yapılandırma Onaylandı", "ConfigurationAck alındı; TV ekranındaki kod bekleniyor")
            val pin = withTimeout(120000) { onPinPrompt() }
            currentCoroutineContext().ensureActive()
            diag.stage("VerifyingPin")
            onStateChanged?.invoke(ConnectionStatus.VerifyingPin(target(device)))
            val secret = PairingCrypto.computePairingSecret(keyCertPair.certificate, cert, pin)
            try { exchange(PairingProtocol.secret(secret), 41) } finally { secret.fill(0) }
            currentCoroutineContext().ensureActive()
            diag.stage("Paired")
            onStateChanged?.invoke(ConnectionStatus.Paired(target(device)))
            Result.success(PairingCrypto.sha256Hex(cert.encoded))
        } catch (e: Exception) {
            diag.failed(e)
            if (e is CancellationException && e !is TimeoutCancellationException) throw e
            onStateChanged?.invoke(ConnectionStatus.Failed(safeConnectionError(e)))
            onEventLog?.invoke("Eşleştirme Hatası", "${e.javaClass.simpleName}: ${safeConnectionError(e)}")
            Result.failure(e)
        } finally {
            cancellationGuard?.cancel()
            runCatching { pairingSocket?.close() }
            pairingSocket = null
        }
    }

    /** Wait for the server's configure/start exchange before declaring the remote usable. */
    suspend fun connect(device: TvDevice? = null): Result<Unit> = withContext(Dispatchers.IO) {
        val diag = diagnostics ?: AttemptDiagnostics(ConnectionDiagnostic(host = host, port = remotePort, service = "remote"))
        remoteDiagnostics = diag
        val transferred = AtomicBoolean(false)
        var cancellationGuard: Job? = null
        var socket: SSLSocket? = null
        try {
            onStateChanged?.invoke(ConnectionStatus.ConnectingToRemoteService(host))
            if (pinnedServerFingerprint == null) throw ProtocolFailure("Kayıtlı TV sertifikası yok; önce Eşleştir seçeneğini kullanın")
            socket = createSocket(pinnedServerFingerprint, diag)
            val sessionSocket = socket
            remoteSocket = socket
            cancellationGuard = launch(start = CoroutineStart.UNDISPATCHED) {
                try { awaitCancellation() } finally {
                    if (!transferred.get()) runCatching { sessionSocket.close() }
                }
            }
            openSocket(socket, remotePort, diag)
            val protocol = RemoteProtocol()
            val input = socket.inputStream
            val output = socket.outputStream
            diag.stage("RemoteStart", "RemoteNegotiation")
            val deadline = System.nanoTime() + responseTimeoutMs * 1_000_000L
            while (!protocol.started) {
                currentCoroutineContext().ensureActive()
                val remaining = (deadline - System.nanoTime()) / 1_000_000L
                if (remaining <= 0) throw SocketTimeoutException()
                socket.soTimeout = remaining.toInt().coerceAtLeast(1)
                handleRemote(receive(input, RemoteProtocol.names, diag), protocol, output, diag)
            }
            currentCoroutineContext().ensureActive()
            socket.soTimeout = 30000
            remoteOutputStream = output
            isConnected = true
            diag.stage("RemoteActive", "Connected")
            onStateChanged?.invoke(ConnectionStatus.Connected(target(device)))
            onEventLog?.invoke("Kumanda Bağlantısı Aktif", "RemoteStart alındı")
            readerJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    while (isActive && remoteSocket === sessionSocket) {
                        handleRemote(receive(input, RemoteProtocol.names, diag), protocol, output, diag)
                    }
                } catch (e: Exception) {
                    if (remoteSocket === sessionSocket && isActive) {
                        diag.failed(e)
                        closeRemote()
                        onStateChanged?.invoke(ConnectionStatus.Failed(safeConnectionError(e)))
                        onEventLog?.invoke("Kumanda Bağlantısı Kesildi", safeConnectionError(e))
                    }
                }
            }
            transferred.set(true)
            Result.success(Unit)
        } catch (e: Exception) {
            closeRemote()
            diag.failed(e)
            if (e is CancellationException) throw e
            onStateChanged?.invoke(ConnectionStatus.Failed(safeConnectionError(e)))
            onEventLog?.invoke("Kumanda Bağlantı Hatası", "${e.javaClass.simpleName}: ${safeConnectionError(e)}")
            Result.failure(e)
        } finally {
            cancellationGuard?.cancel()
            if (!transferred.get()) runCatching { socket?.close() }
        }
    }

    private fun createSocket(pin: String?, diag: AttemptDiagnostics): SSLSocket {
        diag.update { it.copy(identity = keyCertPair.storage) }
        if (!keyCertPair.supportsTlsSigning) {
            throw ProtocolFailure("İstemci TLS imza izinleri eksik; Eşleştir seçeneğini kullanın")
        }
        val helper = TlsSocketFactoryHelper(keyCertPair, pin)
        val socket = helper.createSSLSocketFactory().first.createSocket() as SSLSocket
        socket.soTimeout = responseTimeoutMs
        return socket
    }

    private fun openSocket(socket: SSLSocket, port: Int, diag: AttemptDiagnostics) {
        diag.stage("TCP")
        socket.connect(InetSocketAddress(host, port), responseTimeoutMs)
        diag.update { it.copy(tcp = "Success") }
        diag.stage("TLS")
        socket.startHandshake()
        diag.update { it.copy(tls = "Success (${socket.session.protocol}, ${socket.session.cipherSuite})") }
    }

    private fun send(output: OutputStream, bytes: ByteArray, names: Map<Int, String>, diag: AttemptDiagnostics) {
        val name = messageName(ProtoUtils.ProtoMessage.parse(bytes), names)
        diag.update { it.copy(lastOutgoing = name) }
        ProtoUtils.writeFrame(output, bytes)
    }

    private fun receive(input: InputStream, names: Map<Int, String>, diag: AttemptDiagnostics, pairing: Boolean = false): ProtoUtils.ProtoMessage {
        val msg = ProtoUtils.ProtoMessage.parse(ProtoUtils.readFrame(input))
        diag.update { it.copy(lastIncoming = messageName(msg, names), serverStatus = if (pairing) msg.getInt(2) else null) }
        return msg
    }

    private fun messageName(msg: ProtoUtils.ProtoMessage, names: Map<Int, String>): String =
        msg.fields.filter { it.wireType == ProtoUtils.WIRE_TYPE_LENGTH_DELIMITED }
            .joinToString(",") { names[it.fieldNumber] ?: "UnknownField${it.fieldNumber}" }.ifEmpty { "NoPayload" }

    private fun handleRemote(msg: ProtoUtils.ProtoMessage, protocol: RemoteProtocol, output: OutputStream, diag: AttemptDiagnostics) {
        protocol.accept(msg).forEach { send(output, it, RemoteProtocol.names, diag) }
        msg.getMessage(50)?.let {
            onVolume?.invoke(it.getInt(7) ?: 0, it.getInt(6) ?: 0, it.getInt(8) == 1)
        }
    }

    private suspend fun sendRemote(bytes: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        val output = remoteOutputStream
        if (!isConnected || output == null) return@withContext Result.failure(ProtocolFailure("Televizyona bağlı değil"))
        try {
            currentCoroutineContext().ensureActive()
            send(output, bytes, RemoteProtocol.names, remoteDiagnostics!!)
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            remoteDiagnostics?.failed(e)
            closeRemote()
            onStateChanged?.invoke(ConnectionStatus.Failed(safeConnectionError(e)))
            Result.failure(e)
        }
    }

    suspend fun sendCommand(command: TvCommand): Result<Unit> = sendRemote(RemoteProtocol.key(command))
    suspend fun sendDeepLink(uri: String): Result<Unit> = sendRemote(RemoteProtocol.appLink(uri))
    suspend fun sendTouchpadDirection(dx: Float, dy: Float): Result<Unit> {
        val command = when {
            dy < -30 -> TvCommand.DPadUp
            dy > 30 -> TvCommand.DPadDown
            dx < -30 -> TvCommand.DPadLeft
            dx > 30 -> TvCommand.DPadRight
            else -> return Result.success(Unit)
        }
        return sendCommand(command)
    }

    private fun closeRemote() {
        val socket = remoteSocket
        remoteSocket = null
        isConnected = false
        remoteOutputStream = null
        readerJob?.cancel()
        readerJob = null
        runCatching { socket?.close() }
    }

    fun disconnect() {
        runCatching { pairingSocket?.close() }
        closeRemote()
        remoteDiagnostics?.stage("Disconnected")
        onStateChanged?.invoke(ConnectionStatus.Disconnected)
    }
}

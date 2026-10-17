package com.yunusinan.vestelremote.protocol

import com.yunusinan.vestelremote.data.crypto.CertificateHelper
import com.yunusinan.vestelremote.data.crypto.PairingCrypto
import com.yunusinan.vestelremote.data.protocol.*
import com.yunusinan.vestelremote.domain.model.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.net.InetAddress
import java.net.Socket
import java.security.Principal
import java.security.cert.X509Certificate
import java.util.concurrent.*
import javax.net.ssl.*

/** Real loopback mTLS sockets, independent reference fixture payloads. Not a TV hardware test. */
class RemoteSessionRegressionTest {
    @Test fun restrictedLegacyIdentityFailsBeforeNetworkWithActionableDiagnostics() = runBlocking<Unit> {
        var diag = ConnectionDiagnostic(host = "127.0.0.1", port = 6467, service = "pairing")
        val legacy = CertificateHelper().getOrCreateClientCertificate().copy(
            storage = "AndroidKeyStore/LegacyRestricted", supportsTlsSigning = false)
        val client = AndroidTvRemoteV2Client("127.0.0.1", legacy,
            diagnostics = AttemptDiagnostics(diag) { diag = it })
        assertTrue(client.pair { error("PIN must not open") }.isFailure)
        assertEquals("NotAttempted", diag.tcp)
        assertEquals("AndroidKeyStore/LegacyRestricted", diag.identity)
        assertTrue(diag.exceptionMessage.orEmpty().contains("TLS imza izinleri"))
    }

    private class Peer : AutoCloseable {
        val identity = CertificateHelper().getOrCreateClientCertificate()
        val executor = Executors.newSingleThreadExecutor()
        val socket: SSLServerSocket
        init {
            val keys = object : X509ExtendedKeyManager() {
                override fun getClientAliases(k: String?, i: Array<out Principal>?) = emptyArray<String>()
                override fun chooseClientAlias(k: Array<out String>?, i: Array<out Principal>?, s: Socket?): String? = null
                override fun getServerAliases(k: String?, i: Array<out Principal>?) = if (k == "RSA") arrayOf("server") else null
                override fun chooseServerAlias(k: String?, i: Array<out Principal>?, s: Socket?) = if (k == "RSA") "server" else null
                override fun getCertificateChain(alias: String?) = arrayOf(identity.certificate)
                override fun getPrivateKey(alias: String?) = identity.privateKey
            }
            val trust = object : X509TrustManager {
                override fun checkClientTrusted(c: Array<out X509Certificate>?, a: String?) {}
                override fun checkServerTrusted(c: Array<out X509Certificate>?, a: String?) {}
                override fun getAcceptedIssuers() = emptyArray<X509Certificate>()
            }
            val context = SSLContext.getInstance("TLS").apply { init(arrayOf(keys), arrayOf(trust), null) }
            socket = (context.serverSocketFactory.createServerSocket(0, 1, InetAddress.getLoopbackAddress()) as SSLServerSocket).apply {
                needClientAuth = true
                soTimeout = 4000
            }
        }
        fun serve(block: (SSLSocket) -> Unit): Future<*> = executor.submit {
            (socket.accept() as SSLSocket).use {
                it.soTimeout = 4000
                it.startHandshake()
                assertTrue(it.session.peerCertificates.isNotEmpty())
                block(it)
            }
        }
        override fun close() { socket.close(); executor.shutdownNow() }
    }

    @Test fun remoteReadsConfigureStartPingVolumeAndSendsActualKeys() = runBlocking<Unit> {
        Peer().use { tv ->
            val release = CountDownLatch(1)
            val volume = CompletableDeferred<Triple<Int, Int, Boolean>>()
            val states = CopyOnWriteArrayList<ConnectionStatus>()
            val server = tv.serve { s ->
                // Server speaks first. No client Configure/SetActive may precede this.
                s.outputStream.write(Fixtures.frame("server_configure")); s.outputStream.flush()
                assertArrayEquals(Fixtures.bytes("client_configure"), ProtoUtils.readFrame(s.inputStream))
                s.outputStream.write(Fixtures.frame("server_active")); s.outputStream.flush()
                assertArrayEquals(Fixtures.bytes("client_active"), ProtoUtils.readFrame(s.inputStream))
                s.outputStream.write(Fixtures.frame("ping")); s.outputStream.flush()
                assertArrayEquals(Fixtures.bytes("pong"), ProtoUtils.readFrame(s.inputStream))
                assertFalse(states.any { it is ConnectionStatus.Connected })
                // Coalesced inbound frames must all be consumed, including a post-start ping.
                s.outputStream.write(Fixtures.frame("remote_start") + Fixtures.frame("volume") + Fixtures.frame("ping"))
                s.outputStream.flush()
                val received = (1..4).map { ProtoUtils.readFrame(s.inputStream).toList() }
                assertTrue(received.contains(Fixtures.bytes("pong").toList()))
                for (name in listOf("key_up", "key_home", "key_volume_up")) assertTrue(received.contains(Fixtures.bytes(name).toList()))
                release.await(3, TimeUnit.SECONDS)
            }
            val client = AndroidTvRemoteV2Client("127.0.0.1", CertificateHelper().getOrCreateClientCertificate(),
                pinnedServerFingerprint = PairingCrypto.sha256Hex(tv.identity.certificate.encoded), remotePort = tv.socket.localPort,
                onStateChanged = { states.add(it) }, onVolume = { a, b, c -> volume.complete(Triple(a, b, c)) })
            try {
                assertTrue(client.connect().isSuccess)
                assertEquals(Triple(12, 50, true), withTimeout(2000) { volume.await() })
                for (key in listOf(TvCommand.DPadUp, TvCommand.Home, TvCommand.VolumeUp)) assertTrue(client.sendCommand(key).isSuccess)
                release.countDown()
                server.get(4, TimeUnit.SECONDS)
                withTimeout(2000) { while (client.isConnected) delay(10) }
                assertTrue(states.last() is ConnectionStatus.Failed)
            } finally { release.countDown(); client.disconnect() }
        }
    }

    @Test fun pairingStatusOnlyConfigurationDoesNotOpenPin() = runBlocking<Unit> {
        Peer().use { tv ->
            val server = tv.serve { s ->
                for ((request, reply) in listOf("pair_request" to "pair_request_ack", "pair_options" to "server_options",
                        "pair_configuration" to "status_only")) {
                    assertArrayEquals(Fixtures.bytes(request), ProtoUtils.readFrame(s.inputStream))
                    s.outputStream.write(Fixtures.frame(reply)); s.outputStream.flush()
                }
            }
            var prompted = false
            var diag = ConnectionDiagnostic(host = "127.0.0.1", port = tv.socket.localPort, service = "pairing")
            val client = AndroidTvRemoteV2Client("127.0.0.1", CertificateHelper().getOrCreateClientCertificate(),
                pairPort = tv.socket.localPort, diagnostics = AttemptDiagnostics(diag) { diag = it })
            assertTrue(client.pair { prompted = true; "A1B2C3" }.isFailure)
            assertFalse(prompted)
            assertEquals("ConfigurationAck", diag.stage)
            assertEquals("NoPayload", diag.lastIncoming)
            assertEquals(200, diag.serverStatus)
            assertEquals("Success", diag.tcp)
            assertTrue(diag.tls.startsWith("Success"))
            server.get(4, TimeUnit.SECONDS)
        }
    }

    @Test fun cancellingPairingDuringReadClosesSocketAndPropagatesCancellation() = runBlocking<Unit> {
        Peer().use { tv ->
            val requestRead = CompletableDeferred<Unit>()
            val server = tv.serve { s ->
                ProtoUtils.readFrame(s.inputStream)
                requestRead.complete(Unit)
                assertEquals(-1, s.inputStream.read())
            }
            val client = AndroidTvRemoteV2Client("127.0.0.1", CertificateHelper().getOrCreateClientCertificate(), pairPort = tv.socket.localPort)
            val job = launch { client.pair { error("PIN must not open") } }
            withTimeout(3000) { requestRead.await() }
            withTimeout(2000) { job.cancelAndJoin() }
            assertTrue(job.isCancelled)
            server.get(3, TimeUnit.SECONDS)
        }
    }

    @Test fun remoteCannotBecomeConnectedWithoutStart() = runBlocking<Unit> {
        Peer().use { tv ->
            val server = tv.serve { s ->
                s.outputStream.write(Fixtures.frame("server_configure")); s.outputStream.flush()
                ProtoUtils.readFrame(s.inputStream)
                assertEquals(-1, s.inputStream.read())
            }
            val states = CopyOnWriteArrayList<ConnectionStatus>()
            var diag = ConnectionDiagnostic(host = "127.0.0.1", port = tv.socket.localPort, service = "remote")
            val client = AndroidTvRemoteV2Client("127.0.0.1", CertificateHelper().getOrCreateClientCertificate(),
                pinnedServerFingerprint = PairingCrypto.sha256Hex(tv.identity.certificate.encoded), remotePort = tv.socket.localPort,
                onStateChanged = { states.add(it) }, diagnostics = AttemptDiagnostics(diag) { diag = it }, responseTimeoutMs = 1000)
            assertTrue(client.connect().isFailure)
            assertFalse(client.isConnected)
            assertFalse(states.any { it is ConnectionStatus.Connected })
            assertEquals("RemoteStart", diag.timeoutStage)
            server.get(4, TimeUnit.SECONDS)
        }
    }

    @Test fun pinnedCertificateMismatchFailsTls() = runBlocking<Unit> {
        Peer().use { tv ->
            val server = tv.executor.submit {
                (tv.socket.accept() as SSLSocket).use { s ->
                    s.soTimeout = 3000
                    try { s.startHandshake(); s.inputStream.read() } catch (_: java.io.IOException) { }
                }
            }
            var diag = ConnectionDiagnostic(host = "127.0.0.1", port = tv.socket.localPort, service = "remote")
            val client = AndroidTvRemoteV2Client("127.0.0.1", CertificateHelper().getOrCreateClientCertificate(),
                pinnedServerFingerprint = "00".repeat(32), remotePort = tv.socket.localPort,
                diagnostics = AttemptDiagnostics(diag) { diag = it })
            assertTrue(client.connect().isFailure)
            assertEquals("TLS", diag.stage)
            assertEquals("Failed", diag.tls)
            assertFalse(client.isConnected)
            server.get(4, TimeUnit.SECONDS)
        }
    }
}


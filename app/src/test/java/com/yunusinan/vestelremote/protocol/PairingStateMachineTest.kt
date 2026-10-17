package com.yunusinan.vestelremote.protocol

import com.yunusinan.vestelremote.data.crypto.CertificateHelper
import com.yunusinan.vestelremote.data.crypto.PairingCrypto
import com.yunusinan.vestelremote.data.crypto.TlsSocketFactoryHelper
import com.yunusinan.vestelremote.data.protocol.AndroidTvRemoteV2Client
import com.yunusinan.vestelremote.data.protocol.ProtoUtils
import com.yunusinan.vestelremote.domain.model.ConnectionStatus
import com.yunusinan.vestelremote.domain.model.TvDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.security.MessageDigest
import java.security.interfaces.RSAPublicKey
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLSocket

class PairingStateMachineTest {

    private val certHelper = CertificateHelper(null)
    private val clientCertPair = certHelper.getOrCreateClientCertificate()
    private val serverCertPair = certHelper.getOrCreateClientCertificate()

    private val testDevice = TvDevice(
        id = "test-tv-123",
        name = "Vestel 55QG9960",
        model = "55QG9960",
        ipAddress = "127.0.0.1"
    )

    @Test
    fun testPrematurePinDialogPreventedWhenConnectionFails() = runBlocking {
        var pinPromptCalled = false
        val states = mutableListOf<ConnectionStatus>()

        // Point to an unused local port that will refuse connection
        val client = AndroidTvRemoteV2Client(
            host = "127.0.0.1",
            keyCertPair = clientCertPair,
            pairPort = 65432,
            onStateChanged = { states.add(it) }
        )

        val result = client.pair(testDevice) {
            pinPromptCalled = true
            "A1B2C3"
        }

        assertTrue(result.isFailure)
        assertFalse("PIN dialog MUST NOT be prompted when connection fails!", pinPromptCalled)
        assertTrue(states.any { it is ConnectionStatus.ConnectingToPairingService })
        assertTrue(states.last() is ConnectionStatus.Failed)
    }

    @Test
    fun testPoloFullHandshakeTransitionsAndSecretVerification() = runBlocking {
        // Set up mock SSL Server Socket on loopback
        val serverSocket = withContext(Dispatchers.IO) {
            val sslContext = javax.net.ssl.SSLContext.getInstance("TLS")
            sslContext.init(
                arrayOf(object : javax.net.ssl.X509ExtendedKeyManager() {
                    override fun getClientAliases(k: String?, i: Array<out java.security.Principal>?): Array<String> = emptyArray()
                    override fun chooseClientAlias(k: Array<out String>?, i: Array<out java.security.Principal>?, s: java.net.Socket?): String? = null
                    override fun getServerAliases(k: String?, i: Array<out java.security.Principal>?): Array<String> = arrayOf("server")
                    override fun chooseServerAlias(k: String?, i: Array<out java.security.Principal>?, s: java.net.Socket?): String = "server"
                    override fun getCertificateChain(alias: String?): Array<java.security.cert.X509Certificate> = arrayOf(serverCertPair.certificate)
                    override fun getPrivateKey(alias: String?): java.security.PrivateKey = serverCertPair.privateKey
                }),
                arrayOf(object : javax.net.ssl.X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
                }),
                null
            )
            val s = sslContext.serverSocketFactory.createServerSocket(0, 1, InetAddress.getByName("127.0.0.1")) as SSLServerSocket
            s.needClientAuth = true
            s
        }
        val port = serverSocket.localPort

        // Precompute valid PIN for this client/server pair
        val clientRsa = clientCertPair.certificate.publicKey as RSAPublicKey
        val serverRsa = serverCertPair.certificate.publicKey as RSAPublicKey
        val clientMod = PairingCrypto.getUnsignedModulusBytes(clientRsa.modulus)
        val clientExp = PairingCrypto.getFormattedExponentBytes(clientRsa.publicExponent)
        val serverMod = PairingCrypto.getUnsignedModulusBytes(serverRsa.modulus)
        val serverExp = PairingCrypto.getFormattedExponentBytes(serverRsa.publicExponent)
        val nonceHex = "8C4E"
        val nonceBytes = PairingCrypto.hexToBytes(nonceHex)

        val md = MessageDigest.getInstance("SHA-256")
        md.update(clientMod)
        md.update(clientExp)
        md.update(serverMod)
        md.update(serverExp)
        md.update(nonceBytes)
        val expectedHash = md.digest()
        val checksumHex = "%02X".format(expectedHash[0].toInt() and 0xFF)
        val expectedPin = "$checksumHex$nonceHex"

        val serverJob = launch(Dispatchers.IO) {
            val sock = serverSocket.accept() as SSLSocket
            sock.soTimeout = 5000
            val `in` = sock.inputStream
            val out = sock.outputStream

            // 1. Read PairingRequest, Send PairingRequestAck (field 11)
            val msg1 = readLengthDelimited(`in`)
            val parsed1 = ProtoUtils.ProtoMessage.parse(msg1)
            assertEquals(200, parsed1.getInt(2))
            assertTrue(parsed1.hasField(10))
            assertEquals("atvremote", parsed1.getMessage(10)?.getString(1))

            val ack1 = ProtoUtils.MessageBuilder()
                .int32(1, 2)
                .int32(2, 200)
                .message(11, ProtoUtils.MessageBuilder().string(1, "GoogleTV7288"))
                .build()
            writeLengthDelimited(out, ack1)

            // 2. Read PairingOption, Send PairingOption response (field 20)
            val msg2 = readLengthDelimited(`in`)
            val parsed2 = ProtoUtils.ProtoMessage.parse(msg2)
            assertEquals(200, parsed2.getInt(2))
            assertTrue(parsed2.hasField(20))

            val ack2 = ProtoUtils.MessageBuilder()
                .int32(1, 2)
                .int32(2, 200)
                .message(20, ProtoUtils.MessageBuilder()
                    .message(2, ProtoUtils.MessageBuilder().int32(1, 3).int32(2, 6)).int32(3, 2))
                .build()
            writeLengthDelimited(out, ack2)

            // 3. Read PairingConfiguration, Send ConfigurationAck (field 31) -> TV triggers PIN display
            val msg3 = readLengthDelimited(`in`)
            val parsed3 = ProtoUtils.ProtoMessage.parse(msg3)
            assertEquals(200, parsed3.getInt(2))
            assertTrue(parsed3.hasField(30))

            val ack3 = ProtoUtils.MessageBuilder()
                .int32(1, 2)
                .int32(2, 200)
                .message(31, ByteArray(0))
                .build()
            writeLengthDelimited(out, ack3)

            // 4. Read PairingSecret, Send SecretAck (field 41)
            val msg4 = readLengthDelimited(`in`)
            val parsed4 = ProtoUtils.ProtoMessage.parse(msg4)
            assertEquals(200, parsed4.getInt(2))
            assertTrue(parsed4.hasField(40))
            val receivedSecret = parsed4.getMessage(40)?.getBytes(1)
            assertArrayEquals(expectedHash, receivedSecret)

            val ack4 = ProtoUtils.MessageBuilder()
                .int32(1, 2)
                .int32(2, 200)
                .message(41, ProtoUtils.MessageBuilder().bytes(1, expectedHash))
                .build()
            writeLengthDelimited(out, ack4)

            sock.close()
            serverSocket.close()
        }

        // Run client handshake
        val states = mutableListOf<ConnectionStatus>()
        var pinPromptObserved = false

        val client = AndroidTvRemoteV2Client(
            host = "127.0.0.1",
            keyCertPair = clientCertPair,
            pairPort = port,
            onStateChanged = { states.add(it) }
        )

        val pairResult = client.pair(testDevice) {
            pinPromptObserved = true
            expectedPin
        }

        assertTrue("Pairing handshake must succeed!", pairResult.isSuccess)
        assertTrue("PIN prompt MUST have been observed!", pinPromptObserved)
        assertTrue(states.any { it is ConnectionStatus.ConnectingToPairingService })
        assertTrue(states.any { it is ConnectionStatus.TlsEstablished })
        assertTrue(states.any { it is ConnectionStatus.PairingNegotiation })
        assertTrue(states.any { it is ConnectionStatus.AwaitingTvPin })
        assertTrue(states.any { it is ConnectionStatus.VerifyingPin })
        assertTrue(states.any { it is ConnectionStatus.Paired })

        serverJob.join()
    }

    @Test
    fun testServerRejectionWithStatus400PreventsPinPrompt() = runBlocking {
        val serverSocket = withContext(Dispatchers.IO) {
            val sslContext = javax.net.ssl.SSLContext.getInstance("TLS")
            sslContext.init(
                arrayOf(object : javax.net.ssl.X509ExtendedKeyManager() {
                    override fun getClientAliases(k: String?, i: Array<out java.security.Principal>?): Array<String> = emptyArray()
                    override fun chooseClientAlias(k: Array<out String>?, i: Array<out java.security.Principal>?, s: java.net.Socket?): String? = null
                    override fun getServerAliases(k: String?, i: Array<out java.security.Principal>?): Array<String> = arrayOf("server")
                    override fun chooseServerAlias(k: String?, i: Array<out java.security.Principal>?, s: java.net.Socket?): String = "server"
                    override fun getCertificateChain(alias: String?): Array<java.security.cert.X509Certificate> = arrayOf(serverCertPair.certificate)
                    override fun getPrivateKey(alias: String?): java.security.PrivateKey = serverCertPair.privateKey
                }),
                arrayOf(object : javax.net.ssl.X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
                }),
                null
            )
            val s = sslContext.serverSocketFactory.createServerSocket(0, 1, InetAddress.getByName("127.0.0.1")) as SSLServerSocket
            s.needClientAuth = true
            s
        }
        val port = serverSocket.localPort

        val serverJob = launch(Dispatchers.IO) {
            val sock = serverSocket.accept() as SSLSocket
            sock.soTimeout = 5000
            val `in` = sock.inputStream
            val out = sock.outputStream

            readLengthDelimited(`in`)

            // Respond with STATUS_ERROR (400)
            val errorMsg = ProtoUtils.MessageBuilder()
                .int32(1, 2)
                .int32(2, 400) // STATUS_ERROR
                .build()
            writeLengthDelimited(out, errorMsg)

            sock.close()
            serverSocket.close()
        }

        var pinPromptCalled = false
        val states = mutableListOf<ConnectionStatus>()
        val client = AndroidTvRemoteV2Client(
            host = "127.0.0.1",
            keyCertPair = clientCertPair,
            pairPort = port,
            onStateChanged = { states.add(it) }
        )

        val result = client.pair(testDevice) {
            pinPromptCalled = true
            "A1B2C3"
        }

        assertTrue(result.isFailure)
        assertFalse("PIN dialog MUST NOT be shown if server rejects pairing request!", pinPromptCalled)
        assertTrue(states.last() is ConnectionStatus.Failed)

        serverJob.join()
    }

    private fun writeLengthDelimited(out: OutputStream, bytes: ByteArray) {
        ProtoUtils.writeVarint(out, bytes.size.toLong())
        out.write(bytes)
        out.flush()
    }

    private fun readLengthDelimited(`in`: InputStream): ByteArray {
        val len = ProtoUtils.readVarint(`in`).toInt()
        val buf = ByteArray(len)
        var read = 0
        while (read < len) {
            val c = `in`.read(buf, read, len - read)
            if (c == -1) throw java.io.EOFException()
            read += c
        }
        return buf
    }
}

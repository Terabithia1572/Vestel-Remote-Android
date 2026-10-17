package com.yunusinan.vestelremote.protocol

import com.yunusinan.vestelremote.data.crypto.CertificateHelper
import com.yunusinan.vestelremote.data.crypto.PairingCrypto
import org.junit.Assert.*
import org.junit.Test
import java.math.BigInteger
import java.security.MessageDigest
import java.security.interfaces.RSAPublicKey

class PairingCryptoTest {

    @Test
    fun testIndependentPythonCryptoFixtureAndNoPinInErrors() {
        val properties = java.util.Properties().apply {
            PairingCryptoTest::class.java.getResourceAsStream("/protocol/synthetic-crypto.properties")!!.use { load(it) }
        }
        val factory = java.security.cert.CertificateFactory.getInstance("X.509")
        fun cert(name: String) = javaClass.getResourceAsStream("/protocol/synthetic-$name.der")!!.use {
            factory.generateCertificate(it) as java.security.cert.X509Certificate
        }
        val client = cert("client")
        val server = cert("server")
        assertArrayEquals(PairingCrypto.hexToBytes(properties.getProperty("secret")),
            PairingCrypto.computePairingSecret(client, server, properties.getProperty("pin")))
        for (invalid in listOf("not-a-pin", "12345", "١٢٣٤٥٦")) {
            val exception = assertThrows(IllegalArgumentException::class.java) {
                PairingCrypto.computePairingSecret(client, server, invalid)
            }
            assertFalse(exception.message.orEmpty().contains(invalid))
        }
    }

    @Test
    fun testSha256Digest() {
        val input = "VestelRemote55QG9960".toByteArray(Charsets.UTF_8)
        val hash = PairingCrypto.sha256Hex(input)
        assertNotNull(hash)
        assertEquals(64, hash.length)
    }

    @Test
    fun testParsePinToBytes() {
        // Hex PIN
        val hexPin = "A1B2C3"
        val bytes = PairingCrypto.parsePinToBytes(hexPin)
        assertEquals(3, bytes.size)
        assertEquals(0xA1.toByte(), bytes[0])
        assertEquals(0xB2.toByte(), bytes[1])
        assertEquals(0xC3.toByte(), bytes[2])

        // Numeric PIN
        val numPin = "559960"
        val numBytes = PairingCrypto.parsePinToBytes(numPin)
        assertEquals(3, numBytes.size)
        assertEquals(0x55.toByte(), numBytes[0])
        assertEquals(0x99.toByte(), numBytes[1])
        assertEquals(0x60.toByte(), numBytes[2])
    }

    @Test
    fun testPinNormalization() {
        assertEquals("A1B2C3", PairingCrypto.normalizePin("a1-b2-c3"))
        assertEquals("559960", PairingCrypto.normalizePin(" 55 99 60 "))
        assertEquals("8C4E19", PairingCrypto.normalizePin("8c4e19"))
    }

    @Test
    fun testModulusAndExponentFormatting() {
        // RSA Exponent 65537 (0x10001) must format as 3 bytes: 0x01, 0x00, 0x01
        val exp = BigInteger.valueOf(65537)
        val expBytes = PairingCrypto.getFormattedExponentBytes(exp)
        assertEquals(3, expBytes.size)
        assertEquals(0x01.toByte(), expBytes[0])
        assertEquals(0x00.toByte(), expBytes[1])
        assertEquals(0x01.toByte(), expBytes[2])

        // Modulus with high bit set must not contain sign byte (256 bytes for 2048-bit)
        val certHelper = CertificateHelper(null)
        val pair = certHelper.getOrCreateClientCertificate()
        val rsa = pair.certificate.publicKey as RSAPublicKey
        val modBytes = PairingCrypto.getUnsignedModulusBytes(rsa.modulus)
        assertEquals(256, modBytes.size)
    }

    @Test
    fun testPoloSecretComputationAndChecksumVerification() {
        val certHelper = CertificateHelper(null)
        val clientPair = certHelper.getOrCreateClientCertificate()
        val serverPair = certHelper.getOrCreateClientCertificate()

        val clientRsa = clientPair.certificate.publicKey as RSAPublicKey
        val serverRsa = serverPair.certificate.publicKey as RSAPublicKey

        val clientMod = PairingCrypto.getUnsignedModulusBytes(clientRsa.modulus)
        val clientExp = PairingCrypto.getFormattedExponentBytes(clientRsa.publicExponent)
        val serverMod = PairingCrypto.getUnsignedModulusBytes(serverRsa.modulus)
        val serverExp = PairingCrypto.getFormattedExponentBytes(serverRsa.publicExponent)

        // TV generates random 2-byte nonce (e.g. 0x4E, 0x19 -> "4E19")
        val nonceHex = "4E19"
        val nonceBytes = PairingCrypto.hexToBytes(nonceHex)

        val md = MessageDigest.getInstance("SHA-256")
        md.update(clientMod)
        md.update(clientExp)
        md.update(serverMod)
        md.update(serverExp)
        md.update(nonceBytes)
        val expectedHash = md.digest()

        val checksumHex = "%02X".format(expectedHash[0].toInt() and 0xFF)
        val tvDisplayedPin = "$checksumHex$nonceHex"

        // 1. Verify valid PIN succeeds
        assertTrue(PairingCrypto.verifyPinChecksum(clientPair.certificate, serverPair.certificate, tvDisplayedPin))
        val secret = PairingCrypto.computePairingSecret(clientPair.certificate, serverPair.certificate, tvDisplayedPin)
        assertArrayEquals(expectedHash, secret)

        // 2. Verify wrong checksum or bad PIN fails
        val wrongChecksum = if (checksumHex == "00") "01" else "00"
        val tamperedPin = "$wrongChecksum$nonceHex"
        assertFalse(PairingCrypto.verifyPinChecksum(clientPair.certificate, serverPair.certificate, tamperedPin))
    }
}

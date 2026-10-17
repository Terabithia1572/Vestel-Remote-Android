package com.yunusinan.vestelremote.data.crypto

import java.math.BigInteger
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey

object PairingCrypto {

    /**
     * Calculates the pairing secret for Android TV Remote v2 (Google Polo Protocol).
     *
     * In accordance with Google's Polo specification and official Android TV implementations:
     * 1. Extracts the 2048-bit RSA modulus and public exponent from client and server certificates.
     * 2. Hashes (SHA-256):
     *    - client_modulus (unsigned hex bytes, typically 256 bytes)
     *    - client_exponent (formatted hex bytes, e.g. 0x010001 for 65537)
     *    - server_modulus (unsigned hex bytes, typically 256 bytes)
     *    - server_exponent (formatted hex bytes, e.g. 0x010001)
     *    - nonce (last 4 characters of the 6-hex-digit PIN code)
     * 3. Validates that hash[0] matches the first 2 characters of the PIN (checksum byte).
     * 4. Returns the 32-byte SHA-256 digest to be sent in the PairingSecret message.
     */
    fun computePairingSecret(
        clientCert: X509Certificate,
        serverCert: X509Certificate,
        pin: String
    ): ByteArray {
        val cleanPin = normalizePin(pin)
        require(cleanPin.length == 6) {
            "PIN kodu 6 haneli onaltılık (hex) formatta olmalıdır"
        }
        require(cleanPin.all { it in '0'..'9' || it in 'A'..'F' }) {
            "PIN kodu yalnızca onaltılık karakterlerden (0-9, A-F) oluşmalıdır"
        }

        val clientRsa = clientCert.publicKey as? RSAPublicKey
            ?: throw IllegalArgumentException("İstemci sertifikası geçerli bir RSA ortak anahtarı içermiyor")
        val serverRsa = serverCert.publicKey as? RSAPublicKey
            ?: throw IllegalArgumentException("Televizyon sertifikası geçerli bir RSA ortak anahtarı içermiyor")

        val clientModulusBytes = getUnsignedModulusBytes(clientRsa.modulus)
        val clientExponentBytes = getFormattedExponentBytes(clientRsa.publicExponent)
        val serverModulusBytes = getUnsignedModulusBytes(serverRsa.modulus)
        val serverExponentBytes = getFormattedExponentBytes(serverRsa.publicExponent)
        val nonceBytes = hexToBytes(cleanPin.substring(2))

        val md = MessageDigest.getInstance("SHA-256")
        md.update(clientModulusBytes)
        md.update(clientExponentBytes)
        md.update(serverModulusBytes)
        md.update(serverExponentBytes)
        md.update(nonceBytes)
        val hashResult = md.digest()

        val expectedChecksum = cleanPin.substring(0, 2).toInt(16)
        val actualChecksum = hashResult[0].toInt() and 0xFF

        if (actualChecksum != expectedChecksum) {
            throw IllegalArgumentException(
                "Girilen PIN kodu doğrulanamadı. Lütfen TV ekranındaki kodu kontrol ediniz."
            )
        }

        return hashResult
    }

    /**
     * Verifies whether the provided PIN matches the checksum byte without throwing an exception.
     */
    fun verifyPinChecksum(
        clientCert: X509Certificate,
        serverCert: X509Certificate,
        pin: String
    ): Boolean {
        return try {
            computePairingSecret(clientCert, serverCert, pin)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getUnsignedModulusBytes(modulus: BigInteger): ByteArray {
        var hex = modulus.toString(16).uppercase()
        if (hex.length % 2 != 0) {
            hex = "0$hex"
        }
        return hexToBytes(hex)
    }

    fun getFormattedExponentBytes(exponent: BigInteger): ByteArray {
        var hex = exponent.toString(16).uppercase()
        if (hex.length % 2 != 0) {
            hex = "0$hex"
        }
        return hexToBytes(hex)
    }

    fun normalizePin(pin: String): String {
        return pin.trim().replace(" ", "").replace("-", "").uppercase()
    }

    fun hexToBytes(hex: String): ByteArray {
        val clean = if (hex.length % 2 != 0) "0$hex" else hex
        val result = ByteArray(clean.length / 2)
        for (i in result.indices) {
            val index = i * 2
            result[i] = clean.substring(index, index + 2).toInt(16).toByte()
        }
        return result
    }

    fun sha256(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(data)
    }

    fun sha256Hex(data: ByteArray): String {
        return sha256(data).joinToString("") { "%02x".format(it) }
    }

    fun parsePinToBytes(pin: String): ByteArray {
        val clean = normalizePin(pin)
        return if (clean.length % 2 == 0 && clean.all { it.isDigit() || it in 'A'..'F' }) {
            try {
                hexToBytes(clean)
            } catch (_: Exception) {
                clean.toByteArray(Charsets.UTF_8)
            }
        } else {
            clean.toByteArray(Charsets.UTF_8)
        }
    }
}

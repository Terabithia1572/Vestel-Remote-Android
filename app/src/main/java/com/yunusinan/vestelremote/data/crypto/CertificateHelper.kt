package com.yunusinan.vestelremote.data.crypto

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.*
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import javax.security.auth.x500.X500Principal

class CertificateHelper(private val context: Context? = null) {

    data class KeyCertPair(
        val privateKey: PrivateKey,
        val certificate: X509Certificate,
        val storage: String = "Software",
        val supportsTlsSigning: Boolean = true
    )

    private val legacyAlias = "VestelRemoteClientKey"
    private val tlsAlias = "VestelRemoteClientKeyTlsV2"
    private val keyStoreType = "AndroidKeyStore"

    fun getOrCreateClientCertificate(forPairing: Boolean = false): KeyCertPair {
        // Keep the selected identity stable. Never hide Keystore failures behind a new software key.
        if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val ks = KeyStore.getInstance(keyStoreType).apply { load(null) }
            var keyAlias = if (ks.containsAlias(tlsAlias)) tlsAlias else legacyAlias
            if (ks.containsAlias(keyAlias)) {
                val privKey = ks.getKey(keyAlias, null) as? PrivateKey
                val cert = ks.getCertificate(keyAlias) as? X509Certificate
                if (privKey != null && cert != null) {
                    val info = KeyFactory.getInstance("RSA", keyStoreType).getKeySpec(privKey, KeyInfo::class.java)
                    val tlsCapable = KeyProperties.DIGEST_NONE in info.digests &&
                        KeyProperties.ENCRYPTION_PADDING_NONE in info.encryptionPaddings
                    if (tlsCapable || !forPairing) {
                        return KeyCertPair(privKey, cert,
                            if (tlsCapable) "AndroidKeyStore/TLS" else "AndroidKeyStore/LegacyRestricted",
                            tlsCapable)
                    }
                    // Key authorizations are immutable. Preserve the legacy alias and create a
                    // separate identity only during an explicit pairing operation.
                    keyAlias = tlsAlias
                } else {
                    error("Kayıtlı istemci kimliği okunamadı; otomatik olarak değiştirilmeyecek")
                }
            }

            // Preserve an existing software identity from older installations.
            if (!ks.containsAlias(legacyAlias) &&
                (File(context.filesDir, "client_key.pk8").exists() || File(context.filesDir, "client_cert.der").exists())) {
                return getOrCreateSoftwareCertificate()
            }

            // Generate in AndroidKeyStore
            val kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                keyStoreType
            )
            val spec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setKeySize(2048)
                .setDigests(KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1, KeyProperties.SIGNATURE_PADDING_RSA_PSS)
                // Conscrypt delegates pre-encoded RSA signatures through Cipher/NoPadding.
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setCertificateSubject(X500Principal("CN=VestelRemote, O=Yunus INAN"))
                .setCertificateSerialNumber(BigInteger.valueOf(System.currentTimeMillis()))
                .setCertificateNotBefore(Date(System.currentTimeMillis() - 24 * 3600 * 1000L))
                .setCertificateNotAfter(Date(System.currentTimeMillis() + 20 * 365 * 24 * 3600 * 1000L))
                .build()
            kpg.initialize(spec)
            val pair = kpg.generateKeyPair()
            val cert = ks.getCertificate(keyAlias) as X509Certificate
            return KeyCertPair(pair.private, cert, "AndroidKeyStore/TLS")
        }

        // JVM tests use an in-memory software identity.
        return getOrCreateSoftwareCertificate()
    }

    private fun getOrCreateSoftwareCertificate(): KeyCertPair {
        val filesDir = context?.filesDir
        val keyFile = if (filesDir != null) File(filesDir, "client_key.pk8") else null
        val certFile = if (filesDir != null) File(filesDir, "client_cert.der") else null

        if (keyFile != null && certFile != null && (keyFile.exists() || certFile.exists())) {
            check(keyFile.exists() && certFile.exists()) { "Kayıtlı istemci kimliği eksik" }
            val kf = KeyFactory.getInstance("RSA")
            val keyBytes = keyFile.readBytes()
            val privKey = kf.generatePrivate(PKCS8EncodedKeySpec(keyBytes))

            val cf = CertificateFactory.getInstance("X.509")
            val cert = FileInputStream(certFile).use { cf.generateCertificate(it) as X509Certificate }
            val proof = Signature.getInstance("SHA256withRSA").run {
                initSign(privKey); update(byteArrayOf(1)); sign()
            }
            check(Signature.getInstance("SHA256withRSA").run {
                initVerify(cert); update(byteArrayOf(1)); verify(proof)
            }) { "Kayıtlı anahtar ve sertifika eşleşmiyor" }
            return KeyCertPair(privKey, cert)
        }

        // Generate 2048-bit RSA key pair
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        val keyPair = kpg.generateKeyPair()

        // Generate self-signed X.509 certificate
        val cert = generateSelfSignedCert(keyPair, "CN=VestelRemote, O=Yunus INAN")

        if (keyFile != null && certFile != null) {
            FileOutputStream(keyFile).use { it.write(keyPair.private.encoded) }
            FileOutputStream(certFile).use { it.write(cert.encoded) }
        }

        return KeyCertPair(keyPair.private, cert)
    }

    /**
     * Self-contained self-signed X.509 certificate generator using standard Java Security
     */
    private fun generateSelfSignedCert(keyPair: KeyPair, dn: String): X509Certificate {
        val certBytes = createDerX509Certificate(keyPair, dn)
        val cf = CertificateFactory.getInstance("X.509")
        return cf.generateCertificate(ByteArrayInputStream(certBytes)) as X509Certificate
    }

    private fun createDerX509Certificate(keyPair: KeyPair, dn: String): ByteArray {
        val now = System.currentTimeMillis()
        val notBefore = Date(now - 24 * 3600 * 1000L)
        val notAfter = Date(now + 20 * 365 * 24 * 3600 * 1000L)
        val serial = BigInteger.valueOf(now)

        // Minimal valid X.509 v1 ASN.1 DER structure
        // TBSCertificate:
        //   version: v1 (default, omitted)
        //   serialNumber: INTEGER
        //   signature: AlgorithmIdentifier (sha256WithRSAEncryption 1.2.840.113549.1.1.11)
        //   issuer: Name (CN=VestelRemote)
        //   validity: Validity (notBefore, notAfter UTCTime)
        //   subject: Name (CN=VestelRemote)
        //   subjectPublicKeyInfo: SubjectPublicKeyInfo
        val sigAlgOid = byteArrayOf(0x06, 0x09, 0x2A, 0x86.toByte(), 0x48, 0x86.toByte(), 0xF7.toByte(), 0x0D, 0x01, 0x01, 0x0B, 0x05, 0x00)
        val sigAlgSeq = derSequence(sigAlgOid)

        val serialDer = derInteger(serial)
        val dnDer = createX500NameDer("VestelRemote")
        val validityDer = createValidityDer(notBefore, notAfter)
        val pubKeyInfoDer = keyPair.public.encoded

        val tbsComponents = mutableListOf<ByteArray>()
        tbsComponents.add(serialDer)
        tbsComponents.add(sigAlgSeq)
        tbsComponents.add(dnDer) // issuer
        tbsComponents.add(validityDer)
        tbsComponents.add(dnDer) // subject
        tbsComponents.add(pubKeyInfoDer)

        val tbsBytes = derSequence(tbsComponents.reduce { acc, bytes -> acc + bytes })

        // Sign TBS with private key
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(keyPair.private)
        signer.update(tbsBytes)
        val signature = signer.sign()

        val sigBitString = derBitString(signature)

        // Complete Certificate SEQUENCE { tbsCertificate, signatureAlgorithm, signatureValue }
        return derSequence(tbsBytes + sigAlgSeq + sigBitString)
    }

    private fun derSequence(content: ByteArray): ByteArray {
        val len = derLength(content.size)
        return byteArrayOf(0x30) + len + content
    }

    private fun derInteger(value: BigInteger): ByteArray {
        val bytes = value.toByteArray()
        return byteArrayOf(0x02) + derLength(bytes.size) + bytes
    }

    private fun derBitString(bytes: ByteArray): ByteArray {
        val content = byteArrayOf(0x00) + bytes
        return byteArrayOf(0x03) + derLength(content.size) + content
    }

    private fun derLength(length: Int): ByteArray {
        return if (length < 128) {
            byteArrayOf(length.toByte())
        } else if (length < 256) {
            byteArrayOf(0x81.toByte(), length.toByte())
        } else {
            byteArrayOf(0x82.toByte(), (length shr 8).toByte(), (length and 0xFF).toByte())
        }
    }

    private fun createX500NameDer(commonName: String): ByteArray {
        // CN OID: 2.5.4.3 -> 06 03 55 04 03
        val cnOid = byteArrayOf(0x06, 0x03, 0x55, 0x04, 0x03)
        val cnValue = commonName.toByteArray(Charsets.UTF_8)
        val utf8String = byteArrayOf(0x0C) + derLength(cnValue.size) + cnValue
        val atv = derSequence(cnOid + utf8String)
        val rdn = byteArrayOf(0x31) + derLength(atv.size) + atv
        return derSequence(rdn)
    }

    private fun createValidityDer(notBefore: Date, notAfter: Date): ByteArray {
        val sdf = java.text.SimpleDateFormat("yyMMddHHmmss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val nbBytes = sdf.format(notBefore).toByteArray(Charsets.US_ASCII)
        val naBytes = sdf.format(notAfter).toByteArray(Charsets.US_ASCII)

        val nbDer = byteArrayOf(0x17) + derLength(nbBytes.size) + nbBytes
        val naDer = byteArrayOf(0x17) + derLength(naBytes.size) + naBytes
        return derSequence(nbDer + naDer)
    }
}

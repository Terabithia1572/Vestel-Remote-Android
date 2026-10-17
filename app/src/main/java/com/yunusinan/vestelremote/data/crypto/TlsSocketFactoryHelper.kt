package com.yunusinan.vestelremote.data.crypto

import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.net.ssl.*

class TlsSocketFactoryHelper(
    private val keyCertPair: CertificateHelper.KeyCertPair,
    private val expectedServerFingerprint: String? = null,
    private val onServerCertReceived: ((X509Certificate) -> Unit)? = null
) {

    fun createSSLSocketFactory(): Pair<SSLSocketFactory, X509TrustManager> {
        val keyManager = object : X509ExtendedKeyManager() {
            override fun getClientAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? =
                if (keyType == "RSA") arrayOf("client") else null

            override fun chooseClientAlias(keyType: Array<out String>?, issuers: Array<out Principal>?, socket: Socket?): String? =
                if (keyType?.contains("RSA") == true) "client" else null

            override fun getCertificateChain(alias: String?): Array<X509Certificate> = arrayOf(keyCertPair.certificate)

            override fun getPrivateKey(alias: String?): PrivateKey = keyCertPair.privateKey

            override fun getServerAliases(keyType: String?, issuers: Array<out Principal>?): Array<String> = emptyArray()

            override fun chooseServerAlias(keyType: String?, issuers: Array<out Principal>?, socket: Socket?): String? = null
        }

        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                if (chain.isNullOrEmpty()) {
                    throw SSLException("Server certificate chain is empty")
                }
                val serverCert = chain[0]
                onServerCertReceived?.invoke(serverCert)

                if (expectedServerFingerprint != null) {
                    val receivedFingerprint = PairingCrypto.sha256Hex(serverCert.encoded)
                    if (!receivedFingerprint.equals(expectedServerFingerprint, ignoreCase = true)) {
                        throw SSLException("Server certificate mismatch")
                    }
                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(arrayOf(keyManager), arrayOf(trustManager), null)
        return Pair(sslContext.socketFactory, trustManager)
    }
}

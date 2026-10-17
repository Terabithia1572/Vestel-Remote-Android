package com.yunusinan.vestelremote.data.protocol

import com.yunusinan.vestelremote.domain.model.ConnectionDiagnostic
import java.io.EOFException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException

class ProtocolFailure(message: String) : java.io.IOException(message)

/** Raw exception messages can include secrets. Only our fixed protocol messages are retained. */
fun safeConnectionError(error: Throwable): String = when (error) {
    is ProtocolFailure -> error.message ?: "Protokol hatası"
    is SocketTimeoutException, is TimeoutCancellationException -> "İşlem zaman aşımına uğradı"
    is CancellationException -> "İşlem iptal edildi"
    is SSLException -> "TLS el sıkışması veya sertifika doğrulaması başarısız"
    is ConnectException -> "TCP bağlantısı kurulamadı"
    is UnknownHostException -> "Hedef adres çözümlenemedi"
    is EOFException -> "TV bağlantıyı kapattı veya eksik çerçeve gönderdi"
    is IllegalArgumentException -> "Kod veya protokol verisi doğrulanamadı"
    else -> "Bağlantı işlemi başarısız; ham hata metni gizlendi"
}

class AttemptDiagnostics(
    initial: ConnectionDiagnostic,
    private val publish: (ConnectionDiagnostic) -> Unit = {}
) {
    private var value = initial
    init { publish(value) }

    @Synchronized
    fun update(change: (ConnectionDiagnostic) -> ConnectionDiagnostic) {
        value = change(value)
        publish(value)
    }

    fun stage(stage: String, state: String = stage) = update {
        it.copy(stage = stage, state = state, transitions = (it.transitions + stage).takeLast(32))
    }

    fun failed(error: Throwable) = update {
        it.copy(
            state = if (error is CancellationException && error !is TimeoutCancellationException) "Cancelled" else "Failed",
            tcp = if (it.stage == "TCP") "Failed" else it.tcp,
            tls = if (it.stage == "TLS") "Failed" else it.tls,
            timeoutStage = if (error is SocketTimeoutException || error is TimeoutCancellationException) it.stage else null,
            exceptionClass = error.javaClass.name,
            exceptionMessage = safeConnectionError(error)
        )
    }
}

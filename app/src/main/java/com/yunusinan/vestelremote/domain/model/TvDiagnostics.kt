package com.yunusinan.vestelremote.domain.model

data class TvDiagnostics(
    val targetIp: String,
    val pingSuccess: Boolean = false,
    val pingLatencyMs: Long = -1,
    val remotePort6466Open: Boolean = false,
    val pairingPort6467Open: Boolean = false,
    val dialPort8008Open: Boolean = false,
    val castPort8009Open: Boolean = false,
    val adbPort5555Open: Boolean = false,
    val tlsHandshakeSuccess: Boolean? = null,
    val dialServiceFound: Boolean = false,
    val summary: String = ""
)

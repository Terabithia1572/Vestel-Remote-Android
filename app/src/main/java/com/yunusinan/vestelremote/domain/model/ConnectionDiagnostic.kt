package com.yunusinan.vestelremote.domain.model

import java.util.UUID

/** Metadata only: never contains protocol payloads, PINs, certificates or keys. */
data class ConnectionDiagnostic(
    val id: String = UUID.randomUUID().toString(),
    val startedAt: Long = System.currentTimeMillis(),
    val host: String,
    val port: Int,
    val service: String,
    val stage: String = "ClientIdentity",
    val state: String = "Starting",
    val tcp: String = "NotAttempted",
    val tls: String = "NotAttempted",
    val identity: String = "Unknown",
    val lastOutgoing: String = "None",
    val lastIncoming: String = "None",
    val serverStatus: Int? = null,
    val timeoutStage: String? = null,
    val exceptionClass: String? = null,
    val exceptionMessage: String? = null,
    val transitions: List<String> = emptyList()
) {
    fun export(): String = """
        attempt=$id timestampMs=$startedAt
        target=$host:$port service=$service
        stage=$stage state=$state identity=$identity
        tcp=$tcp tls=$tls
        lastOutgoing=$lastOutgoing lastIncoming=$lastIncoming status=${serverStatus ?: "N/A"}
        timeoutStage=${timeoutStage ?: "None"}
        exception=${exceptionClass ?: "None"} message=${exceptionMessage ?: "None"}
        transitions=${transitions.joinToString(" -> ")}
    """.trimIndent()
}

package com.yunusinan.vestelremote.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ProtocolEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val direction: Direction,
    val title: String,
    val detail: String
) {
    enum class Direction {
        OUTGOING,
        INCOMING,
        SYSTEM,
        ERROR
    }

    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
}

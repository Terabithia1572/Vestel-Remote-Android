package com.yunusinan.vestelremote.domain.model

data class TvDevice(
    val id: String,
    val name: String = "Vestel Android TV",
    val model: String = "55QG9960",
    val ipAddress: String,
    val macAddress: String? = null,
    val port: Int = 6466,
    val pairingPort: Int = 6467,
    val isPaired: Boolean = false,
    val serverFingerprint: String? = null,
    val isOnline: Boolean = false,
    val hasAndroidTvRemote2: Boolean = true,
    val hasDial: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
) {
    val displayTitle: String
        get() = if (name.isNotBlank()) name else "Vestel $model"

    val isConfigured: Boolean
        get() = ipAddress.isNotBlank() && ipAddress != "0.0.0.0"
}

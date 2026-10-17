package com.yunusinan.vestelremote.domain.model

data class TvInput(
    val id: String,
    val name: String,
    val type: InputType,
    val isConnected: Boolean = true,
    val isCurrent: Boolean = false
) {
    enum class InputType {
        HDMI,
        LIVE_TV,
        AV
    }

    companion object {
        val DEFAULT_INPUTS = listOf(
            TvInput("hdmi_1", "HDMI 1", InputType.HDMI, isConnected = true),
            TvInput("hdmi_2", "HDMI 2 (eARC)", InputType.HDMI, isConnected = true),
            TvInput("hdmi_3", "HDMI 3", InputType.HDMI, isConnected = true),
            TvInput("live_tv", "Canlı TV (Uydu/Karasal)", InputType.LIVE_TV, isConnected = true),
            TvInput("av", "AV / Kompozit", InputType.AV, isConnected = false)
        )
    }
}

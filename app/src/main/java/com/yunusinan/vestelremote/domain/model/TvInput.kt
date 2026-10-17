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
        AV,
        SATELLITE,
        CABLE,
        ANTENNA,
        SCREEN_SHARE,
        LIVE_TV
    }

    companion object {
        val DEFAULT_INPUTS = listOf(
            TvInput("hdmi_1", "HDMI 1", InputType.HDMI, isConnected = true),
            TvInput("hdmi_2", "HDMI 2", InputType.HDMI, isConnected = true),
            TvInput("hdmi_3", "HDMI 3", InputType.HDMI, isConnected = true),
            TvInput("av", "AV", InputType.AV, isConnected = true),
            TvInput("satellite", "Uydu", InputType.SATELLITE, isConnected = true),
            TvInput("cable_tv", "Kablo TV", InputType.CABLE, isConnected = true),
            TvInput("antenna", "Anten", InputType.ANTENNA, isConnected = true),
            TvInput("screen_share", "Ekran Paylaşımı", InputType.SCREEN_SHARE, isConnected = true)
        )
    }
}


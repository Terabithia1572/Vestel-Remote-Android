package com.yunusinan.vestelremote.domain.model

sealed class TvCommand(val name: String, val androidKeyCode: Int) {
    // Navigation
    object DPadUp : TvCommand("DPAD_UP", 19)
    object DPadDown : TvCommand("DPAD_DOWN", 20)
    object DPadLeft : TvCommand("DPAD_LEFT", 21)
    object DPadRight : TvCommand("DPAD_RIGHT", 22)
    object Select : TvCommand("DPAD_CENTER", 23)
    object Back : TvCommand("BACK", 4)
    object Home : TvCommand("HOME", 3)
    object Menu : TvCommand("MENU", 82)
    object Assistant : TvCommand("ASSIST", 219)

    // Volume & Sound
    object VolumeUp : TvCommand("VOLUME_UP", 24)
    object VolumeDown : TvCommand("VOLUME_DOWN", 25)
    object Mute : TvCommand("VOLUME_MUTE", 164)

    // Power & Channels
    object Power : TvCommand("POWER", 26)
    object ChannelUp : TvCommand("CHANNEL_UP", 166)
    object ChannelDown : TvCommand("CHANNEL_DOWN", 167)

    // Media Playback
    object PlayPause : TvCommand("MEDIA_PLAY_PAUSE", 85)
    object Play : TvCommand("MEDIA_PLAY", 126)
    object Pause : TvCommand("MEDIA_PAUSE", 127)
    object Rewind : TvCommand("MEDIA_REWIND", 89)
    object FastForward : TvCommand("MEDIA_FAST_FORWARD", 90)
    object Stop : TvCommand("MEDIA_STOP", 86)

    // Color buttons (Vestel TV Broadcast & Apps)
    object Red : TvCommand("PROG_RED", 183)
    object Green : TvCommand("PROG_GREEN", 184)
    object Yellow : TvCommand("PROG_YELLOW", 185)
    object Blue : TvCommand("PROG_BLUE", 186)

    // Info, Guide & Captions
    object Info : TvCommand("INFO", 165)
    object Guide : TvCommand("GUIDE", 172)
    object Captions : TvCommand("CAPTIONS", 175)
    object Input : TvCommand("TV_INPUT", 178)

    // Numeric keypad
    data class Number(val digit: Int) : TvCommand("NUM_$digit", 7 + (digit % 10))
}


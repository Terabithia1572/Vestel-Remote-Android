package com.yunusinan.vestelremote.protocol

import com.yunusinan.vestelremote.domain.model.TvCommand
import org.junit.Assert.assertEquals
import org.junit.Test

class TvCommandEncodingTest {

    @Test
    fun testAndroidKeyCodesMatchOfficialSpec() {
        // Android KeyEvent constants:
        // KEYCODE_DPAD_UP = 19
        // KEYCODE_DPAD_DOWN = 20
        // KEYCODE_DPAD_LEFT = 21
        // KEYCODE_DPAD_RIGHT = 22
        // KEYCODE_DPAD_CENTER = 23
        // KEYCODE_BACK = 4
        // KEYCODE_HOME = 3
        // KEYCODE_VOLUME_UP = 24
        // KEYCODE_VOLUME_DOWN = 25
        // KEYCODE_VOLUME_MUTE = 164
        // KEYCODE_POWER = 26
        // KEYCODE_MEDIA_PLAY_PAUSE = 85

        assertEquals(19, TvCommand.DPadUp.androidKeyCode)
        assertEquals(20, TvCommand.DPadDown.androidKeyCode)
        assertEquals(21, TvCommand.DPadLeft.androidKeyCode)
        assertEquals(22, TvCommand.DPadRight.androidKeyCode)
        assertEquals(23, TvCommand.Select.androidKeyCode)
        assertEquals(4, TvCommand.Back.androidKeyCode)
        assertEquals(3, TvCommand.Home.androidKeyCode)
        assertEquals(24, TvCommand.VolumeUp.androidKeyCode)
        assertEquals(25, TvCommand.VolumeDown.androidKeyCode)
        assertEquals(164, TvCommand.Mute.androidKeyCode)
        assertEquals(26, TvCommand.Power.androidKeyCode)
        assertEquals(85, TvCommand.PlayPause.androidKeyCode)
        assertEquals(183, TvCommand.Red.androidKeyCode)
        assertEquals(184, TvCommand.Green.androidKeyCode)
        assertEquals(185, TvCommand.Yellow.androidKeyCode)
        assertEquals(186, TvCommand.Blue.androidKeyCode)
    }

    @Test
    fun testNumberKeyCodes() {
        // KEYCODE_0 = 7, KEYCODE_1 = 8, ..., KEYCODE_9 = 16
        assertEquals(7, TvCommand.Number(0).androidKeyCode)
        assertEquals(8, TvCommand.Number(1).androidKeyCode)
        assertEquals(12, TvCommand.Number(5).androidKeyCode)
        assertEquals(16, TvCommand.Number(9).androidKeyCode)
    }
}

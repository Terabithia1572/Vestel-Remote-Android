package com.yunusinan.vestelremote

import com.yunusinan.vestelremote.data.protocol.DialClient
import com.yunusinan.vestelremote.domain.model.AppDiscoveryState
import com.yunusinan.vestelremote.domain.model.TvCommand
import com.yunusinan.vestelremote.ui.viewmodel.VolumeSliderController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VolumeAndAppDiscoveryTest {

    @Test
    fun `rapid slider movement coalesces frames and rate limits commands`() = runTest {
        val sentCommands = mutableListOf<TvCommand>()
        val controller = VolumeSliderController(
            scope = backgroundScope,
            stepIntervalMs = 140L,
            maxStepsPerGesture = 10,
            sendVolumeCommand = { cmd -> sentCommands.add(cmd) }
        )

        controller.onTvVolumeUpdated(20, isFromRealTvOrMock = true)

        // Simulate 30 rapid drag frames in the same millisecond (20 -> 50)
        for (v in 21..50) {
            controller.onSliderValueChanged(v)
        }
        runCurrent()

        // Only 1 step should have fired immediately, NOT 30 concurrent coroutines!
        assertEquals(1, sentCommands.size)
        assertEquals(TvCommand.VolumeUp, sentCommands.first())

        // Advance by 2 intervals (280ms) -> 2 more steps should fire (total 3)
        advanceTimeBy(280L)
        runCurrent()
        assertEquals(3, sentCommands.size)

        // Clean up gesture
        controller.onSliderInteractionFinished()
    }

    @Test
    fun `releasing slider immediately cancels pending volume commands`() = runTest {
        val sentCommands = mutableListOf<TvCommand>()
        val controller = VolumeSliderController(
            scope = backgroundScope,
            stepIntervalMs = 140L,
            maxStepsPerGesture = 15,
            sendVolumeCommand = { cmd -> sentCommands.add(cmd) }
        )

        controller.onTvVolumeUpdated(15, isFromRealTvOrMock = true)

        // User drags slider from 15 to 60
        controller.onSliderValueChanged(30)
        controller.onSliderValueChanged(45)
        controller.onSliderValueChanged(60)
        runCurrent()

        // First step sent at t=0
        assertEquals(1, sentCommands.size)

        // One more step at t=140ms
        advanceTimeBy(140L)
        runCurrent()
        assertEquals(2, sentCommands.size)

        // User releases slider at t=160ms
        advanceTimeBy(20L)
        controller.onSliderInteractionFinished()
        runCurrent()

        val countAtRelease = sentCommands.size
        assertEquals(2, countAtRelease)
        assertFalse(controller.state.value.isUserDragging)
        assertFalse(controller.state.value.isAdjusting)

        // Even after waiting 10 full seconds, ZERO additional volume commands may be sent
        advanceTimeBy(10_000L)
        runCurrent()
        assertEquals(countAtRelease, sentCommands.size)
    }

    @Test
    fun `direction reversal immediately cancels previous direction steps`() = runTest {
        val sentCommands = mutableListOf<TvCommand>()
        val controller = VolumeSliderController(
            scope = backgroundScope,
            stepIntervalMs = 140L,
            maxStepsPerGesture = 15,
            sendVolumeCommand = { cmd -> sentCommands.add(cmd) }
        )

        controller.onTvVolumeUpdated(20, isFromRealTvOrMock = true)

        // Drag UP toward 35
        controller.onSliderValueChanged(28)
        controller.onSliderValueChanged(35)
        runCurrent()
        assertEquals(listOf(TvCommand.VolumeUp), sentCommands)

        // Before next upward step fires, user reverses finger direction DOWN to 18
        advanceTimeBy(50L)
        controller.onSliderValueChanged(18)
        runCurrent()

        // The upward loop was cancelled and a downward step was sent instead
        assertEquals(2, sentCommands.size)
        assertEquals(TvCommand.VolumeUp, sentCommands[0])
        assertEquals(TvCommand.VolumeDown, sentCommands[1])

        // Release slider and confirm no further commands fire
        controller.onSliderInteractionFinished()
        advanceTimeBy(5_000L)
        runCurrent()
        assertEquals(2, sentCommands.size)
    }

    @Test
    fun `dial service xml parser accurately distinguishes verified apps from shortcuts`() {
        val runningXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <service xmlns="urn:dial-multiscreen-org:schemas:dial" dialVer="2.1">
                <name>YouTube</name>
                <options allowStop="true"/>
                <state>running</state>
            </service>
        """.trimIndent()

        val runningInfo = DialClient.parseDialServiceXml(
            appId = "youtube",
            dialName = "YouTube",
            httpStatusCode = 200,
            xmlBody = runningXml
        )
        assertEquals(AppDiscoveryState.VERIFIED_RUNNING, runningInfo.state)
        assertTrue(runningInfo.isVerifiedOnTv)
        assertTrue(runningInfo.allowStop)

        val stoppedXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <service xmlns="urn:dial-multiscreen-org:schemas:dial">
                <name>Netflix</name>
                <state>stopped</state>
            </service>
        """.trimIndent()

        val stoppedInfo = DialClient.parseDialServiceXml(
            appId = "netflix",
            dialName = "Netflix",
            httpStatusCode = 200,
            xmlBody = stoppedXml
        )
        assertEquals(AppDiscoveryState.VERIFIED_STOPPED, stoppedInfo.state)
        assertTrue(stoppedInfo.isVerifiedOnTv)

        val notFoundInfo = DialClient.parseDialServiceXml(
            appId = "exxen",
            dialName = "Exxen",
            httpStatusCode = 404,
            xmlBody = ""
        )
        assertEquals(AppDiscoveryState.DIAL_NOT_REGISTERED, notFoundInfo.state)
        assertFalse(notFoundInfo.isVerifiedOnTv)
    }
}

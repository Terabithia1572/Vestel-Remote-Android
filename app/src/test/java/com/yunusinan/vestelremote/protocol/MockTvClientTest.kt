package com.yunusinan.vestelremote.protocol

import com.yunusinan.vestelremote.data.protocol.MockTvClient
import com.yunusinan.vestelremote.domain.model.TvApp
import com.yunusinan.vestelremote.domain.model.TvCommand
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MockTvClientTest {

    private lateinit var mockClient: MockTvClient

    @Before
    fun setUp() {
        mockClient = MockTvClient()
    }

    @Test
    fun testPairingSuccessWithCorrectPin() = runBlocking {
        val result = mockClient.pair { "559960" }
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testPairingFailureWithWrongPin() = runBlocking {
        val result = mockClient.pair { "999999" }
        assertTrue(result.isFailure)
    }

    @Test
    fun testConnectionAndVolumeCommands() = runBlocking {
        val connResult = mockClient.connect()
        assertTrue(connResult.isSuccess)
        assertTrue(mockClient.isConnected)

        val initialVol = mockClient.volumeLevel
        mockClient.sendCommand(TvCommand.VolumeUp)
        assertEquals(initialVol + 1, mockClient.volumeLevel)

        mockClient.sendCommand(TvCommand.VolumeDown)
        assertEquals(initialVol, mockClient.volumeLevel)

        mockClient.sendCommand(TvCommand.Mute)
        assertTrue(mockClient.isMuted)

        mockClient.sendCommand(TvCommand.Mute)
        assertFalse(mockClient.isMuted)
    }

    @Test
    fun testAppLaunchStateChange() = runBlocking {
        mockClient.connect()
        val app = TvApp(
            id = "netflix",
            name = "Netflix",
            packageName = "com.netflix.ninja",
            dialName = "Netflix"
        )
        val result = mockClient.launchApp(app)
        assertTrue(result.isSuccess)
        assertEquals("Netflix", mockClient.currentApp)
    }
}

package com.yunusinan.vestelremote.data.repository

import com.yunusinan.vestelremote.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface TvRepository {
    val connectionStatus: StateFlow<ConnectionStatus>
    val activeDevice: StateFlow<TvDevice?>
    val savedDevices: StateFlow<List<TvDevice>>
    val discoveredDevices: StateFlow<List<TvDevice>>
    val protocolEvents: StateFlow<List<ProtocolEvent>>
    val connectionDiagnostics: StateFlow<List<ConnectionDiagnostic>>
    val appLaunchDiagnostics: StateFlow<List<AppLaunchDiagnostic>>
    val volume: StateFlow<Int>
    val isVolumeReportedByTv: StateFlow<Boolean>
    val isMuted: StateFlow<Boolean>
    val currentApp: StateFlow<String>
    val currentInput: StateFlow<String>
    val isMockMode: StateFlow<Boolean>
    val favoriteAppIds: StateFlow<Set<String>>
    val appCatalog: StateFlow<List<TvApp>>
    val isAppDiscoveryRunning: StateFlow<Boolean>
    val language: StateFlow<String>
    val theme: StateFlow<String>
    val hapticsEnabled: StateFlow<Boolean>

    suspend fun startDiscovery()
    suspend fun connect(device: TvDevice)
    suspend fun pair(device: TvDevice, pinProvider: suspend () -> String): Result<String>
    suspend fun disconnect()
    suspend fun sendCommand(command: TvCommand): Result<Unit>
    suspend fun sendTouchpadDelta(dx: Float, dy: Float)
    suspend fun launchApp(app: TvApp): Result<Boolean>
    suspend fun launchCustomDeepLink(title: String, deepLinkUri: String): Result<Boolean>
    suspend fun refreshInstalledApps(): Result<Int>
    suspend fun switchInput(input: TvInput): Result<Boolean>
    suspend fun wakeOnLan(macAddress: String, ipAddress: String?): Result<Unit>
    suspend fun runDiagnostics(ip: String): TvDiagnostics
    suspend fun forgetDevice(ip: String)
    suspend fun toggleFavorite(appId: String)
    suspend fun setMockMode(enabled: Boolean)
    suspend fun setLanguage(lang: String)
    suspend fun setTheme(theme: String)
    suspend fun setHaptics(enabled: Boolean)
}

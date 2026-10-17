package com.yunusinan.vestelremote.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yunusinan.vestelremote.data.repository.TvRepository
import com.yunusinan.vestelremote.domain.model.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainUiState(
    val activeDevice: TvDevice? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected,
    val volume: Int = 20,
    val isVolumeReportedByTv: Boolean = false,
    val isUserDraggingVolume: Boolean = false,
    val isMuted: Boolean = false,
    val currentApp: String = "Ana Ekran",
    val currentInput: String = "Seçilmedi",
    val discoveredDevices: List<TvDevice> = emptyList(),
    val savedDevices: List<TvDevice> = emptyList(),
    val protocolEvents: List<ProtocolEvent> = emptyList(),
    val connectionDiagnostics: List<ConnectionDiagnostic> = emptyList(),
    val favoriteAppIds: Set<String> = setOf("youtube", "netflix", "prime_video", "disney_plus"),
    val appCatalog: List<TvApp> = TvApp.DEFAULT_CATALOG,
    val isAppDiscoveryRunning: Boolean = false,
    val language: String = "tr",
    val theme: String = "dark",
    val hapticsEnabled: Boolean = true,
    val isMockMode: Boolean = false,
    val selectedTab: Int = 0,
    val showDevicesScreen: Boolean = false,
    val pairingPromptDevice: TvDevice? = null,
    val activeDiagnostics: TvDiagnostics? = null
)

class MainViewModel(
    application: Application,
    private val repository: TvRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var currentPinDeferred: CompletableDeferred<String>? = null
    private var connectionJob: Job? = null

    val volumeController = VolumeSliderController(
        scope = viewModelScope,
        stepIntervalMs = 140L,
        maxStepsPerGesture = 15,
        sendVolumeCommand = { cmd ->
            repository.sendCommand(cmd)
        }
    )

    private fun startConnection(block: suspend () -> Unit) {
        if (connectionJob?.isActive == true) return
        connectionJob = viewModelScope.launch { block() }
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        viewModelScope.launch {
            volumeController.state.collect { volState ->
                _uiState.update {
                    it.copy(
                        volume = volState.displayVolume,
                        isVolumeReportedByTv = volState.isVolumeReportedByTv,
                        isUserDraggingVolume = volState.isUserDragging
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.activeDevice.collect { dev ->
                _uiState.update { it.copy(activeDevice = dev) }
            }
        }
        viewModelScope.launch {
            repository.connectionStatus.collect { status ->
                if (!status.isConnected) {
                    volumeController.onConnectionReset()
                }
                _uiState.update {
                    it.copy(
                        connectionStatus = status,
                        pairingPromptDevice = when (status) {
                            is ConnectionStatus.Failed,
                            is ConnectionStatus.Error,
                            ConnectionStatus.Disconnected -> null
                            else -> it.pairingPromptDevice
                        }
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.connectionDiagnostics.collect { diagnostics ->
                _uiState.update { it.copy(connectionDiagnostics = diagnostics) }
            }
        }
        viewModelScope.launch {
            combine(repository.volume, repository.isVolumeReportedByTv) { vol, reported ->
                vol to reported
            }.collect { (vol, reported) ->
                volumeController.onTvVolumeUpdated(vol, isFromRealTvOrMock = reported)
            }
        }
        viewModelScope.launch {
            repository.isMuted.collect { muted ->
                _uiState.update { it.copy(isMuted = muted) }
            }
        }
        viewModelScope.launch {
            repository.currentApp.collect { app ->
                _uiState.update { it.copy(currentApp = app) }
            }
        }
        viewModelScope.launch {
            repository.currentInput.collect { input ->
                _uiState.update { it.copy(currentInput = input) }
            }
        }
        viewModelScope.launch {
            repository.discoveredDevices.collect { disc ->
                _uiState.update { it.copy(discoveredDevices = disc) }
            }
        }
        viewModelScope.launch {
            repository.savedDevices.collect { saved ->
                _uiState.update { it.copy(savedDevices = saved) }
            }
        }
        viewModelScope.launch {
            repository.protocolEvents.collect { events ->
                _uiState.update { it.copy(protocolEvents = events) }
            }
        }
        viewModelScope.launch {
            repository.favoriteAppIds.collect { favs ->
                _uiState.update { it.copy(favoriteAppIds = favs) }
            }
        }
        viewModelScope.launch {
            repository.appCatalog.collect { apps ->
                _uiState.update { it.copy(appCatalog = apps) }
            }
        }
        viewModelScope.launch {
            repository.isAppDiscoveryRunning.collect { discovering ->
                _uiState.update { it.copy(isAppDiscoveryRunning = discovering) }
            }
        }
        viewModelScope.launch {
            repository.language.collect { lang ->
                _uiState.update { it.copy(language = lang) }
            }
        }
        viewModelScope.launch {
            repository.theme.collect { thm ->
                _uiState.update { it.copy(theme = thm) }
            }
        }
        viewModelScope.launch {
            repository.hapticsEnabled.collect { haptics ->
                _uiState.update { it.copy(hapticsEnabled = haptics) }
            }
        }
        viewModelScope.launch {
            repository.isMockMode.collect { mock ->
                _uiState.update { it.copy(isMockMode = mock) }
            }
        }
    }

    private fun triggerHaptic() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(18)
            }
        } catch (_: Exception) {}
    }

    fun onTabSelected(tabIndex: Int) {
        triggerHaptic()
        _uiState.update { it.copy(selectedTab = tabIndex, showDevicesScreen = false) }
        if (tabIndex == 2 && _uiState.value.connectionStatus.isConnected) {
            onRefreshInstalledApps()
        }
    }

    fun onOpenDevicesScreen() {
        triggerHaptic()
        _uiState.update { it.copy(showDevicesScreen = true) }
    }

    fun onCloseDevicesScreen() {
        triggerHaptic()
        _uiState.update { it.copy(showDevicesScreen = false) }
    }

    fun onLaunchAppById(appId: String) {
        val app = _uiState.value.appCatalog.firstOrNull { it.id == appId }
            ?: TvApp.DEFAULT_CATALOG.firstOrNull { it.id == appId }
        if (app != null) {
            onLaunchApp(app)
        }
    }

    fun onCommand(command: TvCommand) {
        triggerHaptic()
        if (command is TvCommand.VolumeUp || command is TvCommand.VolumeDown) {
            volumeController.cancelPendingCommands()
        }
        viewModelScope.launch {
            repository.sendCommand(command)
        }
    }

    fun onTouchpadDelta(dx: Float, dy: Float) {
        viewModelScope.launch {
            repository.sendTouchpadDelta(dx, dy)
        }
    }

    fun onVolumeChange(newVolume: Int) {
        volumeController.onSliderValueChanged(newVolume)
    }

    fun onVolumeChangeFinished() {
        volumeController.onSliderInteractionFinished()
    }

    fun onScanClick() {
        triggerHaptic()
        viewModelScope.launch {
            repository.startDiscovery()
        }
    }

    fun onConnectDevice(device: TvDevice) {
        triggerHaptic()
        val known = _uiState.value.savedDevices.firstOrNull { it.ipAddress == device.ipAddress }
        if (!_uiState.value.isMockMode && device.serverFingerprint == null && known?.serverFingerprint == null) {
            onPairDevice(device)
        } else startConnection {
            repository.connect(
                device.copy(
                    serverFingerprint = device.serverFingerprint ?: known?.serverFingerprint,
                    macAddress = device.macAddress ?: known?.macAddress
                )
            )
        }
    }

    fun onPairDevice(device: TvDevice) {
        triggerHaptic()
        startConnection {
            repository.pair(device) {
                withContext(Dispatchers.Main.immediate) {
                    val deferred = CompletableDeferred<String>()
                    currentPinDeferred = deferred
                    _uiState.update { it.copy(pairingPromptDevice = device) }
                    try {
                        deferred.await()
                    } finally {
                        currentPinDeferred = null
                        _uiState.update { it.copy(pairingPromptDevice = null) }
                    }
                }
            }
        }
    }

    fun onConfirmPairingPin(pin: String) {
        triggerHaptic()
        currentPinDeferred?.complete(pin)
        _uiState.update { it.copy(pairingPromptDevice = null) }
    }

    fun onDismissPairing() {
        val previous = connectionJob
        connectionJob = viewModelScope.launch {
            previous?.cancelAndJoin()
            currentPinDeferred = null
            _uiState.update { it.copy(pairingPromptDevice = null) }
            repository.disconnect()
        }
    }

    fun onManualConnect(ip: String, mac: String, name: String) {
        triggerHaptic()
        val device = TvDevice(
            id = "manual-$ip",
            name = if (name.isNotBlank()) name else "Vestel 55QG9960",
            model = "55QG9960",
            ipAddress = ip,
            macAddress = mac.takeIf { it.isNotBlank() }
        )
        onConnectDevice(device)
    }

    fun onForgetDevice(ip: String) {
        triggerHaptic()
        viewModelScope.launch {
            repository.forgetDevice(ip)
        }
    }

    fun onRunDiagnostics(ip: String) {
        triggerHaptic()
        viewModelScope.launch {
            val diag = repository.runDiagnostics(ip)
            _uiState.update { it.copy(activeDiagnostics = diag) }
        }
    }

    fun onDismissDiagnostics() {
        _uiState.update { it.copy(activeDiagnostics = null) }
    }

    fun onLaunchApp(app: TvApp) {
        triggerHaptic()
        viewModelScope.launch {
            repository.launchApp(app)
        }
    }

    fun onLaunchCustomDeepLink(title: String, deepLinkUri: String) {
        triggerHaptic()
        viewModelScope.launch {
            repository.launchCustomDeepLink(title, deepLinkUri)
        }
    }

    fun onRefreshInstalledApps() {
        viewModelScope.launch {
            repository.refreshInstalledApps()
        }
    }

    fun onSelectInput(input: TvInput) {
        triggerHaptic()
        viewModelScope.launch {
            repository.switchInput(input)
        }
    }

    fun onWakeOnLan() {
        triggerHaptic()
        val device = _uiState.value.activeDevice
        val mac = device?.macAddress ?: "AA:BB:CC:DD:EE:FF"
        viewModelScope.launch {
            repository.wakeOnLan(mac, device?.ipAddress)
        }
    }

    fun onToggleFavorite(appId: String) {
        triggerHaptic()
        viewModelScope.launch {
            repository.toggleFavorite(appId)
        }
    }

    fun onLanguageChange(lang: String) {
        triggerHaptic()
        viewModelScope.launch {
            repository.setLanguage(lang)
        }
    }

    fun onThemeChange(theme: String) {
        triggerHaptic()
        viewModelScope.launch {
            repository.setTheme(theme)
        }
    }

    fun onHapticsChange(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHaptics(enabled)
        }
    }

    fun onMockModeChange(enabled: Boolean) {
        triggerHaptic()
        val previous = connectionJob
        connectionJob = viewModelScope.launch {
            previous?.cancelAndJoin()
            repository.disconnect()
            repository.setMockMode(enabled)
        }
    }
}

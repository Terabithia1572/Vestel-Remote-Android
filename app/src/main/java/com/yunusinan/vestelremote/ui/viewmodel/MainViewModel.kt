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
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val activeDevice: TvDevice? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected,
    val volume: Int = 20,
    val isMuted: Boolean = false,
    val currentApp: String = "Vestel TV",
    val currentInput: String = "HDMI 1",
    val discoveredDevices: List<TvDevice> = emptyList(),
    val savedDevices: List<TvDevice> = emptyList(),
    val protocolEvents: List<ProtocolEvent> = emptyList(),
    val connectionDiagnostics: List<ConnectionDiagnostic> = emptyList(),
    val favoriteAppIds: Set<String> = setOf("youtube", "netflix", "prime_video", "disney_plus"),
    val language: String = "tr",
    val theme: String = "dark",
    val hapticsEnabled: Boolean = true,
    val isMockMode: Boolean = false,
    val selectedTab: Int = 0,
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
        // Collect reactive repository state
        viewModelScope.launch {
            repository.activeDevice.collect { dev ->
                _uiState.update { it.copy(activeDevice = dev) }
            }
        }
        viewModelScope.launch {
            repository.connectionStatus.collect { status ->
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
            repository.volume.collect { vol ->
                _uiState.update { it.copy(volume = vol) }
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
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun onCommand(command: TvCommand) {
        triggerHaptic()
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
        val current = _uiState.value.volume
        val diff = newVolume - current
        if (diff > 0) {
            onCommand(TvCommand.VolumeUp)
        } else if (diff < 0) {
            onCommand(TvCommand.VolumeDown)
        }
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
        if (! _uiState.value.isMockMode && device.serverFingerprint == null && known?.serverFingerprint == null) {
            onPairDevice(device)
        } else startConnection {
            repository.connect(device.copy(serverFingerprint = device.serverFingerprint ?: known?.serverFingerprint,
                macAddress = device.macAddress ?: known?.macAddress))
        }
    }

    fun onPairDevice(device: TvDevice) {
        triggerHaptic()
        startConnection {
            repository.pair(device) {
                withContext(Dispatchers.Main.immediate) {
                    // Reached only after ConfigurationAck. Display on the real TV still requires observation.
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

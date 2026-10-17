package com.yunusinan.vestelremote.data.repository

import android.content.Context
import com.yunusinan.vestelremote.data.crypto.CertificateHelper
import com.yunusinan.vestelremote.data.datastore.PreferencesManager
import com.yunusinan.vestelremote.data.protocol.*
import com.yunusinan.vestelremote.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.InetSocketAddress
import java.net.Socket

class TvRepositoryImpl(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : TvRepository {

    private val certHelper = CertificateHelper(context)
    private val prefs = PreferencesManager(context)
    private val discoveryManager = TvDiscoveryManager(context) { title, detail ->
        logEvent(ProtocolEvent.Direction.SYSTEM, title, detail)
    }

    private var remoteClient: AndroidTvRemoteV2Client? = null
    private var dialClient: DialClient? = null
    private var mockTvClient: MockTvClient? = null

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    override val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _activeDevice = MutableStateFlow<TvDevice?>(null)
    override val activeDevice: StateFlow<TvDevice?> = _activeDevice.asStateFlow()

    private val _savedDevices = MutableStateFlow<List<TvDevice>>(emptyList())
    override val savedDevices: StateFlow<List<TvDevice>> = _savedDevices.asStateFlow()

    override val discoveredDevices: StateFlow<List<TvDevice>> = discoveryManager.discoveredDevices

    private val _protocolEvents = MutableStateFlow<List<ProtocolEvent>>(emptyList())
    override val protocolEvents: StateFlow<List<ProtocolEvent>> = _protocolEvents.asStateFlow()

    private val _connectionDiagnostics = MutableStateFlow<List<ConnectionDiagnostic>>(emptyList())
    override val connectionDiagnostics = _connectionDiagnostics.asStateFlow()

    private val _appLaunchDiagnostics = MutableStateFlow<List<AppLaunchDiagnostic>>(emptyList())
    override val appLaunchDiagnostics: StateFlow<List<AppLaunchDiagnostic>> = _appLaunchDiagnostics.asStateFlow()

    private fun recordLaunchDiagnostic(entry: AppLaunchDiagnostic) {
        _appLaunchDiagnostics.update { entries ->
            (entries + entry).takeLast(25)
        }
    }

    private fun beginAttempt(device: TvDevice, pairing: Boolean): AttemptDiagnostics = AttemptDiagnostics(
        ConnectionDiagnostic(host = device.ipAddress, port = if (pairing) device.pairingPort else device.port,
            service = if (pairing) "pairing" else "remote")
    ) { entry ->
        _connectionDiagnostics.update { entries ->
            val index = entries.indexOfFirst { it.id == entry.id }
            if (index < 0) (entries + entry).takeLast(20)
            else entries.toMutableList().apply { set(index, entry) }
        }
    }

    private val _volume = MutableStateFlow(20)
    override val volume: StateFlow<Int> = _volume.asStateFlow()

    private val _isVolumeReportedByTv = MutableStateFlow(false)
    override val isVolumeReportedByTv: StateFlow<Boolean> = _isVolumeReportedByTv.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    override val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _currentApp = MutableStateFlow("Ana Ekran")
    override val currentApp: StateFlow<String> = _currentApp.asStateFlow()

    private val _currentInput = MutableStateFlow("Seçilmedi")
    override val currentInput: StateFlow<String> = _currentInput.asStateFlow()

    private val _isMockMode = MutableStateFlow(false)
    override val isMockMode: StateFlow<Boolean> = _isMockMode.asStateFlow()

    private val _favoriteAppIds = MutableStateFlow<Set<String>>(setOf("youtube", "netflix", "prime_video", "disney_plus"))
    override val favoriteAppIds: StateFlow<Set<String>> = _favoriteAppIds.asStateFlow()

    private val _appCatalog = MutableStateFlow<List<TvApp>>(TvApp.DEFAULT_CATALOG)
    override val appCatalog: StateFlow<List<TvApp>> = _appCatalog.asStateFlow()

    private val _isAppDiscoveryRunning = MutableStateFlow(false)
    override val isAppDiscoveryRunning: StateFlow<Boolean> = _isAppDiscoveryRunning.asStateFlow()

    private val _language = MutableStateFlow("tr")
    override val language: StateFlow<String> = _language.asStateFlow()

    private val _theme = MutableStateFlow("dark")
    override val theme: StateFlow<String> = _theme.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    override val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    init {
        // Collect preferences
        scope.launch {
            prefs.savedDevicesFlow.collect { devices ->
                _savedDevices.value = devices
            }
        }
        scope.launch {
            prefs.favoriteAppsFlow.collect { _favoriteAppIds.value = it }
        }
        scope.launch {
            prefs.languageFlow.collect { _language.value = it }
        }
        scope.launch {
            prefs.themeFlow.collect { _theme.value = it }
        }
        scope.launch {
            prefs.hapticsFlow.collect { _hapticsEnabled.value = it }
        }
        scope.launch {
            prefs.mockModeFlow.collect { enabled ->
                _isMockMode.value = enabled
                if (enabled && mockTvClient == null) {
                    mockTvClient = MockTvClient { t, d -> logEvent(ProtocolEvent.Direction.SYSTEM, t, d) }
                }
            }
        }

        logEvent(ProtocolEvent.Direction.SYSTEM, "Uygulama Başlatıldı", "Vestel Remote v1.0.0 hazır")
    }

    private fun logEvent(direction: ProtocolEvent.Direction, title: String, detail: String) {
        val event = ProtocolEvent(direction = direction, title = title, detail = detail)
        _protocolEvents.update { (listOf(event) + it).take(150) }
    }

    override suspend fun startDiscovery() {
        val previous = _connectionStatus.value
        if (previous.isConnecting || previous.isPairing || previous is ConnectionStatus.Scanning) return
        if (!_connectionStatus.compareAndSet(previous, ConnectionStatus.Scanning)) return
        try {
            discoveryManager.startDiscovery(5000)
        } finally {
            _connectionStatus.compareAndSet(ConnectionStatus.Scanning, previous)
        }
    }

    override suspend fun connect(device: TvDevice) {
        if (_isMockMode.value) {
            val mock = mockTvClient ?: MockTvClient().also { mockTvClient = it }
            _connectionStatus.value = ConnectionStatus.Connecting(device.ipAddress)
            val res = mock.connect()
            if (res.isSuccess) {
                _activeDevice.value = mock.mockDevice
                _connectionStatus.value = ConnectionStatus.Connected(mock.mockDevice)
                _volume.value = mock.volumeLevel
                _isVolumeReportedByTv.value = true
                _isMuted.value = mock.isMuted
                _currentApp.value = mock.currentApp
                _currentInput.value = mock.currentInput
                scope.launch { refreshInstalledApps() }
            }
            return
        }

        _connectionStatus.value = ConnectionStatus.Connecting(device.ipAddress)
        logEvent(ProtocolEvent.Direction.OUTGOING, "Bağlantı İsteği", "${device.ipAddress}:6466")

        val diag = beginAttempt(device, pairing = false)
        try {
            remoteClient?.disconnect()
            val known = prefs.savedDevicesFlow.first().firstOrNull { it.ipAddress == device.ipAddress }
            val fingerprint = device.serverFingerprint ?: known?.serverFingerprint
            if (fingerprint == null) throw ProtocolFailure("Kayıtlı TV sertifikası yok; önce Eşleştir seçeneğini kullanın")
            val client = AndroidTvRemoteV2Client(
                host = device.ipAddress,
                keyCertPair = withContext(Dispatchers.IO) { certHelper.getOrCreateClientCertificate() },
                pinnedServerFingerprint = fingerprint,
                pairPort = device.pairingPort,
                remotePort = device.port,
                diagnostics = diag,
                onVolume = { level, maximum, muted ->
                    if (maximum > 0) {
                        _volume.value = (level * 100 / maximum).coerceIn(0, 100)
                        _isVolumeReportedByTv.value = true
                    }
                    _isMuted.value = muted
                },
                onStateChanged = { status -> _connectionStatus.value = status },
                onEventLog = { t, d -> logEvent(ProtocolEvent.Direction.SYSTEM, t, d) }
            )
            remoteClient = client
            dialClient = DialClient(device.ipAddress) { t, d ->
                logEvent(ProtocolEvent.Direction.SYSTEM, t, d)
            }

            val connectResult = client.connect(device)
            if (connectResult.isSuccess) {
                val updated = device.copy(isOnline = true, isPaired = true, serverFingerprint = fingerprint)
                _activeDevice.value = updated
                _connectionStatus.compareAndSet(ConnectionStatus.Connected(device), ConnectionStatus.Connected(updated))
                prefs.saveDevice(updated)
                logEvent(ProtocolEvent.Direction.INCOMING, "Bağlantı Başarılı", "${device.displayTitle} bağlandı")
                scope.launch { refreshInstalledApps() }
            } else {
                val err = safeConnectionError(connectResult.exceptionOrNull() ?: Exception())
                _connectionStatus.value = ConnectionStatus.Failed(err)
            }
        } catch (e: Exception) {
            diag.failed(e)
            if (e is CancellationException) throw e
            _connectionStatus.value = ConnectionStatus.Failed(safeConnectionError(e))
            logEvent(ProtocolEvent.Direction.ERROR, "Bağlantı Hatası", safeConnectionError(e))
        }
    }

    override suspend fun pair(device: TvDevice, pinProvider: suspend () -> String): Result<String> {
        if (_isMockMode.value) {
            val mock = mockTvClient ?: MockTvClient().also { mockTvClient = it }
            _connectionStatus.value = ConnectionStatus.PairingInProgress
            val res = mock.pair(pinProvider)
            if (res.isSuccess) {
                val paired = mock.mockDevice.copy(isPaired = true)
                _activeDevice.value = paired
                connect(paired)
            } else {
                _connectionStatus.value = ConnectionStatus.Failed(res.exceptionOrNull()?.message ?: "Eşleştirme hatası")
            }
            return res
        }

        _connectionStatus.value = ConnectionStatus.ConnectingToPairingService(device.ipAddress)
        logEvent(ProtocolEvent.Direction.OUTGOING, "Eşleştirme Başlatıldı", "${device.ipAddress}:6467 TLS")

        val diag = beginAttempt(device, pairing = true)
        try {
            remoteClient?.disconnect()
            val client = AndroidTvRemoteV2Client(
                host = device.ipAddress,
                keyCertPair = withContext(Dispatchers.IO) { certHelper.getOrCreateClientCertificate(forPairing = true) },
                pinnedServerFingerprint = null,
                pairPort = device.pairingPort,
                remotePort = device.port,
                diagnostics = diag,
                onStateChanged = { status -> _connectionStatus.value = status },
                onEventLog = { t, d -> logEvent(ProtocolEvent.Direction.SYSTEM, t, d) }
            )

            remoteClient = client
            val pairResult = client.pair(device, pinProvider)
            return if (pairResult.isSuccess) {
                val fingerprint = pairResult.getOrNull()
                logEvent(ProtocolEvent.Direction.INCOMING, "Eşleştirme Tamamlandı", "Parmak izi: $fingerprint")
                val updated = device.copy(isPaired = true, isOnline = true, serverFingerprint = fingerprint)
                prefs.saveDevice(updated)
                connect(updated)
                Result.success(fingerprint ?: "OK")
            } else {
                val msg = safeConnectionError(pairResult.exceptionOrNull() ?: Exception())
                _connectionStatus.value = ConnectionStatus.Failed(msg)
                Result.failure(pairResult.exceptionOrNull() ?: Exception(msg))
            }
        } catch (e: Exception) {
            diag.failed(e)
            if (e is CancellationException) throw e
            _connectionStatus.value = ConnectionStatus.Failed(safeConnectionError(e))
            return Result.failure(e)
        }
    }

    override suspend fun disconnect() {
        if (_isMockMode.value) {
            mockTvClient?.disconnect()
        } else {
            remoteClient?.disconnect()
        }
        _isVolumeReportedByTv.value = false
        _connectionStatus.value = ConnectionStatus.Disconnected
        _appCatalog.value = TvApp.DEFAULT_CATALOG
        logEvent(ProtocolEvent.Direction.SYSTEM, "Bağlantı Kesildi", "Kullanıcı isteğiyle kapatıldı")
    }

    override suspend fun sendCommand(command: TvCommand): Result<Unit> {
        logEvent(ProtocolEvent.Direction.OUTGOING, "Komut Gönderildi", "${command.name} (keyCode: ${command.androidKeyCode})")

        if (_isMockMode.value) {
            val res = mockTvClient?.sendCommand(command) ?: Result.failure(IllegalStateException("Mock TV hazır değil"))
            if (res.isSuccess) {
                mockTvClient?.let {
                    _volume.value = it.volumeLevel
                    _isVolumeReportedByTv.value = true
                    _isMuted.value = it.isMuted
                    _currentApp.value = it.currentApp
                }
            }
            return res
        }

        val client = remoteClient
        return if (client != null && client.isConnected) {
            client.sendCommand(command)
        } else {
            Result.failure(IllegalStateException("Televizyon bağlı değil"))
        }
    }

    override suspend fun sendTouchpadDelta(dx: Float, dy: Float) {
        if (_isMockMode.value) {
            val cmd = when {
                dy < -30 -> TvCommand.DPadUp
                dy > 30 -> TvCommand.DPadDown
                dx < -30 -> TvCommand.DPadLeft
                dx > 30 -> TvCommand.DPadRight
                else -> null
            }
            if (cmd != null) sendCommand(cmd)
            return
        }

        remoteClient?.sendTouchpadDirection(dx, dy)
    }

    override suspend fun launchApp(app: TvApp): Result<Boolean> {
        val client = remoteClient
        val dial = dialClient
        val diagnostic = AppLauncherEngine.executeLaunch(
            app = app,
            isMockMode = _isMockMode.value,
            isRemoteConnected = client != null && client.isConnected,
            sendRemoteAppLink = { uri ->
                client?.sendDeepLink(uri) ?: Result.failure(ProtocolFailure("Televizyona bağlı değil"))
            },
            launchViaDial = if (dial != null) {
                { dialName -> dial.launchApp(dialName) }
            } else null,
            verifyDialRunning = if (dial != null) {
                { dialName -> dial.isAppRunning(dialName) }
            } else null,
            launchViaMock = { targetApp ->
                mockTvClient?.launchApp(targetApp) ?: Result.success(true)
            }
        )
        recordLaunchDiagnostic(diagnostic)

        return when (diagnostic.dispatchResult) {
            AppLaunchDispatchStatus.APPLICATION_VISIBLY_OPENED -> {
                _currentApp.value = app.name
                logEvent(
                    ProtocolEvent.Direction.INCOMING,
                    "Uygulama Açık Doğrulandı",
                    "${app.name} (${app.packageName}) -> ${diagnostic.protocolAcknowledgment}"
                )
                scope.launch { refreshInstalledApps() }
                Result.success(true)
            }
            AppLaunchDispatchStatus.LAUNCH_ACCEPTED -> {
                _currentApp.value = app.name
                logEvent(
                    ProtocolEvent.Direction.INCOMING,
                    "Başlatma İsteği Kabul Edildi",
                    "${app.name} (${diagnostic.attemptedSchemeOrEndpoint}) -> ${diagnostic.protocolAcknowledgment}"
                )
                scope.launch { refreshInstalledApps() }
                Result.success(true)
            }
            AppLaunchDispatchStatus.COMMAND_SENT -> {
                logEvent(
                    ProtocolEvent.Direction.OUTGOING,
                    "Başlatma Komutu Gönderildi",
                    "${app.name} (${app.packageName}) -> ${diagnostic.attemptedSchemeOrEndpoint}"
                )
                Result.success(true)
            }
            AppLaunchDispatchStatus.UNSUPPORTED_CAPABILITY -> {
                logEvent(
                    ProtocolEvent.Direction.ERROR,
                    "Doğrudan Başlatma Desteklenmiyor",
                    diagnostic.userFeedbackTr
                )
                Result.failure(UnsupportedAppLaunchException(diagnostic))
            }
            AppLaunchDispatchStatus.DISPATCH_FAILED -> {
                logEvent(
                    ProtocolEvent.Direction.ERROR,
                    "Uygulama Başlatma Hatası",
                    diagnostic.userFeedbackTr
                )
                Result.failure(AppLaunchDispatchException(diagnostic))
            }
        }
    }

    override suspend fun launchCustomDeepLink(title: String, deepLinkUri: String): Result<Boolean> {
        val cleanUri = deepLinkUri.trim()
        val displayTitle = title.trim().ifBlank { "Özel URI" }
        val syntheticApp = TvApp(
            id = "custom_uri",
            name = displayTitle,
            packageName = "custom.deeplink",
            deepLinkUri = cleanUri,
            launchStrategy = AppLaunchStrategy.REMOTE_APP_LINK
        )
        return launchApp(syntheticApp)
    }

    override suspend fun refreshInstalledApps(): Result<Int> {
        if (_isAppDiscoveryRunning.value) return Result.success(0)
        _isAppDiscoveryRunning.value = true
        return try {
            if (_isMockMode.value) {
                delay(250)
                val activeMockApp = mockTvClient?.currentApp ?: ""
                val mockVerifiedIds = setOf("youtube", "netflix", "prime_video", "disney_plus", "spotify", "tabii")
                val updated = TvApp.DEFAULT_CATALOG.map { app ->
                    val state = when {
                        app.name.equals(activeMockApp, ignoreCase = true) -> AppDiscoveryState.VERIFIED_RUNNING
                        mockVerifiedIds.contains(app.id) -> AppDiscoveryState.VERIFIED_STOPPED
                        app.isUserConfirmedInstalled -> AppDiscoveryState.USER_CONFIRMED_INSTALLED
                        else -> AppDiscoveryState.UNVERIFIED_SHORTCUT
                    }
                    app.copy(discoveryState = state)
                }
                _appCatalog.value = updated
                val count = updated.count { it.isVerifiedOnTv }
                logEvent(ProtocolEvent.Direction.INCOMING, "DIAL Uygulama Keşfi (Sanal)", "$count uygulama doğrulandı")
                return Result.success(count)
            }

            val hostIp = _activeDevice.value?.ipAddress
            if (hostIp.isNullOrBlank()) {
                _appCatalog.value = TvApp.DEFAULT_CATALOG
                return Result.failure(IllegalStateException("Aktif televizyon IP adresi bulunamadı"))
            }

            val client = dialClient ?: DialClient(hostIp) { t, d ->
                logEvent(ProtocolEvent.Direction.SYSTEM, t, d)
            }.also { dialClient = it }

            val results = client.probeCatalogApps(TvApp.DEFAULT_CATALOG)
            val updated = TvApp.DEFAULT_CATALOG.map { app ->
                val info = results[app.id]
                val resolvedState = when {
                    info != null && info.isVerifiedOnTv -> info.state
                    app.isUserConfirmedInstalled -> AppDiscoveryState.USER_CONFIRMED_INSTALLED
                    info != null -> info.state
                    else -> AppDiscoveryState.UNVERIFIED_SHORTCUT
                }
                app.copy(discoveryState = resolvedState)
            }
            _appCatalog.value = updated

            // Update active running app name if DIAL reports an app is currently running
            updated.firstOrNull { it.discoveryState == AppDiscoveryState.VERIFIED_RUNNING }?.let { runningApp ->
                _currentApp.value = runningApp.name
            }

            val verifiedCount = updated.count { it.isVerifiedOnTv }
            logEvent(
                ProtocolEvent.Direction.INCOMING,
                "DIAL Uygulama Keşfi",
                "TV üzerinde $verifiedCount DIAL uygulaması doğrulandı"
            )
            Result.success(verifiedCount)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        } finally {
            _isAppDiscoveryRunning.value = false
        }
    }

    override suspend fun switchInput(input: TvInput): Result<Boolean> {
        logEvent(ProtocolEvent.Direction.OUTGOING, "Giriş Değiştir", input.name)
        _currentInput.value = input.name
        _currentApp.value = input.name

        if (_isMockMode.value) {
            return mockTvClient?.switchInput(input) ?: Result.success(true)
        }

        val cmd = when (input.id) {
            "hdmi_1" -> TvCommand.Input
            "hdmi_2" -> TvCommand.Input
            "hdmi_3" -> TvCommand.Input
            "live_tv" -> TvCommand.Input
            else -> TvCommand.Input
        }
        return sendCommand(cmd).map { true }
    }

    override suspend fun wakeOnLan(macAddress: String, ipAddress: String?): Result<Unit> {
        logEvent(ProtocolEvent.Direction.OUTGOING, "Wake-on-LAN", "MAC: $macAddress, Hedef: ${ipAddress ?: "Broadcast"}")
        return WakeOnLan.wake(macAddress, ipAddress)
    }

    override suspend fun runDiagnostics(ip: String): TvDiagnostics = withContext(Dispatchers.IO) {
        logEvent(ProtocolEvent.Direction.SYSTEM, "Teşhis Başlatıldı", "Hedef IP: $ip")
        val startTime = System.currentTimeMillis()

        var pingSuccess = false
        var latency: Long = -1
        try {
            val reachable = java.net.InetAddress.getByName(ip).isReachable(2000)
            latency = System.currentTimeMillis() - startTime
            pingSuccess = reachable
        } catch (_: Exception) {}

        val checkPort = { port: Int ->
            try {
                Socket().use { s ->
                    s.connect(InetSocketAddress(ip, port), 800)
                    true
                }
            } catch (_: Exception) {
                false
            }
        }

        val p6466 = checkPort(6466)
        val p6467 = checkPort(6467)
        val p8008 = checkPort(8008)
        val p8009 = checkPort(8009)
        val p5555 = checkPort(5555)

        val summary = buildString {
            if (p6466 && p6467) append("Android TV Remote v2 Servisi Aktif. ")
            if (p8008) append("DIAL Servisi Aktif. ")
            if (p8009) append("Google Cast Aktif. ")
            if (p5555) append("ADB Hata Ayıklama Açık. ")
            if (!p6466 && !p6467 && !p8008) append("Televizyon bekleme modunda veya ağ izolasyonu var.")
        }

        TvDiagnostics(
            targetIp = ip,
            pingSuccess = pingSuccess,
            pingLatencyMs = latency,
            remotePort6466Open = p6466,
            pairingPort6467Open = p6467,
            dialPort8008Open = p8008,
            castPort8009Open = p8009,
            adbPort5555Open = p5555,
            tlsHandshakeSuccess = null, // TCP probes do not perform TLS.
            dialServiceFound = p8008,
            summary = summary
        )
    }

    override suspend fun forgetDevice(ip: String) {
        prefs.removeDevice(ip)
        if (_activeDevice.value?.ipAddress == ip) {
            disconnect()
            _activeDevice.value = null
        }
    }

    override suspend fun toggleFavorite(appId: String) {
        prefs.toggleFavoriteApp(appId)
    }

    override suspend fun setMockMode(enabled: Boolean) {
        prefs.setMockMode(enabled)
    }

    override suspend fun setLanguage(lang: String) {
        prefs.setLanguage(lang)
    }

    override suspend fun setTheme(theme: String) {
        prefs.setTheme(theme)
    }

    override suspend fun setHaptics(enabled: Boolean) {
        prefs.setHaptics(enabled)
    }
}

package com.yunusinan.vestelremote

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yunusinan.vestelremote.ui.components.DiagnosticsDialog
import com.yunusinan.vestelremote.ui.components.PairingDialog
import com.yunusinan.vestelremote.ui.components.VestelBottomNavigationBar
import com.yunusinan.vestelremote.ui.screens.*
import com.yunusinan.vestelremote.ui.theme.*
import com.yunusinan.vestelremote.ui.viewmodel.MainViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as VestelRemoteApp
                return MainViewModel(app, app.repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val baseContext = LocalContext.current
            val baseConfiguration = LocalConfiguration.current

            val (localizedContext, localizedConfig) = remember(uiState.language, baseConfiguration) {
                val locale = Locale(uiState.language.ifBlank { "tr" })
                Locale.setDefault(locale)
                val config = Configuration(baseConfiguration).apply {
                    setLocale(locale)
                }
                baseContext.createConfigurationContext(config) to config
            }

            BackHandler(enabled = uiState.showDevicesScreen) {
                viewModel.onCloseDevicesScreen()
            }

            BackHandler(enabled = !uiState.showDevicesScreen && uiState.selectedTab != 0) {
                viewModel.onTabSelected(0)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfig
            ) {
                VestelRemoteTheme(themePreference = uiState.theme) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        bottomBar = {
                            VestelBottomNavigationBar(
                                selectedTab = uiState.selectedTab,
                                onTabSelected = viewModel::onTabSelected
                            )
                        }
                    ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (uiState.showDevicesScreen) {
                            DevicesScreen(
                                connectionDiagnostics = uiState.connectionDiagnostics,
                                isMockMode = uiState.isMockMode,
                                discoveredDevices = uiState.discoveredDevices,
                                savedDevices = uiState.savedDevices,
                                activeDevice = uiState.activeDevice,
                                connectionStatus = uiState.connectionStatus,
                                onScanClick = viewModel::onScanClick,
                                onConnectDevice = viewModel::onConnectDevice,
                                onPairDevice = viewModel::onPairDevice,
                                onForgetDevice = viewModel::onForgetDevice,
                                onRunDiagnostics = viewModel::onRunDiagnostics,
                                onManualConnect = viewModel::onManualConnect,
                                onBackClick = viewModel::onCloseDevicesScreen
                            )
                        } else {
                            when (uiState.selectedTab) {
                                0 -> DashboardScreen(
                                    device = uiState.activeDevice,
                                    connectionStatus = uiState.connectionStatus,
                                    volume = uiState.volume,
                                    isVolumeReportedByTv = uiState.isVolumeReportedByTv,
                                    isMuted = uiState.isMuted,
                                    currentApp = uiState.currentApp,
                                    currentInput = uiState.currentInput,
                                    recentEvents = uiState.protocolEvents,
                                    onCommand = viewModel::onCommand,
                                    onVolumeChange = viewModel::onVolumeChange,
                                    onVolumeChangeFinished = viewModel::onVolumeChangeFinished,
                                    onWakeOnLan = viewModel::onWakeOnLan,
                                    onNavigateToDevices = viewModel::onOpenDevicesScreen,
                                    onNavigateToInputs = { viewModel.onTabSelected(3) }
                                )
                                1 -> RemoteScreen(
                                    onCommand = viewModel::onCommand,
                                    onTouchpadDelta = viewModel::onTouchpadDelta,
                                    onLaunchAppById = viewModel::onLaunchAppById,
                                    onNavigateToSettings = { viewModel.onTabSelected(4) }
                                )
                                2 -> AppsScreen(
                                    apps = uiState.appCatalog,
                                    favoriteAppIds = uiState.favoriteAppIds,
                                    connectionStatus = uiState.connectionStatus,
                                    isAppDiscoveryRunning = uiState.isAppDiscoveryRunning,
                                    onLaunchApp = viewModel::onLaunchApp,
                                    onToggleFavorite = viewModel::onToggleFavorite,
                                    onRefreshInstalledApps = viewModel::onRefreshInstalledApps,
                                    onLaunchCustomDeepLink = viewModel::onLaunchCustomDeepLink
                                )
                                3 -> InputsScreen(
                                    currentInputName = uiState.currentInput,
                                    onSelectInput = viewModel::onSelectInput
                                )
                                4 -> SettingsScreen(
                                    activeDevice = uiState.activeDevice,
                                    connectionDiagnostics = uiState.connectionDiagnostics,
                                    currentLanguage = uiState.language,
                                    currentTheme = uiState.theme,
                                    hapticsEnabled = uiState.hapticsEnabled,
                                    isMockMode = uiState.isMockMode,
                                    protocolEvents = uiState.protocolEvents,
                                    onLanguageChange = viewModel::onLanguageChange,
                                    onThemeChange = viewModel::onThemeChange,
                                    onHapticsChange = viewModel::onHapticsChange,
                                    onMockModeChange = viewModel::onMockModeChange,
                                    onNavigateToDevices = viewModel::onOpenDevicesScreen,
                                    onRescanDevices = {
                                        viewModel.onScanClick()
                                        viewModel.onOpenDevicesScreen()
                                    },
                                    onResetPairing = {
                                        uiState.activeDevice?.ipAddress?.let { viewModel.onForgetDevice(it) }
                                    }
                                )
                            }
                        }

                        // Modal Pairing Code Dialog
                        uiState.pairingPromptDevice?.let { dev ->
                            PairingDialog(
                                tvName = dev.displayTitle,
                                onConfirm = { pin -> viewModel.onConfirmPairingPin(pin) },
                                onDismiss = { viewModel.onDismissPairing() }
                            )
                        }

                        // Modal Diagnostics Dialog
                        uiState.activeDiagnostics?.let { diag ->
                            DiagnosticsDialog(
                                diagnostics = diag,
                                onDismiss = { viewModel.onDismissDiagnostics() }
                            )
                        }
                    }
                }
            }
            }
        }
    }
}


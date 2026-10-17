package com.yunusinan.vestelremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yunusinan.vestelremote.domain.model.TvCommand
import com.yunusinan.vestelremote.ui.components.DiagnosticsDialog
import com.yunusinan.vestelremote.ui.components.PairingDialog
import com.yunusinan.vestelremote.ui.screens.*
import com.yunusinan.vestelremote.ui.theme.*
import com.yunusinan.vestelremote.ui.viewmodel.MainViewModel

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

            VestelRemoteTheme(themePreference = uiState.theme) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    bottomBar = {
                        NavigationBar(
                            containerColor = DarkSurfaceElevated,
                            tonalElevation = 6.dp
                        ) {
                            NavigationBarItem(
                                selected = uiState.selectedTab == 0,
                                onClick = { viewModel.onTabSelected(0) },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                                label = { Text(stringResource(R.string.nav_dashboard), fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VestelRed,
                                    selectedTextColor = VestelRed,
                                    indicatorColor = VestelRed.copy(alpha = 0.15f)
                                )
                            )
                            NavigationBarItem(
                                selected = uiState.selectedTab == 1,
                                onClick = { viewModel.onTabSelected(1) },
                                icon = { Icon(Icons.Default.SettingsRemote, contentDescription = null) },
                                label = { Text(stringResource(R.string.nav_remote), fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VestelRed,
                                    selectedTextColor = VestelRed,
                                    indicatorColor = VestelRed.copy(alpha = 0.15f)
                                )
                            )
                            NavigationBarItem(
                                selected = uiState.selectedTab == 2,
                                onClick = { viewModel.onTabSelected(2) },
                                icon = { Icon(Icons.Default.Apps, contentDescription = null) },
                                label = { Text(stringResource(R.string.nav_apps), fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VestelRed,
                                    selectedTextColor = VestelRed,
                                    indicatorColor = VestelRed.copy(alpha = 0.15f)
                                )
                            )
                            NavigationBarItem(
                                selected = uiState.selectedTab == 3,
                                onClick = { viewModel.onTabSelected(3) },
                                icon = { Icon(Icons.Default.Input, contentDescription = null) },
                                label = { Text(stringResource(R.string.nav_inputs), fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VestelRed,
                                    selectedTextColor = VestelRed,
                                    indicatorColor = VestelRed.copy(alpha = 0.15f)
                                )
                            )
                            NavigationBarItem(
                                selected = uiState.selectedTab == 4,
                                onClick = { viewModel.onTabSelected(4) },
                                icon = { Icon(Icons.Default.Devices, contentDescription = null) },
                                label = { Text(stringResource(R.string.nav_devices), fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VestelRed,
                                    selectedTextColor = VestelRed,
                                    indicatorColor = VestelRed.copy(alpha = 0.15f)
                                )
                            )
                            NavigationBarItem(
                                selected = uiState.selectedTab == 5,
                                onClick = { viewModel.onTabSelected(5) },
                                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                label = { Text(stringResource(R.string.nav_settings), fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VestelRed,
                                    selectedTextColor = VestelRed,
                                    indicatorColor = VestelRed.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.selectedTab) {
                            0 -> DashboardScreen(
                                device = uiState.activeDevice,
                                connectionStatus = uiState.connectionStatus,
                                volume = uiState.volume,
                                isMuted = uiState.isMuted,
                                currentApp = uiState.currentApp,
                                currentInput = uiState.currentInput,
                                recentEvents = uiState.protocolEvents,
                                onCommand = viewModel::onCommand,
                                onVolumeChange = viewModel::onVolumeChange,
                                onWakeOnLan = viewModel::onWakeOnLan,
                                onNavigateToDevices = { viewModel.onTabSelected(4) }
                            )
                            1 -> RemoteScreen(
                                onCommand = viewModel::onCommand,
                                onTouchpadDelta = viewModel::onTouchpadDelta
                            )
                            2 -> AppsScreen(
                                favoriteAppIds = uiState.favoriteAppIds,
                                onLaunchApp = viewModel::onLaunchApp,
                                onToggleFavorite = viewModel::onToggleFavorite
                            )
                            3 -> InputsScreen(
                                currentInputName = uiState.currentInput,
                                onSelectInput = viewModel::onSelectInput
                            )
                            4 -> DevicesScreen(
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
                                onManualConnect = viewModel::onManualConnect
                            )
                            5 -> SettingsScreen(
                                connectionDiagnostics = uiState.connectionDiagnostics,
                                currentLanguage = uiState.language,
                                currentTheme = uiState.theme,
                                hapticsEnabled = uiState.hapticsEnabled,
                                isMockMode = uiState.isMockMode,
                                protocolEvents = uiState.protocolEvents,
                                onLanguageChange = viewModel::onLanguageChange,
                                onThemeChange = viewModel::onThemeChange,
                                onHapticsChange = viewModel::onHapticsChange,
                                onMockModeChange = viewModel::onMockModeChange
                            )
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

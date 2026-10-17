package com.yunusinan.vestelremote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.ConnectionStatus
import com.yunusinan.vestelremote.domain.model.TvDevice
import com.yunusinan.vestelremote.domain.model.ConnectionDiagnostic
import com.yunusinan.vestelremote.ui.components.CopyDiagnosticsButton
import com.yunusinan.vestelremote.ui.theme.*

@Composable
fun DevicesScreen(
    discoveredDevices: List<TvDevice>,
    savedDevices: List<TvDevice>,
    activeDevice: TvDevice?,
    connectionStatus: ConnectionStatus,
    connectionDiagnostics: List<ConnectionDiagnostic>,
    isMockMode: Boolean,
    onScanClick: () -> Unit,
    onConnectDevice: (TvDevice) -> Unit,
    onPairDevice: (TvDevice) -> Unit,
    onForgetDevice: (String) -> Unit,
    onRunDiagnostics: (String) -> Unit,
    onManualConnect: (ip: String, mac: String, name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var manualIp by remember { mutableStateOf("") }
    var manualMac by remember { mutableStateOf("") }
    var manualName by remember { mutableStateOf("Vestel 55QG9960") }
    val isScanning = connectionStatus is ConnectionStatus.Scanning
    val busy = connectionStatus.isConnecting || connectionStatus.isPairing

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Scan Trigger
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.devices_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onScanClick,
                    enabled = !isScanning && !busy,
                    colors = ButtonDefaults.buttonColors(containerColor = VestelRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.scanning), fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.scan_network), fontSize = 12.sp)
                    }
                }
            }
        }

        // Discovered Devices Section
        item {
            Column {
                Text(when (connectionStatus) {
                    is ConnectionStatus.Failed -> connectionStatus.message
                    is ConnectionStatus.ConnectingToPairingService -> "Eşleştirme servisine bağlanılıyor…"
                    is ConnectionStatus.TlsEstablished -> "TLS bağlantısı kuruldu"
                    is ConnectionStatus.PairingNegotiation -> "Eşleştirme: ${connectionStatus.stage}"
                    is ConnectionStatus.VerifyingPin -> "Kod doğrulanıyor…"
                    is ConnectionStatus.ConnectingToRemoteService -> "Kumanda oturumu başlatılıyor…"
                    is ConnectionStatus.Connected -> "Kumanda bağlantısı hazır"
                    else -> "İlk kullanımda Eşleştir seçeneğini kullanın."
                }, color = if (connectionStatus is ConnectionStatus.Failed) StatusRed else DarkTextSecondary)
                CopyDiagnosticsButton(connectionDiagnostics, isMockMode)
            }
        }

        if (discoveredDevices.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.discovered_devices),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(discoveredDevices, key = { "disc-${it.id}" }) { device ->
                DeviceItemCard(
                    device = device,
                    isActive = connectionStatus.isConnected && activeDevice?.ipAddress == device.ipAddress,
                    enabled = !busy,
                    onConnect = { onConnectDevice(device) },
                    onPair = { onPairDevice(device) },
                    onDiagnostics = { onRunDiagnostics(device.ipAddress) },
                    onForget = null
                )
            }
        }

        // Saved Devices Section
        item {
            Text(
                text = stringResource(R.string.saved_devices),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (savedDevices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Text(
                        text = "Henüz kayıtlı televizyon yok. Aşağıdan manuel IP girebilir veya yukarıdaki butona basarak ağınızı tarayabilirsiniz.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(savedDevices, key = { "saved-${it.id}" }) { device ->
                DeviceItemCard(
                    device = device,
                    isActive = connectionStatus.isConnected && activeDevice?.ipAddress == device.ipAddress,
                    enabled = !busy,
                    onConnect = { onConnectDevice(device) },
                    onPair = { onPairDevice(device) },
                    onDiagnostics = { onRunDiagnostics(device.ipAddress) },
                    onForget = { onForgetDevice(device.ipAddress) }
                )
            }
        }

        // Manual IP Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.manual_connect),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = manualIp,
                        onValueChange = { manualIp = it.trim() },
                        label = { Text(stringResource(R.string.ip_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VestelRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = manualMac,
                        onValueChange = { manualMac = it.trim().uppercase() },
                        label = { Text(stringResource(R.string.mac_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VestelRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = manualName,
                        onValueChange = { manualName = it },
                        label = { Text(stringResource(R.string.device_name_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VestelRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (manualIp.isNotBlank()) {
                                onManualConnect(manualIp, manualMac, manualName)
                            }
                        },
                        enabled = manualIp.isNotBlank() && !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = VestelRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kaydet ve Bağlan", color = Color.White)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DeviceItemCard(
    device: TvDevice,
    isActive: Boolean,
    enabled: Boolean,
    onConnect: () -> Unit,
    onPair: () -> Unit,
    onDiagnostics: () -> Unit,
    onForget: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = if (isActive) StatusGreen else VestelRed,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${device.ipAddress} • ${device.model}${if (!device.macAddress.isNullOrBlank()) " • " + device.macAddress else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextMuted,
                        fontSize = 11.sp
                    )
                }

                if (isActive) {
                    Text(
                        text = "AKTİF",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onConnect,
                    enabled = enabled,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Bağlan", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onPair,
                    enabled = enabled,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VestelRed)
                ) {
                    Text("Eşleştir", fontSize = 12.sp)
                }

                IconButton(onClick = onDiagnostics, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.NetworkCheck, contentDescription = "Teşhis", tint = StatusBlue)
                }

                if (onForget != null) {
                    IconButton(onClick = onForget, enabled = enabled, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Unut", tint = StatusRed)
                    }
                }
            }
        }
    }
}

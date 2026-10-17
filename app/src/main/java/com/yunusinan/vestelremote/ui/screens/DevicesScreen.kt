package com.yunusinan.vestelremote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.ConnectionDiagnostic
import com.yunusinan.vestelremote.domain.model.ConnectionStatus
import com.yunusinan.vestelremote.domain.model.TvDevice
import com.yunusinan.vestelremote.ui.components.CopyDiagnosticsButton
import com.yunusinan.vestelremote.ui.theme.*

private val Ipv4Regex = Regex(
    "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$"
)

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
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var manualIp by remember { mutableStateOf("") }
    var manualMac by remember { mutableStateOf("") }
    var manualName by remember { mutableStateOf("Vestel 55QG9960") }
    var showInvalidIpError by remember { mutableStateOf(false) }

    val isScanning = connectionStatus is ConnectionStatus.Scanning
    val busy = connectionStatus.isConnecting || connectionStatus.isPairing

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VestelDesignTokens.Gradients.screenBackground)
            .padding(horizontal = VestelDesignTokens.Spacing.lg, vertical = VestelDesignTokens.Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(VestelDesignTokens.Spacing.md)
    ) {
        // Redesigned Header Hierarchy: Back Button + Title + Subtitle + Dedicated Action Row
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (onBackClick != null) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.btn_back),
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = stringResource(R.string.devices_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.devices_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Prominent Scan & Copy Diagnostics Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onScanClick,
                        enabled = !isScanning && !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = VestelRed),
                        shape = VestelDesignTokens.Radius.buttonShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 11.dp),
                        modifier = Modifier.weight(1.32f)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(15.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.scanning),
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.WifiFind,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.scan_network),
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    CopyDiagnosticsButton(
                        attempts = connectionDiagnostics,
                        isMockMode = isMockMode,
                        compactLabel = true,
                        modifier = Modifier.weight(0.88f)
                    )
                }
            }
        }

        // Connection Status Banner Card
        item {
            val (statusColor, statusText) = when (connectionStatus) {
                is ConnectionStatus.Failed -> Pair(StatusRed, connectionStatus.message)
                is ConnectionStatus.ConnectingToPairingService -> Pair(StatusYellow, "Eşleştirme servisine (Port 6467) bağlanılıyor…")
                is ConnectionStatus.TlsEstablished -> Pair(StatusYellow, "TLS el sıkışması tamamlandı")
                is ConnectionStatus.PairingNegotiation -> Pair(StatusYellow, "Eşleştirme aşaması: ${connectionStatus.stage}")
                is ConnectionStatus.AwaitingTvPin -> Pair(StatusYellow, "Televizyon ekranındaki 6 haneli eşleştirme kodunu girin")
                is ConnectionStatus.VerifyingPin -> Pair(StatusYellow, "Eşleştirme kodu doğrulanıyor…")
                is ConnectionStatus.ConnectingToRemoteService -> Pair(StatusBlue, "Kumanda servisine (Port 6466) bağlanılıyor…")
                is ConnectionStatus.Connected -> Pair(
                    StatusGreen,
                    "${connectionStatus.device.displayTitle} (${connectionStatus.device.ipAddress}) ile aktif bağlantı hazır."
                )
                is ConnectionStatus.Scanning -> Pair(StatusBlue, "Yerel ağda mDNS ve SSDP üzerinden televizyon aranıyor…")
                else -> Pair(DarkTextSecondary, "İlk bağlantıda Eşleştir seçeneğini, sonraki bağlantılarda Bağlan seçeneğini kullanın.")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(statusColor.copy(alpha = 0.1f))
                    .border(1.dp, statusColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (connectionStatus is ConnectionStatus.Failed) StatusRed else Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Discovered Devices Section
        if (discoveredDevices.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.discovered_devices),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "${discoveredDevices.size} Cihaz",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusGreen
                    )
                }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.saved_devices),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                if (savedDevices.isNotEmpty()) {
                    Text(
                        text = "${savedDevices.size} Kayıt",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkTextSecondary
                    )
                }
            }
        }

        if (savedDevices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, VestelDesignTokens.Radius.compactCardShape),
                    shape = VestelDesignTokens.Radius.compactCardShape,
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Text(
                        text = "Henüz kayıtlı televizyon yok. Aşağıdan manuel IP girebilir veya yukarıdaki 'Ağda Ara' butonunu kullanarak televizyonunuzu bulabilirsiniz.",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, VestelDesignTokens.Radius.cardShape),
                shape = VestelDesignTokens.Radius.cardShape,
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(VestelDesignTokens.Spacing.lg)) {
                    Text(
                        text = stringResource(R.string.manual_connect),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "mDNS/SSDP keşfi modem tarafından engelleniyorsa televizyonun yerel IPv4 adresini doğrudan girin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = manualIp,
                        onValueChange = {
                            manualIp = it.trim()
                            if (showInvalidIpError) showInvalidIpError = false
                        },
                        label = { Text(stringResource(R.string.ip_hint)) },
                        isError = showInvalidIpError,
                        supportingText = if (showInvalidIpError) {
                            { Text(stringResource(R.string.err_invalid_ip), color = StatusRed) }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = VestelDesignTokens.Radius.buttonShape,
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
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = VestelDesignTokens.Radius.buttonShape,
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
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        shape = VestelDesignTokens.Radius.buttonShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VestelRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val trimmedIp = manualIp.trim()
                            if (Ipv4Regex.matches(trimmedIp)) {
                                showInvalidIpError = false
                                onManualConnect(trimmedIp, manualMac, manualName)
                            } else {
                                showInvalidIpError = true
                            }
                        },
                        enabled = manualIp.isNotBlank() && !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = VestelRed),
                        shape = VestelDesignTokens.Radius.buttonShape,
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kaydet ve Bağlan",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(VestelDesignTokens.Spacing.lg))
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
    val isPaired = device.isPaired || !device.serverFingerprint.isNullOrBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isActive) StatusGreen.copy(alpha = 0.35f) else DarkBorder,
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isActive) StatusGreen.copy(alpha = 0.15f)
                            else VestelRed.copy(alpha = 0.14f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = if (isActive) StatusGreen else VestelRed,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val meta = buildString {
                        append(device.ipAddress)
                        if (device.model.isNotBlank()) append(" • ${device.model}")
                        if (!device.macAddress.isNullOrBlank()) append(" • ${device.macAddress}")
                    }
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                val badgeColor = when {
                    isActive -> StatusGreen
                    isPaired -> StatusBlue
                    else -> DarkTextMuted
                }
                val badgeText = when {
                    isActive -> "AKTİF"
                    isPaired -> "EŞLEŞTİ"
                    else -> "YENİ"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.16f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onConnect,
                    enabled = enabled,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) StatusGreen.copy(alpha = 0.22f) else VestelRed
                    )
                ) {
                    Text(
                        text = if (isActive) "Yeniden Bağlan" else stringResource(R.string.connect),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onPair,
                    enabled = enabled,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = stringResource(R.string.pair_tv),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDiagnostics,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = stringResource(R.string.run_diagnostics),
                        tint = StatusBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (onForget != null) {
                    IconButton(
                        onClick = onForget,
                        enabled = enabled,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = stringResource(R.string.forget_device),
                            tint = StatusRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

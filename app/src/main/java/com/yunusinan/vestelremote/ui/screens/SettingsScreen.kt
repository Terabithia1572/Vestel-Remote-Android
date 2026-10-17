package com.yunusinan.vestelremote.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.AppLaunchDiagnostic
import com.yunusinan.vestelremote.domain.model.ConnectionDiagnostic
import com.yunusinan.vestelremote.domain.model.ProtocolEvent
import com.yunusinan.vestelremote.domain.model.TvDevice
import com.yunusinan.vestelremote.ui.theme.*

@Composable
fun SettingsScreen(
    activeDevice: TvDevice? = null,
    currentLanguage: String,
    currentTheme: String,
    hapticsEnabled: Boolean,
    isMockMode: Boolean,
    protocolEvents: List<ProtocolEvent>,
    connectionDiagnostics: List<ConnectionDiagnostic>,
    appLaunchDiagnostics: List<AppLaunchDiagnostic> = emptyList(),
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onMockModeChange: (Boolean) -> Unit,
    onNavigateToDevices: () -> Unit = {},
    onRescanDevices: () -> Unit = {},
    onResetPairing: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEventLogDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showChecklistDialog by remember { mutableStateOf(false) }
    var showResetPairingConfirm by remember { mutableStateOf(false) }
    var textSizeOption by remember { mutableStateOf("Orta") }

    val themeDisplayLabel = when (currentTheme) {
        "dark" -> "Koyu"
        "light" -> "Açık"
        else -> "Sistem"
    }

    val languageDisplayLabel = if (currentLanguage == "en") "English" else "Türkçe"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VestelDesignTokens.Gradients.screenBackground)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Screen Header: "Ayarlar"
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }

        // Section 1: "Cihaz"
        item {
            SettingsSectionHeader(title = "Cihaz")
            Spacer(modifier = Modifier.height(6.dp))
            SettingsGroupCard {
                SettingsRowItem(
                    icon = Icons.Default.Tv,
                    title = "Bağlı Cihaz",
                    subtitle = activeDevice?.displayTitle ?: "Bağlı TV Yok",
                    onClick = onNavigateToDevices
                )
                SettingsDivider()
                SettingsRowItem(
                    icon = Icons.Default.Refresh,
                    title = "Yeniden Tara",
                    onClick = onRescanDevices
                )
                SettingsDivider()
                SettingsRowItem(
                    icon = Icons.Default.Extension,
                    title = "Eşleştirmeyi Sıfırla",
                    onClick = {
                        if (activeDevice != null) {
                            showResetPairingConfirm = true
                        } else {
                            onNavigateToDevices()
                        }
                    }
                )
            }
        }

        // Section 2: "Görünüm"
        item {
            SettingsSectionHeader(title = "Görünüm")
            Spacer(modifier = Modifier.height(6.dp))
            SettingsGroupCard {
                SettingsRowItem(
                    icon = Icons.Default.DarkMode,
                    title = "Tema",
                    trailingValue = themeDisplayLabel,
                    onClick = {
                        val nextTheme = when (currentTheme) {
                            "system" -> "dark"
                            "dark" -> "light"
                            else -> "system"
                        }
                        onThemeChange(nextTheme)
                    }
                )
                SettingsDivider()
                SettingsRowItem(
                    icon = Icons.Default.Adjust,
                    title = "Renk Teması",
                    trailingValue = "Kırmızı",
                    onClick = {
                        Toast.makeText(context, "Vestel Kırmızı vurgu teması aktif", Toast.LENGTH_SHORT).show()
                    }
                )
                SettingsDivider()
                SettingsRowItem(
                    icon = Icons.Default.Language,
                    title = "Dil",
                    trailingValue = languageDisplayLabel,
                    onClick = {
                        val nextLang = if (currentLanguage == "tr") "en" else "tr"
                        onLanguageChange(nextLang)
                    }
                )
                SettingsDivider()
                SettingsRowItem(
                    icon = Icons.Default.TextFields,
                    title = "Yazı Boyutu",
                    trailingValue = textSizeOption,
                    onClick = {
                        textSizeOption = when (textSizeOption) {
                            "Küçük" -> "Orta"
                            "Orta" -> "Büyük"
                            else -> "Küçük"
                        }
                    }
                )
            }
        }

        // Section 3: "Gelişmiş"
        item {
            SettingsSectionHeader(title = "Gelişmiş")
            Spacer(modifier = Modifier.height(6.dp))
            SettingsGroupCard {
                SettingsRowItem(
                    icon = Icons.Default.Description,
                    title = "Protokol Günlüğü",
                    onClick = { showEventLogDialog = true }
                )
                SettingsDivider()
                SettingsRowItem(
                    icon = Icons.Default.ContentCopy,
                    title = "Tanılama Bilgilerini Kopyala",
                    onClick = {
                        val payload = buildString {
                            appendLine("Vestel Remote / pairing-audit-20261017 / mockMode=$isMockMode")
                            appendLine("Uygulama oturumundaki son 20 bağlantı denemesi; PIN/anahtar/paket içeriği kaydedilmez.")
                            if (connectionDiagnostics.isEmpty()) appendLine("Henüz gerçek bağlantı denemesi yok.")
                            connectionDiagnostics.forEach { appendLine(it.export()); appendLine() }
                            appendLine("--- UYGULAMA BAŞLATMA TEŞHİSİ (SON 25 DENEME) ---")
                            if (appLaunchDiagnostics.isEmpty()) {
                                appendLine("Henüz uygulama başlatma denemesi yok.")
                            } else {
                                appLaunchDiagnostics.forEach { appendLine(it.export()); appendLine() }
                            }
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("VestelRemoteDiagnostics", payload))
                        Toast.makeText(context, "Tanılama bilgileri panoya kopyalandı", Toast.LENGTH_SHORT).show()
                    }
                )
                SettingsDivider()
                SettingsRowItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.about_title),
                    onClick = { showAboutDialog = true }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Reset Pairing Confirmation Dialog
    if (showResetPairingConfirm && activeDevice != null) {
        AlertDialog(
            onDismissRequest = { showResetPairingConfirm = false },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Eşleştirmeyi Sıfırla",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${activeDevice.displayTitle} (${activeDevice.ipAddress}) için kayıtlı sertifika ve eşleştirme bilgisi sıfırlansın mı?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetPairingConfirm = false
                        onResetPairing()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VestelRed)
                ) {
                    Text("Sıfırla", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPairingConfirm = false }) {
                    Text(stringResource(R.string.cancel), color = DarkTextSecondary)
                }
            }
        )
    }

    // Protocol Event Log Dialog
    if (showEventLogDialog) {
        AlertDialog(
            onDismissRequest = { showEventLogDialog = false },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Protokol Günlüğü (${protocolEvents.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(protocolEvents) { event ->
                        EventLogItem(event)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showEventLogDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VestelRed)
                ) {
                    Text("Kapat", color = Color.White)
                }
            }
        )
    }

    // About & Developer Tools Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.about_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "DONANIM DOĞRULANDI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = StatusGreen
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = VestelRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.app_version_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )

                    HorizontalDivider(color = DarkDivider)

                    Text(
                        text = stringResource(R.string.developer_name),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.developer_attribution),
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusGreen
                    )
                    Text(
                        text = stringResource(R.string.developer_github),
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusBlue
                    )

                    Text(
                        text = stringResource(R.string.original_project_credit),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                    Text(
                        text = stringResource(R.string.rea_credit),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )

                    HorizontalDivider(color = DarkDivider)

                    // Haptics & Mock TV Mode Toggles preserved inside About / Developer Tools
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.setting_haptic),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = onHapticsChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VestelRed
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.setting_mock_mode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isMockMode,
                            onCheckedChange = onMockModeChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StatusYellow
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            showAboutDialog = false
                            showChecklistDialog = true
                        },
                        shape = VestelDesignTokens.Radius.buttonShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Checklist, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.setting_hw_checklist),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = stringResource(R.string.disclaimer),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 14.sp),
                        color = DarkTextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VestelRed)
                ) {
                    Text("Kapat", color = Color.White)
                }
            }
        )
    }

    // Hardware Checklist Dialog
    if (showChecklistDialog) {
        AlertDialog(
            onDismissRequest = { showChecklistDialog = false },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Vestel 55QG9960 (GoogleTV7288) Doğrulama",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ChecklistItem("mDNS / SSDP Ağ Keşfi (GoogleTV7288)", true)
                    ChecklistItem("Eşleştirme Servisi (Port 6467 TLS)", true)
                    ChecklistItem("PIN Eşleştirme & Sunucu Sertifika Sabitleme", true)
                    ChecklistItem("Kumanda Oturumu (Port 6466 Protobuf)", true)
                    ChecklistItem("D-Pad & Tamam Navigasyonu", true)
                    ChecklistItem("Geri, Ana Ekran & Menü Komutları", true)
                    ChecklistItem("Ses Seviyesi & Sessiz (Mute)", true)
                    ChecklistItem("Kaydedilen Cihazla Yeniden Bağlantı", true)
                    ChecklistItem("DIAL Uygulama Keşfi & Başlatma (Port 8008)", true)
                    ChecklistItem("HDMI Giriş Menüsü Tetikleme", true)
                    ChecklistItem("Wake-on-LAN Bekleme Modundan Uyandırma", false)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showChecklistDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VestelRed)
                ) {
                    Text("Kapat", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium
        ),
        color = DarkTextSecondary,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
    )
}

@Composable
private fun SettingsGroupCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = DarkDivider,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingValue: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (subtitle != null) 62.dp else 54.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.88f),
            modifier = Modifier.size(21.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = DarkTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (!trailingValue.isNullOrBlank()) {
            Text(
                text = trailingValue,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = DarkTextSecondary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = DarkTextSecondary,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun ChecklistItem(label: String, isVerified: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isVerified) StatusGreen else DarkTextMuted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.92f)
        )
    }
}


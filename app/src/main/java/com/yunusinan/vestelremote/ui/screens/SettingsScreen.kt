package com.yunusinan.vestelremote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.ConnectionDiagnostic
import com.yunusinan.vestelremote.domain.model.ProtocolEvent
import com.yunusinan.vestelremote.ui.components.CopyDiagnosticsButton
import com.yunusinan.vestelremote.ui.theme.*

@Composable
fun SettingsScreen(
    currentLanguage: String,
    currentTheme: String,
    hapticsEnabled: Boolean,
    isMockMode: Boolean,
    protocolEvents: List<ProtocolEvent>,
    connectionDiagnostics: List<ConnectionDiagnostic>,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onMockModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showChecklistDialog by remember { mutableStateOf(false) }
    var showEventLogDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = VestelDesignTokens.Spacing.lg, vertical = VestelDesignTokens.Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(VestelDesignTokens.Spacing.md)
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.settings_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTextSecondary
                )
            }
        }

        // Appearance & Behavior Card
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
                        text = "Genel Tercihler",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Language Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.setting_language),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Text(
                                text = if (currentLanguage == "tr") "Türkçe (Varsayılan)" else "English",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = currentLanguage == "tr",
                                onClick = { onLanguageChange("tr") },
                                label = { Text("TR") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VestelRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = currentLanguage == "en",
                                onClick = { onLanguageChange("en") },
                                label = { Text("EN") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VestelRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    )

                    // Theme Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.setting_theme),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Text(
                                text = when (currentTheme) {
                                    "dark" -> "AMOLED Koyu Tema"
                                    "light" -> "Açık Tema"
                                    else -> "Sistem Varsayılanı"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = currentTheme == "dark",
                                onClick = { onThemeChange("dark") },
                                label = { Text("Koyu") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VestelRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = currentTheme == "light",
                                onClick = { onThemeChange("light") },
                                label = { Text("Açık") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VestelRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    )

                    // Haptic Feedback Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.setting_haptic),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Text(
                                text = "Tuş basımlarında dokunsal titreşim geri bildirimi",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = onHapticsChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VestelRed
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    )

                    // Mock TV Mode Toggle (Developer Mode)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.setting_mock_mode),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Text(
                                text = stringResource(R.string.setting_mock_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Switch(
                            checked = isMockMode,
                            onCheckedChange = onMockModeChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StatusYellow
                            )
                        )
                    }
                }
            }
        }

        // Diagnostics & Validation Tools Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, VestelDesignTokens.Radius.cardShape),
                shape = VestelDesignTokens.Radius.cardShape,
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier.padding(VestelDesignTokens.Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Geliştirici ve Donanım Araçları",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    CopyDiagnosticsButton(
                        attempts = connectionDiagnostics,
                        isMockMode = isMockMode,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = { showChecklistDialog = true },
                        shape = VestelDesignTokens.Radius.buttonShape,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.9f))
                    ) {
                        Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(18.dp), tint = StatusGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.setting_hw_checklist),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = { showEventLogDialog = true },
                        shape = VestelDesignTokens.Radius.buttonShape,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.9f))
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp), tint = StatusBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${stringResource(R.string.setting_event_log)} (${protocolEvents.size})",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // About & Attribution Card (Required Attribution)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, VestelDesignTokens.Radius.cardShape),
                shape = VestelDesignTokens.Radius.cardShape,
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.about_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "DONANIM DOĞRULANDI",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                                color = StatusGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        color = VestelRed
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.app_version_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    )

                    Text(
                        text = stringResource(R.string.developer_name),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.developer_attribution),
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.developer_github),
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusBlue
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.original_project_credit),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.rea_credit),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.disclaimer),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, lineHeight = 15.sp),
                        color = DarkTextMuted
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(VestelDesignTokens.Spacing.lg))
        }
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
                    Text(
                        text = "Fiziksel Vestel 55QG9960 (GoogleTV7288) üzerinde doğrulanan özellikler:",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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

    // Protocol Event Log Dialog
    if (showEventLogDialog) {
        AlertDialog(
            onDismissRequest = { showEventLogDialog = false },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Protokol Olay Geçmişi (${protocolEvents.size})",
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

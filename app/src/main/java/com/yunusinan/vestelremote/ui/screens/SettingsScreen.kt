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
import com.yunusinan.vestelremote.domain.model.ProtocolEvent
import com.yunusinan.vestelremote.domain.model.ConnectionDiagnostic
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // Appearance & Behavior Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Genel Tercihler",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Language Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.setting_language), color = Color.White, fontSize = 14.sp)
                            Text(if (currentLanguage == "tr") "Türkçe" else "English", color = DarkTextMuted, fontSize = 12.sp)
                        }

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

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))

                    // Theme Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.setting_theme), color = Color.White, fontSize = 14.sp)
                            Text(when (currentTheme) {
                                "dark" -> "AMOLED Koyu"
                                "light" -> "Açık"
                                else -> "Sistem Varsayılanı"
                            }, color = DarkTextMuted, fontSize = 12.sp)
                        }

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

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))

                    // Haptic Feedback Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.setting_haptic), color = Color.White, fontSize = 14.sp)
                            Text("Tuş basımlarında dokunsal titreşim", color = DarkTextMuted, fontSize = 12.sp)
                        }

                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = onHapticsChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VestelRed
                            )
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))

                    // Mock TV Mode Toggle (Developer Mode)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.setting_mock_mode), color = Color.White, fontSize = 14.sp)
                            Text(stringResource(R.string.setting_mock_desc), color = DarkTextMuted, fontSize = 11.sp)
                        }

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

        // Diagnostics & Validation Tools
        item {
            CopyDiagnosticsButton(connectionDiagnostics, isMockMode)
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Geliştirici ve Donanım Araçları",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showChecklistDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.setting_hw_checklist), fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showEventLogDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${stringResource(R.string.setting_event_log)} (${protocolEvents.size})", fontSize = 13.sp)
                    }
                }
            }
        }

        // About & Attribution Card (Required Attribution)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(R.string.about_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        color = VestelRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.app_version_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = stringResource(R.string.developer_name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.developer_attribution),
                        style = MaterialTheme.typography.bodyMedium,
                        color = StatusGreen,
                        fontSize = 12.sp
                    )
                    Text(
                        text = stringResource(R.string.developer_github),
                        style = MaterialTheme.typography.bodyMedium,
                        color = StatusBlue,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.original_project_credit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = stringResource(R.string.rea_credit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.disclaimer),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
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
                    text = "Vestel 55QG9960 Donanım Doğrulama",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Gerçek televizyonda doğrulama bekleniyor.", color = DarkTextSecondary)
                    ChecklistItem("Android TV Sürümü", false)
                    ChecklistItem("Android TV Remote Servisi (Port 6466)", false)
                    ChecklistItem("Eşleştirme Servisi (Port 6467)", false)
                    ChecklistItem("mDNS / SSDP Keşfi", false)
                    ChecklistItem("PIN Eşleştirme Handshake", false)
                    ChecklistItem("D-Pad & Tamam Navigasyonu", false)
                    ChecklistItem("Geri & Ana Menü (Home)", false)
                    ChecklistItem("Ses Seviyesi & Sessiz (Mute)", false)
                    ChecklistItem("DIAL Uygulama Başlatma (YouTube, Netflix)", false)
                    ChecklistItem("HDMI Kaynak Seçimi", false)
                    ChecklistItem("Wake-on-LAN Bekleme Modundan Uyandırma", false)
                    ChecklistItem("Yeniden Otomatik Bağlantı", false)
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
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp
        )
    }
}


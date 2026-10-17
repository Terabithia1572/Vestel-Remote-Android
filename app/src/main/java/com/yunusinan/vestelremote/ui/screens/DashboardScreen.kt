package com.yunusinan.vestelremote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.ConnectionStatus
import com.yunusinan.vestelremote.domain.model.ProtocolEvent
import com.yunusinan.vestelremote.domain.model.TvCommand
import com.yunusinan.vestelremote.domain.model.TvDevice
import com.yunusinan.vestelremote.ui.components.RoundIconButton
import com.yunusinan.vestelremote.ui.components.TvStatusCard
import com.yunusinan.vestelremote.ui.theme.*

@Composable
fun DashboardScreen(
    device: TvDevice?,
    connectionStatus: ConnectionStatus,
    volume: Int,
    isVolumeReportedByTv: Boolean,
    isMuted: Boolean,
    currentApp: String,
    currentInput: String,
    recentEvents: List<ProtocolEvent>,
    onCommand: (TvCommand) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onVolumeChangeFinished: () -> Unit,
    onWakeOnLan: () -> Unit,
    onNavigateToDevices: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionStatus.isConnected

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = VestelDesignTokens.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(VestelDesignTokens.Spacing.md)
    ) {
        item {
            Spacer(modifier = Modifier.height(VestelDesignTokens.Spacing.xs))
            TvStatusCard(
                device = device,
                connectionStatus = connectionStatus,
                volume = volume,
                isVolumeReportedByTv = isVolumeReportedByTv,
                isMuted = isMuted,
                currentApp = currentApp,
                onPowerClick = { onCommand(TvCommand.Power) },
                onMuteClick = { onCommand(TvCommand.Mute) },
                onVolumeChange = onVolumeChange,
                onVolumeChangeFinished = onVolumeChangeFinished,
                onVolumeDownClick = { onCommand(TvCommand.VolumeDown) },
                onVolumeUpClick = { onCommand(TvCommand.VolumeUp) }
            )
        }

        // Quick Controls Card
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
                        text = stringResource(R.string.quick_actions),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        QuickActionItem(
                            icon = Icons.Default.Home,
                            label = stringResource(R.string.quick_home),
                            accessibilityDescription = stringResource(R.string.btn_home),
                            onClick = { onCommand(TvCommand.Home) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionItem(
                            icon = Icons.Default.ArrowBack,
                            label = stringResource(R.string.quick_back),
                            accessibilityDescription = stringResource(R.string.btn_back),
                            onClick = { onCommand(TvCommand.Back) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionItem(
                            icon = Icons.Default.Mic,
                            label = stringResource(R.string.quick_assistant),
                            accessibilityDescription = stringResource(R.string.btn_assistant),
                            onClick = { onCommand(TvCommand.Assistant) },
                            tint = VestelRed,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionItem(
                            icon = Icons.Default.PowerSettingsNew,
                            label = stringResource(R.string.quick_wol),
                            accessibilityDescription = stringResource(R.string.wol_wake),
                            onClick = onWakeOnLan,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionItem(
                            icon = Icons.Default.Devices,
                            label = stringResource(R.string.quick_devices),
                            accessibilityDescription = stringResource(R.string.nav_devices),
                            onClick = onNavigateToDevices,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Current Input & Verified Device Info Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, VestelDesignTokens.Radius.cardShape),
                shape = VestelDesignTokens.Radius.cardShape,
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(VestelDesignTokens.Spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.current_input),
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val inputLabel = if (isConnected && currentInput != "Seçilmedi") {
                            currentInput
                        } else if (currentInput != "Seçilmedi") {
                            "$currentInput (Son Seçim)"
                        } else {
                            "Kaynağı Girişler sekmesinden seçin"
                        }
                        Text(
                            text = inputLabel,
                            style = if (currentInput != "Seçilmedi") MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodySmall,
                            color = if (currentInput != "Seçilmedi") Color.White else DarkTextSecondary,
                            fontWeight = if (currentInput != "Seçilmedi") FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isConnected) "Bağlı Cihaz" else "Hedef Profil",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isConnected && device != null) {
                                device.displayTitle
                            } else {
                                device?.displayTitle ?: "Bağlı TV Yok"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isConnected) StatusGreen else VestelRed,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Protocol Activity Log Preview Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.setting_event_log),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${recentEvents.size} Olay",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkTextSecondary
                    )
                }
            }
        }

        if (recentEvents.isEmpty()) {
            item {
                Text(
                    text = "Henüz ağ etkinliği kaydedilmedi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkTextMuted
                )
            }
        } else {
            items(recentEvents.take(6)) { event ->
                EventLogItem(event)
            }
        }

        item {
            Spacer(modifier = Modifier.height(VestelDesignTokens.Spacing.md))
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    accessibilityDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White.copy(alpha = 0.88f)
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RoundIconButton(
            icon = icon,
            contentDescription = accessibilityDescription,
            onClick = onClick,
            size = VestelDesignTokens.Dimensions.quickActionButtonSize,
            iconSize = VestelDesignTokens.IconSize.md,
            tint = tint,
            backgroundColor = Color.White.copy(alpha = 0.06f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
            color = DarkTextSecondary,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EventLogItem(event: ProtocolEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(0.5.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (badgeColor, badgeLabel) = when (event.direction) {
            ProtocolEvent.Direction.OUTGOING -> Pair(VestelRed, "TX")
            ProtocolEvent.Direction.INCOMING -> Pair(StatusGreen, "RX")
            ProtocolEvent.Direction.SYSTEM -> Pair(StatusBlue, "SYS")
            ProtocolEvent.Direction.ERROR -> Pair(StatusRed, "ERR")
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor.copy(alpha = 0.16f))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                color = badgeColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = event.detail,
                style = MaterialTheme.typography.bodySmall,
                color = DarkTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = event.formattedTime,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = DarkTextMuted
        )
    }
}

package com.yunusinan.vestelremote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    isMuted: Boolean,
    currentApp: String,
    currentInput: String,
    recentEvents: List<ProtocolEvent>,
    onCommand: (TvCommand) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onWakeOnLan: () -> Unit,
    onNavigateToDevices: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // TV Hero Card
            TvStatusCard(
                device = device,
                connectionStatus = connectionStatus,
                volume = volume,
                isMuted = isMuted,
                currentApp = currentApp,
                onPowerClick = { onCommand(TvCommand.Power) },
                onMuteClick = { onCommand(TvCommand.Mute) },
                onVolumeChange = onVolumeChange
            )
        }

        // Quick Controls Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.quick_actions),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickActionItem(
                            icon = Icons.Default.Home,
                            label = stringResource(R.string.btn_home),
                            onClick = { onCommand(TvCommand.Home) }
                        )
                        QuickActionItem(
                            icon = Icons.Default.ArrowBack,
                            label = stringResource(R.string.btn_back),
                            onClick = { onCommand(TvCommand.Back) }
                        )
                        QuickActionItem(
                            icon = Icons.Default.Mic,
                            label = stringResource(R.string.btn_assistant),
                            onClick = { onCommand(TvCommand.Assistant) },
                            tint = VestelRed
                        )
                        QuickActionItem(
                            icon = Icons.Default.SettingsRemote,
                            label = stringResource(R.string.wol_wake),
                            onClick = onWakeOnLan
                        )
                        QuickActionItem(
                            icon = Icons.Default.Devices,
                            label = stringResource(R.string.nav_devices),
                            onClick = onNavigateToDevices
                        )
                    }
                }
            }
        }

        // Current Input & System Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.current_input),
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkTextMuted
                        )
                        Text(
                            text = currentInput,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Model",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkTextMuted
                        )
                        Text(
                            text = "Vestel 55QG9960",
                            style = MaterialTheme.typography.titleMedium,
                            color = VestelRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Protocol Activity Log Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Protokol Olay Günlüğü",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${recentEvents.size} Olay",
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkTextSecondary
                )
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
            items(recentEvents.take(5)) { event ->
                EventLogItem(event)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.White.copy(alpha = 0.85f)
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        RoundIconButton(
            icon = icon,
            contentDescription = label,
            onClick = onClick,
            size = 50.dp,
            iconSize = 24.dp,
            tint = tint,
            backgroundColor = Color.White.copy(alpha = 0.06f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DarkTextSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
fun EventLogItem(event: ProtocolEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val badgeColor = when (event.direction) {
            ProtocolEvent.Direction.OUTGOING -> VestelRed
            ProtocolEvent.Direction.INCOMING -> StatusGreen
            ProtocolEvent.Direction.SYSTEM -> StatusBlue
            ProtocolEvent.Direction.ERROR -> StatusRed
        }

        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(badgeColor)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = event.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkTextMuted,
                fontSize = 11.sp
            )
        }

        Text(
            text = event.formattedTime,
            style = MaterialTheme.typography.labelSmall,
            color = DarkTextMuted,
            fontSize = 10.sp
        )
    }
}

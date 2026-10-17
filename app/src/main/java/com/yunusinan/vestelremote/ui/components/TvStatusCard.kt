package com.yunusinan.vestelremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.ConnectionStatus
import com.yunusinan.vestelremote.domain.model.TvDevice
import com.yunusinan.vestelremote.ui.theme.*

@Composable
fun TvStatusCard(
    device: TvDevice?,
    connectionStatus: ConnectionStatus,
    volume: Int,
    isMuted: Boolean,
    currentApp: String,
    onPowerClick: () -> Unit,
    onMuteClick: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: TV Icon, Names & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VestelRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = "TV",
                        tint = VestelRed,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device?.displayTitle ?: stringResource(R.string.default_tv_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Vestel 55QG9960 • ${device?.ipAddress ?: "IP Yapılandırılmadı"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Power Button
                RoundIconButton(
                    icon = Icons.Default.PowerSettingsNew,
                    contentDescription = "Power",
                    onClick = onPowerClick,
                    size = 42.dp,
                    iconSize = 22.dp,
                    tint = if (connectionStatus.isConnected) StatusGreen else VestelRed,
                    backgroundColor = Color.White.copy(alpha = 0.08f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status Indicator & Active App Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Connection Badge
                val (badgeColor, badgeText) = when (connectionStatus) {
                    is ConnectionStatus.Connected -> Pair(StatusGreen, stringResource(R.string.status_connected))
                    is ConnectionStatus.Connecting,
                    is ConnectionStatus.ConnectingToRemoteService -> Pair(StatusBlue, stringResource(R.string.status_connecting))
                    is ConnectionStatus.ConnectingToPairingService,
                    is ConnectionStatus.TlsEstablished,
                    is ConnectionStatus.PairingNegotiation,
                    is ConnectionStatus.AwaitingTvPin,
                    is ConnectionStatus.VerifyingPin,
                    is ConnectionStatus.Paired,
                    is ConnectionStatus.PairingPrompt,
                    ConnectionStatus.PairingInProgress -> Pair(StatusYellow, stringResource(R.string.status_pairing))
                    is ConnectionStatus.Standby -> Pair(StatusYellow, stringResource(R.string.status_standby))
                    is ConnectionStatus.Failed -> Pair(StatusRed, connectionStatus.message)
                    is ConnectionStatus.Error -> Pair(StatusRed, stringResource(R.string.status_error))
                    else -> Pair(DarkTextMuted, stringResource(R.string.status_disconnected))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = badgeColor,
                        fontSize = 12.sp
                    )
                }

                // Active App/Input
                Text(
                    text = currentApp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Volume Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMuteClick) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeDown,
                        contentDescription = "Mute",
                        tint = if (isMuted) VestelRed else Color.White.copy(alpha = 0.8f)
                    )
                }

                Slider(
                    value = volume.toFloat(),
                    onValueChange = { onVolumeChange(it.toInt()) },
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = VestelRed,
                        activeTrackColor = VestelRed,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )

                Text(
                    text = if (isMuted) "MUTED" else "$volume%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isMuted) VestelRed else DarkTextSecondary,
                    modifier = Modifier.width(44.dp)
                )
            }
        }
    }
}

package com.yunusinan.vestelremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    isVolumeReportedByTv: Boolean,
    isMuted: Boolean,
    currentApp: String,
    onPowerClick: () -> Unit,
    onMuteClick: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onVolumeChangeFinished: () -> Unit,
    onVolumeDownClick: () -> Unit,
    onVolumeUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionStatus.isConnected

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isConnected) StatusGreen.copy(alpha = 0.22f) else DarkBorder,
                shape = VestelDesignTokens.Radius.heroCardShape
            ),
        shape = VestelDesignTokens.Radius.heroCardShape,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: TV Icon, Device Identity & Power Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isConnected) StatusGreen.copy(alpha = 0.14f)
                            else VestelRed.copy(alpha = 0.14f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = if (isConnected) StatusGreen else VestelRed,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device?.displayTitle ?: stringResource(R.string.default_tv_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val subtitleText = if (device != null) {
                        val modelLabel = device.model.takeIf { it.isNotBlank() } ?: "Android TV"
                        "$modelLabel • ${device.ipAddress}"
                    } else {
                        "Eşleştirilmiş TV seçilmedi • Cihazlar sekmesinden bağlanın"
                    }
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                RoundIconButton(
                    icon = Icons.Default.PowerSettingsNew,
                    contentDescription = stringResource(R.string.power_off),
                    onClick = onPowerClick,
                    size = 44.dp,
                    iconSize = 22.dp,
                    tint = if (isConnected) StatusGreen else VestelRed,
                    backgroundColor = Color.White.copy(alpha = 0.08f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Connection Status & Session Pill Row
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = badgeColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (isConnected) currentApp else "Oturum Yok",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isConnected) Color.White.copy(alpha = 0.85f) else DarkTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Volume Header Row: Label, Sync Badge & Aligned Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.volume),
                        style = MaterialTheme.typography.labelMedium,
                        color = DarkTextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val modeText = if (isConnected && isVolumeReportedByTv) {
                        stringResource(R.string.volume_tv_synced)
                    } else {
                        stringResource(R.string.volume_step_mode)
                    }
                    val modeColor = if (isConnected && isVolumeReportedByTv) StatusGreen else DarkTextMuted
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(modeColor.copy(alpha = 0.14f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = modeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = modeColor
                        )
                    }
                }

                val volumeDisplayText = when {
                    isMuted -> stringResource(R.string.mute).uppercase()
                    isConnected && isVolumeReportedByTv -> "%$volume"
                    else -> "~%$volume"
                }

                Text(
                    text = volumeDisplayText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isMuted) VestelRed else Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Stabilized Volume Slider & Precision Step Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMuteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isMuted -> Icons.Default.VolumeMute
                            volume > 50 -> Icons.Default.VolumeUp
                            else -> Icons.Default.VolumeDown
                        },
                        contentDescription = stringResource(if (isMuted) R.string.unmute else R.string.mute),
                        tint = if (isMuted) VestelRed else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onVolumeDownClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = stringResource(R.string.btn_vol_down),
                        tint = DarkTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Slider(
                    value = volume.toFloat(),
                    onValueChange = { onVolumeChange(it.toInt()) },
                    onValueChangeFinished = onVolumeChangeFinished,
                    valueRange = 0f..100f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = VestelRed,
                        activeTrackColor = VestelRed,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )

                IconButton(
                    onClick = onVolumeUpClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.btn_vol_up),
                        tint = DarkTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

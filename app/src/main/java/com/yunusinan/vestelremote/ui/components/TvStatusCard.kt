package com.yunusinan.vestelremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
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

/**
 * Reference Dashboard Top Card: Compact connected-TV information card with
 * red TV badge on left, device title & model+IP subtitle in center,
 * cyan-ringed power control on right, and connection status bar below divider.
 */
@Composable
fun TvStatusCard(
    device: TvDevice?,
    connectionStatus: ConnectionStatus,
    onPowerClick: () -> Unit,
    onDeviceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionStatus.isConnected

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = DarkBorder,
                shape = VestelDesignTokens.Radius.heroCardShape
            )
            .clip(VestelDesignTokens.Radius.heroCardShape)
            .clickable(onClick = onDeviceClick),
        shape = VestelDesignTokens.Radius.heroCardShape,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Row: Red TV Badge, Device Identity & Cyan Power Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(VestelRed.copy(alpha = 0.15f))
                        .border(0.5.dp, VestelRed.copy(alpha = 0.32f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = VestelRed,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device?.displayTitle ?: stringResource(R.string.default_tv_name),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val subtitleText = if (device != null) {
                        val rawModel = device.model.ifBlank { "55QG9960" }
                        val formattedModel = if (rawModel.startsWith("Vestel", ignoreCase = true)) {
                            rawModel
                        } else {
                            "Vestel $rawModel"
                        }
                        "$formattedModel • ${device.ipAddress}"
                    } else {
                        "Cihaz seçilmedi • Bağlanmak için dokunun"
                    }
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = DarkTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Prominent cyan/teal power button matching reference
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) PowerCyanSurface else DarkControlSurface)
                        .border(
                            width = 1.2.dp,
                            color = if (isConnected) PowerCyan.copy(alpha = 0.7f) else VestelRed.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable(onClick = onPowerClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = stringResource(R.string.power_off),
                        tint = if (isConnected) PowerCyan else VestelRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            HorizontalDivider(
                thickness = 0.6.dp,
                color = DarkDivider
            )

            // Bottom Status Row: Status Dot + Status Label on Left, "Vestel TV" on Right
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
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
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = badgeColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (device != null) "Vestel TV" else stringResource(R.string.nav_devices),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!isConnected) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = DarkTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reference Dashboard Separate Horizontal Volume Card:
 * Speaker icon, circular [-] button, red-track Slider with white thumb, percentage text, and circular [+] button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeControlCard(
    volume: Int,
    isMuted: Boolean,
    onMuteClick: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onVolumeChangeFinished: () -> Unit,
    onVolumeDownClick: () -> Unit,
    onVolumeUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, VestelDesignTokens.Radius.cardShape),
        shape = VestelDesignTokens.Radius.cardShape,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMuteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = when {
                        isMuted -> Icons.Default.VolumeMute
                        volume > 50 -> Icons.Default.VolumeUp
                        else -> Icons.Default.VolumeDown
                    },
                    contentDescription = stringResource(if (isMuted) R.string.unmute else R.string.mute),
                    tint = if (isMuted) VestelRed else Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Circular Minus Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkControlSurface)
                    .border(1.dp, DarkControlBorder, CircleShape)
                    .clickable(onClick = onVolumeDownClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = stringResource(R.string.btn_vol_down),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Slider(
                value = volume.toFloat(),
                onValueChange = { onVolumeChange(it.toInt()) },
                onValueChangeFinished = onVolumeChangeFinished,
                valueRange = 0f..100f,
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = VestelRed,
                    inactiveTrackColor = Color(0xFF282E3D)
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                    )
                },
                track = { sliderState ->
                    val fraction = ((sliderState.value - sliderState.valueRange.start) /
                        (sliderState.valueRange.endInclusive - sliderState.valueRange.start)).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF282E3D))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(VestelRed)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (isMuted) stringResource(R.string.mute) else "%$volume",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (isMuted) VestelRed else Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.widthIn(min = 34.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Circular Plus Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkControlSurface)
                    .border(1.dp, DarkControlBorder, CircleShape)
                    .clickable(onClick = onVolumeUpClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.btn_vol_up),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}


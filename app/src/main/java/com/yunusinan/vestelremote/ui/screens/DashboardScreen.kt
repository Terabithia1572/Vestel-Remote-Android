package com.yunusinan.vestelremote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.ConnectionStatus
import com.yunusinan.vestelremote.domain.model.ProtocolEvent
import com.yunusinan.vestelremote.domain.model.TvCommand
import com.yunusinan.vestelremote.domain.model.TvDevice
import com.yunusinan.vestelremote.ui.components.RoundIconButton
import com.yunusinan.vestelremote.ui.components.TvStatusCard
import com.yunusinan.vestelremote.ui.components.VolumeControlCard
import com.yunusinan.vestelremote.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    onNavigateToInputs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isConnected = connectionStatus.isConnected
    var showAllEventsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VestelDesignTokens.Gradients.screenBackground)
            .padding(horizontal = VestelDesignTokens.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Compact Connected-TV Information Card
        item {
            Spacer(modifier = Modifier.height(6.dp))
            TvStatusCard(
                device = device,
                connectionStatus = connectionStatus,
                onPowerClick = { onCommand(TvCommand.Power) },
                onDeviceClick = onNavigateToDevices
            )
        }

        // 2. Separate Horizontal Volume Control Card
        item {
            VolumeControlCard(
                volume = volume,
                isMuted = isMuted,
                onMuteClick = { onCommand(TvCommand.Mute) },
                onVolumeChange = onVolumeChange,
                onVolumeChangeFinished = onVolumeChangeFinished,
                onVolumeDownClick = { onCommand(TvCommand.VolumeDown) },
                onVolumeUpClick = { onCommand(TvCommand.VolumeUp) }
            )
        }

        // 3. Quick Actions Card ("Hızlı İşlemler")
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, VestelDesignTokens.Radius.cardShape),
                shape = VestelDesignTokens.Radius.cardShape,
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        text = stringResource(R.string.quick_actions),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        QuickActionItem(
                            icon = Icons.Default.Home,
                            label = stringResource(R.string.btn_home),
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
                            label = stringResource(R.string.btn_assistant),
                            accessibilityDescription = stringResource(R.string.btn_assistant),
                            onClick = { onCommand(TvCommand.Assistant) },
                            tint = VestelRed,
                            isHighlighted = true,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionItem(
                            icon = Icons.Default.Tv,
                            label = "Kaynaklar",
                            accessibilityDescription = stringResource(R.string.nav_inputs),
                            onClick = {
                                onCommand(TvCommand.Input)
                                onNavigateToInputs()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Compact Active-Input & Model Card
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
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aktif Giriş",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                            color = DarkTextSecondary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        val activeInputDisplay = when {
                            currentInput != "Seçilmedi" -> currentInput
                            isConnected -> currentApp
                            else -> "Seçilmedi"
                        }
                        Text(
                            text = activeInputDisplay,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Model",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                            color = DarkTextSecondary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        val modelDisplay = if (device != null) {
                            val raw = device.model.ifBlank { "55QG9960" }
                            if (raw.startsWith("Vestel", ignoreCase = true)) raw else "Vestel $raw"
                        } else {
                            "Vestel 55QG9960"
                        }
                        Text(
                            text = modelDisplay,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = VestelRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 5. Recent Commands / Activity Section ("Son İşlemler")
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Son İşlemler",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Tümünü Gör",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                        color = DarkTextSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showAllEventsDialog = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, VestelDesignTokens.Radius.cardShape),
                    shape = VestelDesignTokens.Radius.cardShape,
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    val displayedEvents = recentEvents.take(4)
                    if (displayedEvents.isEmpty()) {
                        Text(
                            text = "Henüz ağ etkinliği kaydedilmedi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextMuted,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            displayedEvents.forEachIndexed { index, event ->
                                ReferenceActivityRow(
                                    event = event,
                                    useRedDot = index % 2 == 1 || event.direction == ProtocolEvent.Direction.ERROR
                                )
                                if (index < displayedEvents.lastIndex) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = DarkDivider,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(VestelDesignTokens.Spacing.sm))
        }
    }

    if (showAllEventsDialog) {
        AlertDialog(
            onDismissRequest = { showAllEventsDialog = false },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Protokol Olay Günlüğü (${recentEvents.size})",
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
                    items(recentEvents) { event ->
                        EventLogItem(event)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAllEventsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VestelRed)
                ) {
                    Text("Kapat", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    accessibilityDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White.copy(alpha = 0.92f),
    isHighlighted: Boolean = false
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
            backgroundColor = DarkControlSurface,
            borderColor = if (isHighlighted) VestelRed.copy(alpha = 0.28f) else DarkControlBorder
        )
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color.White.copy(alpha = 0.88f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        )
    }
}

@Composable
private fun ReferenceActivityRow(
    event: ProtocolEvent,
    useRedDot: Boolean
) {
    val dotColor = when (event.direction) {
        ProtocolEvent.Direction.ERROR -> StatusRed
        else -> if (useRedDot) VestelRed else StatusGreen
    }

    val (primaryText, secondaryText) = remember(event.title, event.detail) {
        formatEventForReferenceRow(event)
    }

    val timeShort = remember(event.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                ) {
                    append(primaryText)
                }
                if (secondaryText.isNotBlank()) {
                    append(" ")
                    withStyle(
                        SpanStyle(
                            color = DarkTextSecondary,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    ) {
                        append(secondaryText)
                    }
                }
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = timeShort,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
            color = DarkTextSecondary
        )
    }
}

private fun formatEventForReferenceRow(event: ProtocolEvent): Pair<String, String> {
    val detail = event.detail.trim()
    val keyCodeIndex = detail.indexOf("(keyCode:")
    if (keyCodeIndex > 0) {
        val rawName = detail.substring(0, keyCodeIndex).trim()
        val keyCodePart = detail.substring(keyCodeIndex).trim()
        val snakeName = rawName
            .replace("DPad", "DPAD_")
            .replace(Regex("([a-z])([A-Z])"), "$1_$2")
            .uppercase(Locale.ROOT)
        return snakeName to keyCodePart
    }
    return event.title to "($detail)"
}

@Composable
fun EventLogItem(event: ProtocolEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
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


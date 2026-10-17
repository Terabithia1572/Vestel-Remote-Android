package com.yunusinan.vestelremote.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.TvCommand
import com.yunusinan.vestelremote.ui.components.*
import com.yunusinan.vestelremote.ui.theme.*

@Composable
fun RemoteScreen(
    onCommand: (TvCommand) -> Unit,
    onTouchpadDelta: (dx: Float, dy: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isTouchpadMode by remember { mutableStateOf(false) }
    var showNumpad by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = VestelDesignTokens.Spacing.lg, vertical = VestelDesignTokens.Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Segmented Mode Switcher (D-Pad vs Touchpad)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isTouchpadMode = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isTouchpadMode) VestelRed else Color.Transparent,
                    contentColor = if (!isTouchpadMode) Color.White else DarkTextSecondary
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.nav_remote),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = { isTouchpadMode = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTouchpadMode) VestelRed else Color.Transparent,
                    contentColor = if (isTouchpadMode) Color.White else DarkTextSecondary
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.nav_touchpad),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Top System Controls Row (Power, Input, Assistant, Menu)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RemoteLabeledActionButton(
                icon = Icons.Default.PowerSettingsNew,
                label = "Güç",
                contentDescription = stringResource(R.string.power_off),
                onClick = { onCommand(TvCommand.Power) },
                tint = VestelRed
            )
            RemoteLabeledActionButton(
                icon = Icons.Default.Input,
                label = "Giriş",
                contentDescription = stringResource(R.string.nav_inputs),
                onClick = { onCommand(TvCommand.Input) }
            )
            RemoteLabeledActionButton(
                icon = Icons.Default.Mic,
                label = "Asistan",
                contentDescription = stringResource(R.string.btn_assistant),
                onClick = { onCommand(TvCommand.Assistant) },
                tint = VestelRed
            )
            RemoteLabeledActionButton(
                icon = Icons.Default.Menu,
                label = "Menü",
                contentDescription = stringResource(R.string.btn_menu),
                onClick = { onCommand(TvCommand.Menu) }
            )
        }

        // Center Area: Navigation + Rockers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Volume Rocker
            RockerPill(
                label = "SES",
                onUpClick = { onCommand(TvCommand.VolumeUp) },
                onDownClick = { onCommand(TvCommand.VolumeDown) }
            )

            // Center: D-Pad or Touchpad
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isTouchpadMode) {
                    TouchpadControl(
                        onSwipeDelta = onTouchpadDelta,
                        onTap = { onCommand(TvCommand.Select) }
                    )
                } else {
                    DPadControl(
                        size = VestelDesignTokens.Dimensions.dpadSize,
                        onCommand = onCommand
                    )
                }
            }

            // Channel Rocker
            ChannelRockerPill(
                label = "KNL",
                onUpClick = { onCommand(TvCommand.ChannelUp) },
                onDownClick = { onCommand(TvCommand.ChannelDown) }
            )
        }

        // System Navigation Row (Back, Home, Mute, Info)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RemoteLabeledActionButton(
                icon = Icons.Default.ArrowBack,
                label = stringResource(R.string.btn_back),
                contentDescription = stringResource(R.string.btn_back),
                onClick = { onCommand(TvCommand.Back) }
            )
            RemoteLabeledActionButton(
                icon = Icons.Default.Home,
                label = stringResource(R.string.quick_home),
                contentDescription = stringResource(R.string.btn_home),
                onClick = { onCommand(TvCommand.Home) }
            )
            RemoteLabeledActionButton(
                icon = Icons.Default.VolumeMute,
                label = stringResource(R.string.mute),
                contentDescription = stringResource(R.string.mute),
                onClick = { onCommand(TvCommand.Mute) }
            )
            RemoteLabeledActionButton(
                icon = Icons.Default.Info,
                label = "Bilgi",
                contentDescription = "Bilgi",
                onClick = { onCommand(TvCommand.Info) }
            )
        }

        // Media Playback Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                .padding(vertical = 6.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onCommand(TvCommand.Rewind) }) {
                Icon(Icons.Default.FastRewind, contentDescription = stringResource(R.string.btn_rewind), tint = Color.White)
            }
            IconButton(onClick = { onCommand(TvCommand.PlayPause) }) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.btn_play_pause),
                    tint = VestelRed,
                    modifier = Modifier.size(30.dp)
                )
            }
            IconButton(onClick = { onCommand(TvCommand.Stop) }) {
                Icon(Icons.Default.Stop, contentDescription = stringResource(R.string.btn_stop), tint = Color.White)
            }
            IconButton(onClick = { onCommand(TvCommand.FastForward) }) {
                Icon(Icons.Default.FastForward, contentDescription = stringResource(R.string.btn_fast_forward), tint = Color.White)
            }
        }

        // TV Broadcast Color Keys (Red, Green, Yellow, Blue)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorKeyButton(color = ColorKeyRed, contentDescription = "Kırmızı Tuş", onClick = { onCommand(TvCommand.Red) })
            ColorKeyButton(color = ColorKeyGreen, contentDescription = "Yeşil Tuş", onClick = { onCommand(TvCommand.Green) })
            ColorKeyButton(color = ColorKeyYellow, contentDescription = "Sarı Tuş", onClick = { onCommand(TvCommand.Yellow) })
            ColorKeyButton(color = ColorKeyBlue, contentDescription = "Mavi Tuş", onClick = { onCommand(TvCommand.Blue) })
        }

        // Keypad toggle button
        OutlinedButton(
            onClick = { showNumpad = !showNumpad },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.85f))
        ) {
            Icon(Icons.Default.Dialpad, contentDescription = null, modifier = Modifier.size(16.dp), tint = VestelRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (showNumpad) "Numara Tuşlarını Gizle" else "Numara Tuşlarını Göster",
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Number Pad Grid
        AnimatedVisibility(visible = showNumpad) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceElevated.copy(alpha = 0.55f))
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    NumKeyButton("1", onClick = { onCommand(TvCommand.Number(1)) })
                    NumKeyButton("2", onClick = { onCommand(TvCommand.Number(2)) })
                    NumKeyButton("3", onClick = { onCommand(TvCommand.Number(3)) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    NumKeyButton("4", onClick = { onCommand(TvCommand.Number(4)) })
                    NumKeyButton("5", onClick = { onCommand(TvCommand.Number(5)) })
                    NumKeyButton("6", onClick = { onCommand(TvCommand.Number(6)) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    NumKeyButton("7", onClick = { onCommand(TvCommand.Number(7)) })
                    NumKeyButton("8", onClick = { onCommand(TvCommand.Number(8)) })
                    NumKeyButton("9", onClick = { onCommand(TvCommand.Number(9)) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    RoundIconButton(
                        icon = Icons.Default.List,
                        contentDescription = "Kanal Rehberi (Guide)",
                        onClick = { onCommand(TvCommand.Guide) },
                        size = VestelDesignTokens.Dimensions.numpadButtonSize
                    )
                    NumKeyButton("0", onClick = { onCommand(TvCommand.Number(0)) })
                    RoundIconButton(
                        icon = Icons.Default.Subtitles,
                        contentDescription = "Altyazı / Bilgi",
                        onClick = { onCommand(TvCommand.Info) },
                        size = VestelDesignTokens.Dimensions.numpadButtonSize
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(VestelDesignTokens.Spacing.md))
    }
}

@Composable
private fun RemoteLabeledActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = Color.White.copy(alpha = 0.88f)
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        RoundIconButton(
            icon = icon,
            contentDescription = contentDescription,
            onClick = onClick,
            tint = tint
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = DarkTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

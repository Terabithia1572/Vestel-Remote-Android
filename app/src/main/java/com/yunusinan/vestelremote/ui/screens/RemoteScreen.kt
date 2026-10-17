package com.yunusinan.vestelremote.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
    var showColorKeys by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Switcher (D-Pad vs Touchpad)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurfaceElevated)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isTouchpadMode = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isTouchpadMode) VestelRed else Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.nav_remote), fontSize = 13.sp)
            }

            Button(
                onClick = { isTouchpadMode = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTouchpadMode) VestelRed else Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.nav_touchpad), fontSize = 13.sp)
            }
        }

        // Top System Controls Row (Power, Input, Assistant, Menu)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundIconButton(
                icon = Icons.Default.PowerSettingsNew,
                contentDescription = "Power",
                onClick = { onCommand(TvCommand.Power) },
                tint = VestelRed
            )
            RoundIconButton(
                icon = Icons.Default.Input,
                contentDescription = "Input",
                onClick = { onCommand(TvCommand.Input) }
            )
            RoundIconButton(
                icon = Icons.Default.Mic,
                contentDescription = "Assistant",
                onClick = { onCommand(TvCommand.Assistant) },
                tint = VestelRed
            )
            RoundIconButton(
                icon = Icons.Default.Menu,
                contentDescription = "Menu",
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
                label = "VOL",
                onUpClick = { onCommand(TvCommand.VolumeUp) },
                onDownClick = { onCommand(TvCommand.VolumeDown) }
            )

            // Center: D-Pad or Touchpad
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isTouchpadMode) {
                    TouchpadControl(
                        onSwipeDelta = onTouchpadDelta,
                        onTap = { onCommand(TvCommand.Select) }
                    )
                } else {
                    DPadControl(
                        size = 230.dp,
                        onCommand = onCommand
                    )
                }
            }

            // Channel Rocker
            ChannelRockerPill(
                label = "CH",
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
            RoundIconButton(
                icon = Icons.Default.ArrowBack,
                contentDescription = "Back",
                onClick = { onCommand(TvCommand.Back) }
            )
            RoundIconButton(
                icon = Icons.Default.Home,
                contentDescription = "Home",
                onClick = { onCommand(TvCommand.Home) }
            )
            RoundIconButton(
                icon = Icons.Default.VolumeMute,
                contentDescription = "Mute",
                onClick = { onCommand(TvCommand.Mute) }
            )
            RoundIconButton(
                icon = Icons.Default.Info,
                contentDescription = "Info",
                onClick = { onCommand(TvCommand.Info) }
            )
        }

        // Media Playback Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DarkSurfaceElevated)
                .padding(vertical = 6.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onCommand(TvCommand.Rewind) }) {
                Icon(Icons.Default.FastRewind, contentDescription = "Rewind", tint = Color.White)
            }
            IconButton(onClick = { onCommand(TvCommand.PlayPause) }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play/Pause", tint = VestelRed, modifier = Modifier.size(30.dp))
            }
            IconButton(onClick = { onCommand(TvCommand.Stop) }) {
                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
            }
            IconButton(onClick = { onCommand(TvCommand.FastForward) }) {
                Icon(Icons.Default.FastForward, contentDescription = "Fast Forward", tint = Color.White)
            }
        }

        // TV Broadcast Color Keys (Red, Green, Yellow, Blue)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorKeyButton(color = ColorKeyRed, contentDescription = "Red", onClick = { onCommand(TvCommand.Red) })
            ColorKeyButton(color = ColorKeyGreen, contentDescription = "Green", onClick = { onCommand(TvCommand.Green) })
            ColorKeyButton(color = ColorKeyYellow, contentDescription = "Yellow", onClick = { onCommand(TvCommand.Yellow) })
            ColorKeyButton(color = ColorKeyBlue, contentDescription = "Blue", onClick = { onCommand(TvCommand.Blue) })
        }

        // Keypad toggle button
        TextButton(
            onClick = { showNumpad = !showNumpad },
            colors = ButtonDefaults.textButtonColors(contentColor = DarkTextSecondary)
        ) {
            Icon(Icons.Default.Dialpad, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (showNumpad) "Numaraları Gizle" else "Numara Tuşlarını Göster", fontSize = 12.sp)
        }

        // Number Pad Grid
        AnimatedVisibility(visible = showNumpad) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    NumKeyButton("1", onClick = { onCommand(TvCommand.Number(1)) })
                    NumKeyButton("2", onClick = { onCommand(TvCommand.Number(2)) })
                    NumKeyButton("3", onClick = { onCommand(TvCommand.Number(3)) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    NumKeyButton("4", onClick = { onCommand(TvCommand.Number(4)) })
                    NumKeyButton("5", onClick = { onCommand(TvCommand.Number(5)) })
                    NumKeyButton("6", onClick = { onCommand(TvCommand.Number(6)) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    NumKeyButton("7", onClick = { onCommand(TvCommand.Number(7)) })
                    NumKeyButton("8", onClick = { onCommand(TvCommand.Number(8)) })
                    NumKeyButton("9", onClick = { onCommand(TvCommand.Number(9)) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    RoundIconButton(
                        icon = Icons.Default.List,
                        contentDescription = "Guide",
                        onClick = { onCommand(TvCommand.Guide) },
                        size = 54.dp
                    )
                    NumKeyButton("0", onClick = { onCommand(TvCommand.Number(0)) })
                    RoundIconButton(
                        icon = Icons.Default.Subtitles,
                        contentDescription = "Subtitle",
                        onClick = { onCommand(TvCommand.Info) },
                        size = 54.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

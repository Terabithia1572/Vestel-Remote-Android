package com.yunusinan.vestelremote.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onLaunchAppById: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isTouchpadMode by remember { mutableStateOf(false) }
    var showExtraControls by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VestelDesignTokens.Gradients.screenBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Screen Header: "Kumanda" + Gear Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.nav_remote),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )

            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.nav_settings),
                    tint = DarkTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Row 1: Top Action Row (Power, Input, Home, Back)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundIconButton(
                icon = Icons.Default.PowerSettingsNew,
                contentDescription = stringResource(R.string.power_off),
                onClick = { onCommand(TvCommand.Power) },
                size = 54.dp,
                iconSize = 24.dp,
                tint = VestelRed,
                backgroundColor = Color(0xFF231218),
                borderColor = VestelRed.copy(alpha = 0.45f)
            )
            RoundIconButton(
                icon = Icons.Default.Input,
                contentDescription = stringResource(R.string.nav_inputs),
                onClick = { onCommand(TvCommand.Input) },
                size = 54.dp,
                iconSize = 22.dp,
                tint = Color.White,
                backgroundColor = DarkSurfaceElevated,
                borderColor = DarkControlBorder
            )
            RoundIconButton(
                icon = Icons.Default.Home,
                contentDescription = stringResource(R.string.btn_home),
                onClick = { onCommand(TvCommand.Home) },
                size = 54.dp,
                iconSize = 24.dp,
                tint = Color.White,
                backgroundColor = DarkSurfaceElevated,
                borderColor = DarkControlBorder
            )
            RoundIconButton(
                icon = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.btn_back),
                onClick = { onCommand(TvCommand.Back) },
                size = 54.dp,
                iconSize = 23.dp,
                tint = Color.White,
                backgroundColor = DarkSurfaceElevated,
                borderColor = DarkControlBorder
            )
        }

        // Row 2: Center Control Deck (Ses Rocker + Mute | D-Pad / Touchpad | Kanal Rocker + More)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left Column: Volume Rocker + Mute Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RockerPill(
                    label = "Ses",
                    onUpClick = { onCommand(TvCommand.VolumeUp) },
                    onDownClick = { onCommand(TvCommand.VolumeDown) }
                )

                RoundIconButton(
                    icon = Icons.Default.VolumeOff,
                    contentDescription = stringResource(R.string.mute),
                    onClick = { onCommand(TvCommand.Mute) },
                    size = 48.dp,
                    iconSize = 21.dp,
                    tint = Color.White,
                    backgroundColor = DarkSurfaceElevated,
                    borderColor = DarkControlBorder
                )
            }

            // Center: Circular D-Pad (or Touchpad when toggled)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isTouchpadMode) {
                    TouchpadControl(
                        onSwipeDelta = onTouchpadDelta,
                        onTap = { onCommand(TvCommand.Select) }
                    )
                } else {
                    DPadControl(
                        size = 198.dp,
                        onCommand = onCommand
                    )
                }
            }

            // Right Column: Channel Rocker + More (...) Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChannelRockerPill(
                    label = "Kanal",
                    onUpClick = { onCommand(TvCommand.ChannelUp) },
                    onDownClick = { onCommand(TvCommand.ChannelDown) }
                )

                RoundIconButton(
                    icon = Icons.Default.MoreHoriz,
                    contentDescription = stringResource(R.string.toggle_numpad),
                    onClick = { showExtraControls = !showExtraControls },
                    size = 48.dp,
                    iconSize = 22.dp,
                    tint = if (showExtraControls || isTouchpadMode) VestelRed else Color.White,
                    backgroundColor = DarkSurfaceElevated,
                    borderColor = if (showExtraControls || isTouchpadMode) VestelRed.copy(alpha = 0.5f) else DarkControlBorder
                )
            }
        }

        // Row 3: Playback Controls (3 Separate Pill Buttons: Rewind, Play/Pause, FastForward)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaybackPillButton(
                onClick = { onCommand(TvCommand.Rewind) },
                contentDescription = stringResource(R.string.btn_rewind),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            PlaybackPillButton(
                onClick = { onCommand(TvCommand.PlayPause) },
                contentDescription = stringResource(R.string.btn_play_pause),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Box(
                        modifier = Modifier
                            .width(2.2.dp)
                            .height(12.dp)
                            .background(Color.White, RoundedCornerShape(1.dp))
                    )
                    Spacer(modifier = Modifier.width(2.5.dp))
                    Box(
                        modifier = Modifier
                            .width(2.2.dp)
                            .height(12.dp)
                            .background(Color.White, RoundedCornerShape(1.dp))
                    )
                }
            }

            PlaybackPillButton(
                onClick = { onCommand(TvCommand.FastForward) },
                contentDescription = stringResource(R.string.btn_fast_forward),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Row 4: Prominent Branded App Shortcuts (3x2 Grid)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 4.1: NETFLIX & YouTube
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // NETFLIX
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFFF4F5F7))
                        .clickable { onLaunchAppById("netflix") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NETFLIX",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.4.sp
                        ),
                        color = Color(0xFFE50914)
                    )
                }

                // YouTube
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFFF4F5F7))
                        .clickable { onLaunchAppById("youtube") },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(15.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF0000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "YouTube",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = Color(0xFF121212)
                        )
                    }
                }
            }

            // Row 4.2: Disney+ & prime video
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Disney+
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0E2154), Color(0xFF17388A))
                            )
                        )
                        .border(1.dp, Color(0xFF284CA8), RoundedCornerShape(11.dp))
                        .clickable { onLaunchAppById("disney_plus") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(modifier = Modifier.width(38.dp).height(6.dp)) {
                            drawArc(
                                color = Color(0xFF7CA7FF),
                                startAngle = 195f,
                                sweepAngle = 150f,
                                useCenter = false,
                                style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round),
                                topLeft = Offset(0f, 1.dp.toPx()),
                                size = Size(size.width, size.height * 2.1f)
                            )
                        }
                        Text(
                            text = "Disney+",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }

                // prime video
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF112F66), Color(0xFF1B4B9E))
                            )
                        )
                        .border(1.dp, Color(0xFF2B62BE), RoundedCornerShape(11.dp))
                        .clickable { onLaunchAppById("prime_video") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "prime video",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Canvas(modifier = Modifier.width(36.dp).height(5.dp)) {
                            val path = Path().apply {
                                moveTo(2.dp.toPx(), 1.dp.toPx())
                                quadraticTo(
                                    size.width * 0.5f,
                                    size.height + 2.dp.toPx(),
                                    size.width - 2.dp.toPx(),
                                    1.dp.toPx()
                                )
                            }
                            drawPath(
                                path = path,
                                color = Color(0xFF00B4FF),
                                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }

            // Row 4.3: EXXEN & Blutv
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // EXXEN
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFF1A1E28))
                        .border(1.dp, DarkControlBorder, RoundedCornerShape(11.dp))
                        .clickable { onLaunchAppById("exxen") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "EXXEN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFFFFD600)
                    )
                }

                // Blutv
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFF172447))
                        .border(1.dp, Color(0xFF283D73), RoundedCornerShape(11.dp))
                        .clickable { onLaunchAppById("blutv") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Blutv",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )
                }
            }
        }

        // Row 5: Bottom Utility Row (Menü, Rehber, Bilgi, Altyazı)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RemoteBottomTile(
                icon = Icons.Default.FormatListBulleted,
                label = stringResource(R.string.btn_menu),
                onClick = { onCommand(TvCommand.Menu) },
                modifier = Modifier.weight(1f)
            )
            RemoteBottomTile(
                icon = Icons.Default.DateRange,
                label = "Rehber",
                onClick = { onCommand(TvCommand.Guide) },
                modifier = Modifier.weight(1f)
            )
            RemoteBottomTile(
                icon = Icons.Default.Info,
                label = "Bilgi",
                onClick = { onCommand(TvCommand.Info) },
                modifier = Modifier.weight(1f)
            )
            RemoteBottomTile(
                icon = Icons.Default.Subtitles,
                label = "Altyazı",
                onClick = { onCommand(TvCommand.Captions) },
                modifier = Modifier.weight(1f)
            )
        }

        // Expandable Section via (...) Button: Touchpad Switcher, Color Keys & 0-9 Numeric Keypad
        AnimatedVisibility(visible = showExtraControls) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Touchpad vs D-Pad Mode Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { isTouchpadMode = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isTouchpadMode) VestelRed else Color.Transparent,
                            contentColor = if (!isTouchpadMode) Color.White else DarkTextSecondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("D-Pad", style = MaterialTheme.typography.labelLarge)
                    }
                    Button(
                        onClick = { isTouchpadMode = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTouchpadMode) VestelRed else Color.Transparent,
                            contentColor = if (isTouchpadMode) Color.White else DarkTextSecondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.nav_touchpad), style = MaterialTheme.typography.labelLarge)
                    }
                }

                // TV Broadcast Color Keys (Red, Green, Yellow, Blue)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorKeyButton(color = ColorKeyRed, contentDescription = "Kırmızı Tuş", onClick = { onCommand(TvCommand.Red) })
                    ColorKeyButton(color = ColorKeyGreen, contentDescription = "Yeşil Tuş", onClick = { onCommand(TvCommand.Green) })
                    ColorKeyButton(color = ColorKeyYellow, contentDescription = "Sarı Tuş", onClick = { onCommand(TvCommand.Yellow) })
                    ColorKeyButton(color = ColorKeyBlue, contentDescription = "Mavi Tuş", onClick = { onCommand(TvCommand.Blue) })
                }

                // Number Pad Grid (0-9) + Assistant & Stop
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        NumKeyButton("1", onClick = { onCommand(TvCommand.Number(1)) })
                        NumKeyButton("2", onClick = { onCommand(TvCommand.Number(2)) })
                        NumKeyButton("3", onClick = { onCommand(TvCommand.Number(3)) })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        NumKeyButton("4", onClick = { onCommand(TvCommand.Number(4)) })
                        NumKeyButton("5", onClick = { onCommand(TvCommand.Number(5)) })
                        NumKeyButton("6", onClick = { onCommand(TvCommand.Number(6)) })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        NumKeyButton("7", onClick = { onCommand(TvCommand.Number(7)) })
                        NumKeyButton("8", onClick = { onCommand(TvCommand.Number(8)) })
                        NumKeyButton("9", onClick = { onCommand(TvCommand.Number(9)) })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        RoundIconButton(
                            icon = Icons.Default.Mic,
                            contentDescription = stringResource(R.string.btn_assistant),
                            onClick = { onCommand(TvCommand.Assistant) },
                            tint = VestelRed,
                            size = VestelDesignTokens.Dimensions.numpadButtonSize
                        )
                        NumKeyButton("0", onClick = { onCommand(TvCommand.Number(0)) })
                        RoundIconButton(
                            icon = Icons.Default.Stop,
                            contentDescription = stringResource(R.string.btn_stop),
                            onClick = { onCommand(TvCommand.Stop) },
                            size = VestelDesignTokens.Dimensions.numpadButtonSize
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun PlaybackPillButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkControlBorder, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun RemoteBottomTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(66.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkControlBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White.copy(alpha = 0.92f),
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color.White.copy(alpha = 0.88f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}


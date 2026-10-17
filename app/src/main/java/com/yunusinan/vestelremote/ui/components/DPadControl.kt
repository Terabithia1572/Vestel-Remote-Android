package com.yunusinan.vestelremote.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.TvCommand
import com.yunusinan.vestelremote.ui.theme.DarkBorder
import com.yunusinan.vestelremote.ui.theme.DarkSurfaceElevated
import com.yunusinan.vestelremote.ui.theme.VestelDesignTokens
import com.yunusinan.vestelremote.ui.theme.VestelRed

@Composable
fun DPadControl(
    size: Dp = VestelDesignTokens.Dimensions.dpadSize,
    onCommand: (TvCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(DarkSurfaceElevated)
            .border(1.5.dp, DarkBorder, CircleShape)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        // UP Button
        DPadArrowButton(
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = stringResource(R.string.btn_up),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.52f)
                .fillMaxHeight(0.32f),
            onClick = { onCommand(TvCommand.DPadUp) }
        )

        // DOWN Button
        DPadArrowButton(
            icon = Icons.Default.KeyboardArrowDown,
            contentDescription = stringResource(R.string.btn_down),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.52f)
                .fillMaxHeight(0.32f),
            onClick = { onCommand(TvCommand.DPadDown) }
        )

        // LEFT Button
        DPadArrowButton(
            icon = Icons.Default.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.btn_left),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.32f)
                .fillMaxHeight(0.52f),
            onClick = { onCommand(TvCommand.DPadLeft) }
        )

        // RIGHT Button
        DPadArrowButton(
            icon = Icons.Default.KeyboardArrowRight,
            contentDescription = stringResource(R.string.btn_right),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.32f)
                .fillMaxHeight(0.52f),
            onClick = { onCommand(TvCommand.DPadRight) }
        )

        // CENTER OK Button
        val okInteraction = remember { MutableInteractionSource() }
        val isPressed by okInteraction.collectIsPressedAsState()
        val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "okScale")

        Box(
            modifier = Modifier
                .size(size * 0.37f)
                .scale(scale)
                .clip(CircleShape)
                .background(if (isPressed) VestelRed.copy(alpha = 0.85f) else Color(0xFF21212B))
                .border(
                    width = 1.dp,
                    color = if (isPressed) VestelRed else Color.White.copy(alpha = 0.12f),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = okInteraction,
                    indication = null,
                    onClick = { onCommand(TvCommand.Select) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "OK",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun DPadArrowButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.88f else 1f, label = "arrowScale")

    Box(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isPressed) VestelRed else Color.White.copy(alpha = 0.88f),
            modifier = Modifier.size(32.dp)
        )
    }
}

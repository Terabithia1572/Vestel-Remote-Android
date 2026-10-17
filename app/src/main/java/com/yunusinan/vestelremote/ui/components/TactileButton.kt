package com.yunusinan.vestelremote.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.ui.theme.DarkSurfaceElevated
import com.yunusinan.vestelremote.ui.theme.VestelRed

@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    iconSize: Dp = 24.dp,
    tint: Color = Color.White.copy(alpha = 0.85f),
    backgroundColor: Color = DarkSurfaceElevated,
    activeTintColor: Color = VestelRed
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.88f else 1f, label = "btnScale")

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isPressed) backgroundColor.copy(alpha = 0.6f) else backgroundColor)
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
            tint = if (isPressed) activeTintColor else tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun ColorKeyButton(
    color: Color,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.85f else 1f, label = "colorBtnScale")

    Box(
        modifier = modifier
            .width(58.dp)
            .height(18.dp)
            .scale(scale)
            .clip(RoundedCornerShape(9.dp))
            .background(color.copy(alpha = if (isPressed) 1f else 0.85f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
    )
}

@Composable
fun NumKeyButton(
    digit: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.88f else 1f, label = "numBtnScale")

    Box(
        modifier = modifier
            .size(54.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isPressed) VestelRed.copy(alpha = 0.7f) else DarkSurfaceElevated)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 18.sp
        )
    }
}

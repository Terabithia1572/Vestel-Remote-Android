package com.yunusinan.vestelremote.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.ui.theme.DarkBorder
import com.yunusinan.vestelremote.ui.theme.DarkSurfaceElevated
import com.yunusinan.vestelremote.ui.theme.VestelDesignTokens
import com.yunusinan.vestelremote.ui.theme.VestelRed

@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = VestelDesignTokens.Dimensions.remoteRoundButtonSize,
    iconSize: Dp = 23.dp,
    tint: Color = Color.White.copy(alpha = 0.88f),
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
            .background(if (isPressed) backgroundColor.copy(alpha = 0.65f) else backgroundColor)
            .border(
                width = 1.dp,
                color = if (isPressed) activeTintColor.copy(alpha = 0.45f) else DarkBorder,
                shape = CircleShape
            )
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
    val scale by animateFloatAsState(if (isPressed) 0.88f else 1f, label = "colorBtnScale")

    // Outer Box provides a generous 40dp touch target while keeping a sleek pill appearance
    Box(
        modifier = modifier
            .width(68.dp)
            .height(40.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(58.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = if (isPressed) 1f else 0.86f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = if (isPressed) 0.4f else 0.14f),
                    shape = RoundedCornerShape(8.dp)
                )
        )
    }
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
            .size(VestelDesignTokens.Dimensions.numpadButtonSize)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isPressed) VestelRed.copy(alpha = 0.75f) else DarkSurfaceElevated)
            .border(1.dp, if (isPressed) VestelRed else DarkBorder, CircleShape)
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
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )
    }
}

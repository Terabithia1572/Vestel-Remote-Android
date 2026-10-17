package com.yunusinan.vestelremote.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.ui.theme.DarkBorder
import com.yunusinan.vestelremote.ui.theme.DarkSurfaceElevated
import com.yunusinan.vestelremote.ui.theme.VestelDesignTokens
import com.yunusinan.vestelremote.ui.theme.VestelRed

@Composable
fun RockerPill(
    label: String,
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(33.dp)

    Column(
        modifier = modifier
            .width(VestelDesignTokens.Dimensions.rockerWidth)
            .height(VestelDesignTokens.Dimensions.rockerHeight)
            .clip(pillShape)
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, pillShape)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val upInteraction = remember { MutableInteractionSource() }
        val upPressed by upInteraction.collectIsPressedAsState()
        val upScale by animateFloatAsState(if (upPressed) 0.86f else 1f, label = "upScale")

        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(upScale)
                .clickable(
                    interactionSource = upInteraction,
                    indication = null,
                    onClick = onUpClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "$label +",
                tint = if (upPressed) VestelRed else Color.White.copy(alpha = 0.88f),
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.55f)
        ) {
            HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                ),
                color = Color.White.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.08f))
        }

        val downInteraction = remember { MutableInteractionSource() }
        val downPressed by downInteraction.collectIsPressedAsState()
        val downScale by animateFloatAsState(if (downPressed) 0.86f else 1f, label = "downScale")

        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(downScale)
                .clickable(
                    interactionSource = downInteraction,
                    indication = null,
                    onClick = onDownClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "$label -",
                tint = if (downPressed) VestelRed else Color.White.copy(alpha = 0.88f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ChannelRockerPill(
    label: String = "CH",
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(33.dp)

    Column(
        modifier = modifier
            .width(VestelDesignTokens.Dimensions.rockerWidth)
            .height(VestelDesignTokens.Dimensions.rockerHeight)
            .clip(pillShape)
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, pillShape)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val upInteraction = remember { MutableInteractionSource() }
        val upPressed by upInteraction.collectIsPressedAsState()
        val upScale by animateFloatAsState(if (upPressed) 0.86f else 1f, label = "chUpScale")

        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(upScale)
                .clickable(
                    interactionSource = upInteraction,
                    indication = null,
                    onClick = onUpClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Kanal +",
                tint = if (upPressed) VestelRed else Color.White.copy(alpha = 0.88f),
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.55f)
        ) {
            HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                ),
                color = Color.White.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.08f))
        }

        val downInteraction = remember { MutableInteractionSource() }
        val downPressed by downInteraction.collectIsPressedAsState()
        val downScale by animateFloatAsState(if (downPressed) 0.86f else 1f, label = "chDownScale")

        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(downScale)
                .clickable(
                    interactionSource = downInteraction,
                    indication = null,
                    onClick = onDownClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Kanal -",
                tint = if (downPressed) VestelRed else Color.White.copy(alpha = 0.88f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

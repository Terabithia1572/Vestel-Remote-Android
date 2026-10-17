package com.yunusinan.vestelremote.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.domain.model.TvCommand
import com.yunusinan.vestelremote.ui.theme.DarkSurfaceElevated
import com.yunusinan.vestelremote.ui.theme.VestelRed

@Composable
fun RockerPill(
    label: String,
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(68.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(DarkSurfaceElevated)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper button (+)
        val upInteraction = remember { MutableInteractionSource() }
        val upPressed by upInteraction.collectIsPressedAsState()
        val upScale by animateFloatAsState(if (upPressed) 0.85f else 1f, label = "upScale")

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
                contentDescription = "$label Up",
                tint = if (upPressed) VestelRed else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(26.dp)
            )
        }

        // Center Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 11.sp
        )

        // Lower button (-)
        val downInteraction = remember { MutableInteractionSource() }
        val downPressed by downInteraction.collectIsPressedAsState()
        val downScale by animateFloatAsState(if (downPressed) 0.85f else 1f, label = "downScale")

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
                contentDescription = "$label Down",
                tint = if (downPressed) VestelRed else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(26.dp)
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
    Column(
        modifier = modifier
            .width(68.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(DarkSurfaceElevated)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val upInteraction = remember { MutableInteractionSource() }
        val upPressed by upInteraction.collectIsPressedAsState()
        val upScale by animateFloatAsState(if (upPressed) 0.85f else 1f, label = "chUpScale")

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
                contentDescription = "Channel Up",
                tint = if (upPressed) VestelRed else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 11.sp
        )

        val downInteraction = remember { MutableInteractionSource() }
        val downPressed by downInteraction.collectIsPressedAsState()
        val downScale by animateFloatAsState(if (downPressed) 0.85f else 1f, label = "chDownScale")

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
                contentDescription = "Channel Down",
                tint = if (downPressed) VestelRed else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

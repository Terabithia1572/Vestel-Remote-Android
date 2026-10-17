package com.yunusinan.vestelremote.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.ui.theme.DarkBorder
import com.yunusinan.vestelremote.ui.theme.DarkSurfaceElevated
import com.yunusinan.vestelremote.ui.theme.DarkTextMuted
import com.yunusinan.vestelremote.ui.theme.VestelDesignTokens
import com.yunusinan.vestelremote.ui.theme.VestelRed

@Composable
fun TouchpadControl(
    onSwipeDelta: (dx: Float, dy: Float) -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var touchPos by remember { mutableStateOf<Offset?>(null) }
    var accumulatedDx by remember { mutableFloatStateOf(0f) }
    var accumulatedDy by remember { mutableFloatStateOf(0f) }

    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(VestelDesignTokens.Dimensions.touchpadHeight)
            .clip(shape)
            .background(DarkSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (touchPos != null) VestelRed.copy(alpha = 0.45f) else DarkBorder,
                shape = shape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onTap()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        touchPos = offset
                        accumulatedDx = 0f
                        accumulatedDy = 0f
                    },
                    onDragEnd = {
                        touchPos = null
                    },
                    onDragCancel = {
                        touchPos = null
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        touchPos = change.position
                        accumulatedDx += dragAmount.x
                        accumulatedDy += dragAmount.y

                        val threshold = 32f
                        if (kotlin.math.abs(accumulatedDx) >= threshold || kotlin.math.abs(accumulatedDy) >= threshold) {
                            onSwipeDelta(accumulatedDx, accumulatedDy)
                            accumulatedDx = 0f
                            accumulatedDy = 0f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.5.dp.toPx()
            val inset = 16.dp.toPx()
            val cornerLen = 18.dp.toPx()
            val accentColor = Color.White.copy(alpha = 0.14f)

            // Top-left corner accent
            drawLine(accentColor, Offset(inset, inset), Offset(inset + cornerLen, inset), strokeWidth)
            drawLine(accentColor, Offset(inset, inset), Offset(inset, inset + cornerLen), strokeWidth)

            // Top-right corner accent
            drawLine(accentColor, Offset(size.width - inset, inset), Offset(size.width - inset - cornerLen, inset), strokeWidth)
            drawLine(accentColor, Offset(size.width - inset, inset), Offset(size.width - inset, inset + cornerLen), strokeWidth)

            // Bottom-left corner accent
            drawLine(accentColor, Offset(inset, size.height - inset), Offset(inset + cornerLen, size.height - inset), strokeWidth)
            drawLine(accentColor, Offset(inset, size.height - inset), Offset(inset, size.height - inset - cornerLen), strokeWidth)

            // Bottom-right corner accent
            drawLine(accentColor, Offset(size.width - inset, size.height - inset), Offset(size.width - inset - cornerLen, size.height - inset), strokeWidth)
            drawLine(accentColor, Offset(size.width - inset, size.height - inset), Offset(size.width - inset, size.height - inset - cornerLen), strokeWidth)

            // Draw finger follower ripple if active
            touchPos?.let { pos ->
                drawCircle(
                    color = VestelRed.copy(alpha = 0.22f),
                    radius = 28.dp.toPx(),
                    center = pos
                )
                drawCircle(
                    color = VestelRed,
                    radius = 7.dp.toPx(),
                    center = pos
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.touchpad_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.88f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.touchpad_hint),
                style = MaterialTheme.typography.bodySmall,
                color = DarkTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

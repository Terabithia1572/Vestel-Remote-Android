package com.yunusinan.vestelremote.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.ui.theme.DarkBorder
import com.yunusinan.vestelremote.ui.theme.DarkSurfaceElevated
import com.yunusinan.vestelremote.ui.theme.DarkTextMuted
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceElevated)
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

                        // Trigger step when threshold reached
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
            // Draw subtle corner accents
            val strokeWidth = 1.5.dp.toPx()
            val cornerLen = 20.dp.toPx()
            val color = Color.White.copy(alpha = 0.08f)

            // Top-left
            drawLine(color, Offset(16.dp.toPx(), 16.dp.toPx()), Offset(16.dp.toPx() + cornerLen, 16.dp.toPx()), strokeWidth)
            drawLine(color, Offset(16.dp.toPx(), 16.dp.toPx()), Offset(16.dp.toPx(), 16.dp.toPx() + cornerLen), strokeWidth)

            // Top-right
            drawLine(color, Offset(size.width - 16.dp.toPx(), 16.dp.toPx()), Offset(size.width - 16.dp.toPx() - cornerLen, 16.dp.toPx()), strokeWidth)
            drawLine(color, Offset(size.width - 16.dp.toPx(), 16.dp.toPx()), Offset(size.width - 16.dp.toPx(), 16.dp.toPx() + cornerLen), strokeWidth)

            // Draw finger follower dot if active
            touchPos?.let { pos ->
                drawCircle(
                    color = VestelRed.copy(alpha = 0.25f),
                    radius = 28.dp.toPx(),
                    center = pos
                )
                drawCircle(
                    color = VestelRed,
                    radius = 8.dp.toPx(),
                    center = pos
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.touchpad_title),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.touchpad_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = DarkTextMuted,
                fontSize = 12.sp
            )
        }
    }
}

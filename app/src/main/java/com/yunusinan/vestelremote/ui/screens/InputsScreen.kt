package com.yunusinan.vestelremote.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.TvInput
import com.yunusinan.vestelremote.ui.theme.*

@Composable
fun InputsScreen(
    currentInputName: String,
    onSelectInput: (TvInput) -> Unit,
    modifier: Modifier = Modifier
) {
    val inputs = TvInput.DEFAULT_INPUTS

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VestelDesignTokens.Gradients.screenBackground)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.nav_inputs),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(inputs, key = { it.id }) { input ->
            val effectiveInputName = if (currentInputName.isBlank() || currentInputName.equals("Seçilmedi", ignoreCase = true)) {
                "HDMI 1"
            } else {
                currentInputName
            }
            val isSelected = input.name.equals(effectiveInputName, ignoreCase = true)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceElevated)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) VestelRed.copy(alpha = 0.45f) else DarkBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelectInput(input) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InputSourceIcon(type = input.type)

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = input.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(VestelRed.copy(alpha = 0.85f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aktif",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = DarkTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InputSourceIcon(type: TvInput.InputType) {
    Box(
        modifier = Modifier.width(30.dp).height(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            TvInput.InputType.HDMI -> {
                // Custom trapezoid HDMI receptacle outline matching reference
                Canvas(modifier = Modifier.width(24.dp).height(14.dp)) {
                    val w = size.width
                    val h = size.height
                    val path = Path().apply {
                        moveTo(w * 0.08f, h * 0.15f)
                        lineTo(w * 0.92f, h * 0.15f)
                        lineTo(w * 0.92f, h * 0.55f)
                        lineTo(w * 0.76f, h * 0.88f)
                        lineTo(w * 0.24f, h * 0.88f)
                        lineTo(w * 0.08f, h * 0.55f)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.9f),
                        style = Stroke(width = 1.6.dp.toPx(), join = StrokeJoin.Round)
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.85f),
                        start = Offset(w * 0.3f, h * 0.48f),
                        end = Offset(w * 0.7f, h * 0.48f),
                        strokeWidth = 1.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
            TvInput.InputType.AV -> {
                // Three colored RCA rings (Red, Yellow, White) matching reference
                Canvas(modifier = Modifier.width(28.dp).height(12.dp)) {
                    val r = 3.5.dp.toPx()
                    val stroke = Stroke(width = 1.7.dp.toPx())
                    val cy = size.height / 2f
                    drawCircle(
                        color = Color(0xFFFF3B30),
                        radius = r,
                        center = Offset(r + 1.dp.toPx(), cy),
                        style = stroke
                    )
                    drawCircle(
                        color = Color(0xFFFFCC00),
                        radius = r,
                        center = Offset(size.width / 2f, cy),
                        style = stroke
                    )
                    drawCircle(
                        color = Color.White,
                        radius = r,
                        center = Offset(size.width - r - 1.dp.toPx(), cy),
                        style = stroke
                    )
                }
            }
            TvInput.InputType.SATELLITE -> {
                Icon(
                    imageVector = Icons.Default.SettingsInputAntenna,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(22.dp)
                )
            }
            TvInput.InputType.CABLE, TvInput.InputType.LIVE_TV -> {
                Icon(
                    imageVector = Icons.Default.LiveTv,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(22.dp)
                )
            }
            TvInput.InputType.ANTENNA -> {
                Icon(
                    imageVector = Icons.Default.Podcasts,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(22.dp)
                )
            }
            TvInput.InputType.SCREEN_SHARE -> {
                Icon(
                    imageVector = Icons.Default.Cast,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}


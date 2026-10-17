package com.yunusinan.vestelremote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.SettingsInputHdmi
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
            .padding(horizontal = VestelDesignTokens.Spacing.lg, vertical = VestelDesignTokens.Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = stringResource(R.string.inputs_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.inputs_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTextSecondary
                )
            }
        }

        items(inputs, key = { it.id }) { input ->
            val isSelected = input.name.equals(currentInputName, ignoreCase = true)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) VestelRed.copy(alpha = 0.45f) else DarkBorder,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onSelectInput(input) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) VestelRed.copy(alpha = 0.14f) else DarkSurfaceElevated
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (input.type) {
                        TvInput.InputType.HDMI -> Icons.Default.SettingsInputHdmi
                        TvInput.InputType.LIVE_TV -> Icons.Default.LiveTv
                        TvInput.InputType.AV -> Icons.Default.Tv
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) VestelRed.copy(alpha = 0.22f)
                                else Color.White.copy(alpha = 0.06f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = input.name,
                            tint = if (isSelected) VestelRed else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = input.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val subtitle = when (input.id) {
                            "hdmi_2" -> "eARC / ARC Ses ve 4K Görüntü Girişi"
                            "live_tv" -> "Dahili Uydu / DVB-T2/C/S2 Kanal Girişi"
                            "av", "av_in" -> "Analog Kompozit / Yan Bağlantı Girişi"
                            else -> "Harici 4K UHD HDMI Kaynak Girişi"
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Seçili",
                            tint = StatusGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = DarkTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

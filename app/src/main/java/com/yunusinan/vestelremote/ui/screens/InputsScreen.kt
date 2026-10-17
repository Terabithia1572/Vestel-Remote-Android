package com.yunusinan.vestelremote.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.inputs_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(inputs, key = { it.id }) { input ->
            val isSelected = input.name.equals(currentInputName, ignoreCase = true)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSelectInput(input) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) VestelRed.copy(alpha = 0.15f) else DarkSurfaceElevated
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (input.type) {
                        TvInput.InputType.HDMI -> Icons.Default.SettingsInputHdmi
                        TvInput.InputType.LIVE_TV -> Icons.Default.LiveTv
                        TvInput.InputType.AV -> Icons.Default.Tv
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = input.name,
                        tint = if (isSelected) VestelRed else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = input.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (input.id == "hdmi_2") "eARC Destekli Ses & Görüntü Portu" else "Vestel TV Giriş Kaynağı",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkTextMuted,
                            fontSize = 12.sp
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = StatusGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

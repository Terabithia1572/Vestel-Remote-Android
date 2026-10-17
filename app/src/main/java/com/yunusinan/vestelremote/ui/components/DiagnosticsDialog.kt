package com.yunusinan.vestelremote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.TvDiagnostics
import com.yunusinan.vestelremote.ui.theme.DarkSurfaceElevated
import com.yunusinan.vestelremote.ui.theme.DarkTextSecondary
import com.yunusinan.vestelremote.ui.theme.StatusGreen
import com.yunusinan.vestelremote.ui.theme.StatusRed
import com.yunusinan.vestelremote.ui.theme.VestelRed

@Composable
fun DiagnosticsDialog(
    diagnostics: TvDiagnostics,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = DarkSurfaceElevated,
        title = {
            Text(
                text = "Ağ ve Protokol Teşhisi",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Hedef IP: ${diagnostics.targetIp}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                DiagRow("Ping Erişimi", if (diagnostics.pingSuccess) "${diagnostics.pingLatencyMs} ms" else "Yanıt yok", diagnostics.pingSuccess)
                DiagRow("Android TV Remote (Port 6466)", if (diagnostics.remotePort6466Open) "Açık (TCP)" else "Kapalı", diagnostics.remotePort6466Open)
                DiagRow("Eşleştirme Servisi (Port 6467)", if (diagnostics.pairingPort6467Open) "Açık (TCP)" else "Kapalı", diagnostics.pairingPort6467Open)
                DiagRow("DIAL Uygulama Servisi (Port 8008)", if (diagnostics.dialPort8008Open) "Açık (HTTP)" else "Kapalı", diagnostics.dialPort8008Open)
                DiagRow("Google Cast (Port 8009)", if (diagnostics.castPort8009Open) "Açık (TCP)" else "Kapalı", diagnostics.castPort8009Open)
                DiagRow("ADB Servisi (Port 5555)", if (diagnostics.adbPort5555Open) "Açık" else "Kapalı", diagnostics.adbPort5555Open)

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Değerlendirme: ${diagnostics.summary} TLS bu port taramasında sınanmadı.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = VestelRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tamam", color = Color.White)
            }
        }
    )
}

@Composable
private fun DiagRow(label: String, value: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = if (isOk) StatusGreen else StatusRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}


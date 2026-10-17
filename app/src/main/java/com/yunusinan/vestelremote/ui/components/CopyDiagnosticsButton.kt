package com.yunusinan.vestelremote.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yunusinan.vestelremote.domain.model.ConnectionDiagnostic
import com.yunusinan.vestelremote.ui.theme.DarkBorder
import com.yunusinan.vestelremote.ui.theme.StatusGreen

@Composable
fun CopyDiagnosticsButton(
    attempts: List<ConnectionDiagnostic>,
    isMockMode: Boolean,
    modifier: Modifier = Modifier,
    compactLabel: Boolean = false
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = {
            clipboard.setText(AnnotatedString(buildString {
                appendLine("Vestel Remote / pairing-audit-20261017 / mockMode=$isMockMode")
                appendLine("Uygulama oturumundaki son 20 bağlantı denemesi; PIN/anahtar/paket içeriği kaydedilmez.")
                if (attempts.isEmpty()) appendLine("Henüz gerçek bağlantı denemesi yok.")
                attempts.forEach { appendLine(it.export()); appendLine() }
            }))
            copied = true
        },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (copied) StatusGreen else Color.White.copy(alpha = 0.9f)
        )
    ) {
        Icon(
            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (copied) StatusGreen else Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when {
                copied -> "Teşhis Kopyalandı"
                compactLabel -> "Teşhisi Kopyala"
                else -> "Bağlantı Teşhisini Kopyala"
            },
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

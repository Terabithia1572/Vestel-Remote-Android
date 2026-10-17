package com.yunusinan.vestelremote.ui.components

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.yunusinan.vestelremote.domain.model.ConnectionDiagnostic

@Composable
fun CopyDiagnosticsButton(attempts: List<ConnectionDiagnostic>, isMockMode: Boolean) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    OutlinedButton(onClick = {
        clipboard.setText(AnnotatedString(buildString {
            appendLine("Vestel Remote / pairing-audit-20261017 / mockMode=$isMockMode")
            appendLine("Uygulama oturumundaki son 20 bağlantı denemesi; PIN/anahtar/paket içeriği kaydedilmez.")
            if (attempts.isEmpty()) appendLine("Henüz gerçek bağlantı denemesi yok.")
            attempts.forEach { appendLine(it.export()); appendLine() }
        }))
        copied = true
    }) {
        Text(if (copied) "Teşhis kopyalandı" else "Teşhisi Kopyala (Copy Diagnostics)")
    }
}

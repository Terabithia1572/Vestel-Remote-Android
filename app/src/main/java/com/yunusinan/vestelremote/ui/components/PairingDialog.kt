package com.yunusinan.vestelremote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.ui.theme.DarkSurfaceElevated
import com.yunusinan.vestelremote.ui.theme.DarkTextSecondary
import com.yunusinan.vestelremote.ui.theme.VestelRed

@Composable
fun PairingDialog(
    tvName: String,
    onConfirm: (pin: String) -> Unit,
    onDismiss: () -> Unit
) {
    var pinText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = DarkSurfaceElevated,
        title = {
            Text(
                text = stringResource(R.string.pairing_code_prompt),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$tvName ekranında beliren 6 haneli güvenlik kodunu aşağıya giriniz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pinText,
                    onValueChange = { input ->
                        val normalized = input.uppercase().filter { it in '0'..'9' || it in 'A'..'F' }
                        if (normalized.length <= 6) pinText = normalized
                    },
                    placeholder = { Text("Örn: 8C4E19", color = Color.Gray) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center,
                        letterSpacing = 4.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VestelRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = VestelRed
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(pinText.trim()) },
                enabled = pinText.trim().length == 6,
                colors = ButtonDefaults.buttonColors(containerColor = VestelRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.confirm), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = DarkTextSecondary)
            }
        }
    )
}

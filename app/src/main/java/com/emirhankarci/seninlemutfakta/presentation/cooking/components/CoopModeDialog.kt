package com.emirhankarci.seninlemutfakta.presentation.cooking.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CoopModeDialog(
    recipeName: String,
    onDismiss: () -> Unit,
    onSoloMode: () -> Unit,
    onCoopMode: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = "👫",
                style = MaterialTheme.typography.displayLarge
            )
        },
        title = {
            Text(
                text = "Birlikte mi yapmak istersiniz?",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = recipeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Eşinizle birlikte yaparsanız:",
                    style = MaterialTheme.typography.labelLarge
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BulletPoint("İkiniz de farklı adımları göreceksiniz")
                    BulletPoint("Her biriniz kendi işinizi yapacaksınız")
                    BulletPoint("Adımlar senkronize ilerleyecek")
                    BulletPoint("Daha eğlenceli olacak! 😊")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCoopMode,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("👫 Birlikte Yapalım!")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onSoloMode,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("👤 Tek Başıma")
            }
        }
    )
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
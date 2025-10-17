package com.emirhankarci.seninlemutfakta.presentation.cooking.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WaitingForPartnerDialog(
    recipeName: String,
    partnerName: String = "Eşiniz",
    onCancel: () -> Unit,
    onJoin: () -> Unit
) {
    // Animasyon için
    val infiniteTransition = rememberInfiniteTransition(label = "waiting")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    AlertDialog(
        onDismissRequest = { /* Dismiss yapma, cancel butonu var */ },
        icon = {
            Text(
                text = "⏳",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.alpha(alpha)
            )
        },
        title = {
            Text(
                text = "$partnerName sizi bekliyor!",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = recipeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Divider()

                Text(
                    text = "$partnerName tarifi başlattı ve sizi bekliyor. Hazır olduğunuzda katılın!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onJoin,  // ← DEĞİŞTİR
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🎯 Katıl ve Başla!")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("İptal")
            }
        }
    )
}
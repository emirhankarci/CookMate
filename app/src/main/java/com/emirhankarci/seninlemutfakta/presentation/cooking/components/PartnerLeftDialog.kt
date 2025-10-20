package com.emirhankarci.seninlemutfakta.presentation.cooking.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PartnerLeftDialog(
    onBackToRecipes: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Kapatılamaz */ },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFFFFF9F5),
        icon = {
            Text(text = "😢", fontSize = 64.sp)
        },
        title = {
            Text(
                text = "Eşiniz Ayrıldı",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Eşiniz pişirme oturumundan ayrıldı.",
                    fontSize = 14.sp,
                    color = Color(0xFF95A5A6),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Text(
                    text = "Oturum sonlandırıldı.",
                    fontSize = 14.sp,
                    color = Color(0xFF95A5A6),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onBackToRecipes,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF69B4)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ana Sayfaya Dön",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    )
}


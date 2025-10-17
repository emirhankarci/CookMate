package com.emirhankarci.seninlemutfakta.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.emirhankarci.seninlemutfakta.data.model.Gender

@Composable
fun UserSelectionScreen(
    onUserSelected: (userId: String, gender: Gender, coupleId: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "👥 Test Kullanıcı Seç",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "İki cihazda test için farklı kullanıcı seçin",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider()

        // Çift 1 - Kullanıcı 1 (Kadın)
        Button(
            onClick = {
                onUserSelected(
                    "user_female_001",
                    Gender.FEMALE,
                    "couple_001"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("👩 Ayşe (Kadın)", style = MaterialTheme.typography.titleMedium)
                Text("Çift: Ahmet & Ayşe", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Çift 1 - Kullanıcı 2 (Erkek)
        Button(
            onClick = {
                onUserSelected(
                    "user_male_001",
                    Gender.MALE,
                    "couple_001"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("👨 Ahmet (Erkek)", style = MaterialTheme.typography.titleMedium)
                Text("Çift: Ahmet & Ayşe", style = MaterialTheme.typography.bodySmall)
            }
        }

        Divider()

        Text(
            text = "💡 İpucu: Emülatör 1'de Ayşe, Emülatör 2'de Ahmet seçin",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
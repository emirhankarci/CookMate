package com.emirhankarci.seninlemutfakta.presentation.cooking.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emirhankarci.seninlemutfakta.data.model.Gender

@Composable
fun GenderSelectionScreen(
    onGenderSelected: (Gender) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "Kim yemek yapıyor?",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = { onGenderSelected(Gender.FEMALE) },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Text(
                text = "👩‍🍳 Kadın",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Button(
            onClick = { onGenderSelected(Gender.MALE) },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Text(
                text = "👨‍🍳 Erkek",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
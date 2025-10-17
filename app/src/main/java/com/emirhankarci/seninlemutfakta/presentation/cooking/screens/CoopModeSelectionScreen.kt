package com.emirhankarci.seninlemutfakta.presentation.cooking.screens

import androidx.compose.runtime.Composable
import com.emirhankarci.seninlemutfakta.presentation.cooking.components.CoopModeDialog

@Composable
fun CoopModeSelectionScreen(
    recipeName: String,
    onSoloMode: () -> Unit,
    onCoopMode: () -> Unit
) {
    CoopModeDialog(
        recipeName = recipeName,
        onDismiss = onSoloMode,
        onSoloMode = onSoloMode,
        onCoopMode = onCoopMode
    )
}

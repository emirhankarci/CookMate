package com.emirhankarci.seninlemutfakta.presentation.cooking.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.emirhankarci.seninlemutfakta.data.model.Gender
import com.emirhankarci.seninlemutfakta.data.model.SessionStatus
import com.emirhankarci.seninlemutfakta.presentation.cooking.CookingSessionEvent
import com.emirhankarci.seninlemutfakta.presentation.cooking.CookingSessionState
import com.emirhankarci.seninlemutfakta.presentation.cooking.PartnerConnectionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingSessionScreen(
    state: CookingSessionState,
    onEvent: (CookingSessionEvent) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.recipe?.titleTurkish ?: state.recipe?.title ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Adım ${(state.session?.currentStep ?: 0) + 1} / ${state.session?.totalSteps ?: 0}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Çık")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    ErrorScreen(
                        error = state.error,
                        onRetry = { onEvent(CookingSessionEvent.ClearError) }
                    )
                }

                state.session == null -> {
                    Text(
                        text = "Session yükleniyor...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.session.status == SessionStatus.WAITING -> {
                    WaitingForPartnerContent(
                        recipeName = state.recipe?.titleTurkish ?: state.recipe?.title ?: "",
                        onBack = onBack
                    )
                }

                state.currentStep == null -> {
                    Text(
                        text = "Tarif yükleniyor...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    CookingContent(
                        state = state,
                        onEvent = onEvent
                    )
                }
            }
        }
    }

    // Completion Dialog
    if (state.showCompletionDialog) {
        CompletionDialog(
            onDismiss = { onEvent(CookingSessionEvent.DismissCompletionDialog) },
            onBackToRecipes = onBack
        )
    }
}

@Composable
fun CookingContent(
    state: CookingSessionState,
    onEvent: (CookingSessionEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Connection Warning
        if (!state.isConnected) {
            ConnectionWarningCard()
        }

        // Partner Disconnected Warning
        if (state.session?.isCoopMode == true &&
            state.partnerConnectionStatus == PartnerConnectionStatus.DISCONNECTED) {
            PartnerDisconnectedCard()
        }

        // Progress Bar
        ProgressSection(
            progress = state.getProgressPercentage(),
            currentStep = (state.session?.currentStep ?: 0) + 1,
            totalSteps = state.session?.totalSteps ?: 0
        )

        // Partner Status (Coop Mode'da)
        if (state.session?.isCoopMode == true) {
            PartnerStatusCard(
                partnerStatus = state.partnerConnectionStatus,
                isPartnerWaiting = state.isPartnerWaiting(),
                isPartnerCompleted = state.isPartnerStepCompleted(),
                statusMessage = state.getPartnerStatusMessage()
            )
        }

        // Current Step Card
        CurrentStepCard(
            step = state.currentStep!!,
            stepNumber = (state.session?.currentStep ?: 0) + 1,
            isCoopMode = state.session?.isCoopMode ?: false,  // ← EKLE
            currentUserGender = state.currentUserGender  // ← EKLE
        )

        Spacer(modifier = Modifier.weight(1f))

        // My Status
        MyStatusCard(
            isCompleted = state.isMyStepCompleted(),
            isWaiting = state.amIWaiting()
        )

        // Complete Button
        CompleteStepButton(
            isCompleted = state.isMyStepCompleted(),
            isWaiting = state.amIWaiting(),
            canProceed = state.canProceedToNextStep(),
            onClick = { onEvent(CookingSessionEvent.CompleteCurrentStep) }
        )
    }
}

@Composable
fun ProgressSection(
    progress: Int,
    currentStep: Int,
    totalSteps: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "İlerleme",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "%$progress",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        LinearProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
fun PartnerStatusCard(
    partnerStatus: PartnerConnectionStatus,
    isPartnerWaiting: Boolean,
    isPartnerCompleted: Boolean,
    statusMessage: String
) {
    val backgroundColor = when {
        partnerStatus != PartnerConnectionStatus.ONLINE -> Color(0xFFFFEBEE)
        isPartnerCompleted -> Color(0xFFE8F5E9)
        isPartnerWaiting -> Color(0xFFFFF9C4)
        else -> Color(0xFFE3F2FD)
    }

    val icon = when {
        partnerStatus != PartnerConnectionStatus.ONLINE -> "⚠️"
        isPartnerCompleted -> "✅"
        isPartnerWaiting -> "⏳"
        else -> "👨‍🍳"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Column {
                Text(
                    text = "Eşiniz",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun CurrentStepCard(
    step: com.emirhankarci.seninlemutfakta.data.model.RecipeStep,
    stepNumber: Int,
    isCoopMode: Boolean = false,
    currentUserGender: Gender? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Step Number
            Text(
                text = "Adım $stepNumber",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // Coop Mode'da görev göstergesi
            if (isCoopMode && currentUserGender != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentUserGender == Gender.FEMALE) "👩‍🍳" else "👨‍🍳",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Sizin Göreviniz",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Divider()
            }

            // Step Description
            Text(
                text = step.description,
                style = MaterialTheme.typography.titleLarge
            )

            // Estimated Time
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱️",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${step.estimatedTime} dakika",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Tips
            if (step.tips.isNotEmpty()) {
                Divider()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💡",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = step.tips,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MyStatusCard(
    isCompleted: Boolean,
    isWaiting: Boolean
) {
    val backgroundColor = when {
        isCompleted && isWaiting -> Color(0xFFFFF9C4)
        isCompleted -> Color(0xFFE8F5E9)
        else -> Color(0xFFE3F2FD)
    }

    val statusText = when {
        isCompleted && isWaiting -> "✅ Tamamlandı - Eşinizi bekliyorsunuz"
        isCompleted -> "✅ Bu adımı tamamladınız"
        else -> "👨‍🍳 Adımı tamamlayın"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Text(
            text = statusText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CompleteStepButton(
    isCompleted: Boolean,
    isWaiting: Boolean,
    canProceed: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isCompleted,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = when {
                isWaiting -> "Eşinizi bekliyorsunuz... ⏳"
                isCompleted -> "Tamamlandı ✅"
                else -> "Adımı Tamamla"
            },
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bir hata oluştu",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Tekrar Dene")
        }
    }
}

@Composable
fun CompletionDialog(
    onDismiss: () -> Unit,
    onBackToRecipes: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.displayMedium
            )
        },
        title = {
            Text(
                text = "Tebrikler!",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Tarifi başarıyla tamamladınız!\nAfiyet olsun! 😋",
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onBackToRecipes()
                }
            ) {
                Text("Tariflere Dön")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

@Composable
fun ConnectionWarningCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.headlineMedium
            )
            Column {
                Text(
                    text = "Bağlantı Kesildi",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "İnternet bağlantınızı kontrol edin",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun PartnerDisconnectedCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏸️",
                style = MaterialTheme.typography.headlineMedium
            )
            Column {
                Text(
                    text = "Eşiniz Bağlantısını Kaybetti",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "30 saniyedir çevrimdışı. Lütfen bekleyin.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun WaitingForPartnerContent(
    recipeName: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animasyonlu emoji
        Text(
            text = "⏳",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Eşinizi Bekliyorsunuz",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = recipeName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Divider()

                Text(
                    text = "Eşiniz uygulamayı açtığında size bildirim gelecek ve birlikte tarifinizi yapmaya başlayabileceksiniz.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("İptal Et ve Geri Dön")
        }
    }
}
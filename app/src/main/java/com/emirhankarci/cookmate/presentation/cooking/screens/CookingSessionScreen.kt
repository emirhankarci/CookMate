package com.emirhankarci.cookmate.presentation.cooking.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.emirhankarci.cookmate.R
import com.emirhankarci.cookmate.data.model.Gender
import com.emirhankarci.cookmate.data.model.SessionStatus
import com.emirhankarci.cookmate.presentation.cooking.CookingSessionEvent
import com.emirhankarci.cookmate.presentation.cooking.CookingSessionState
import com.emirhankarci.cookmate.presentation.cooking.PartnerConnectionStatus

@Composable
fun CookingSessionScreen(
    state: CookingSessionState,
    onEvent: (CookingSessionEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFFF69B4)
                )
            }

            state.error != null -> {
                ErrorScreen(
                    error = state.error,
                    onRetry = { onEvent(CookingSessionEvent.ClearError) },
                    onBack = onBack
                )
            }

            state.session == null -> {
                LoadingContent(message = "Loading session...")
            }

            state.session.status == SessionStatus.WAITING && state.session.isCoopMode -> {
                WaitingForPartnerContent(
                    recipeName = state.recipe?.titleTurkish ?: state.recipe?.title ?: "",
                    onBack = onBack
                )
            }

            state.currentStep == null -> {
                LoadingContent(message = "Loading recipe...")
            }

            else -> {
                CookingContent(
                    state = state,
                    onEvent = onEvent,
                    onBack = onBack,
                    modifier = Modifier
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingScreenHeader(
    recipeName: String,
    onBack: () -> Unit
) {
    val gradientColors = listOf(
        Color(0xFFFFB6C1),
        Color(0xFFFF69B4),
        Color(0xFFFF6B6B)
    )

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = recipeName,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier.background(Brush.linearGradient(colors = gradientColors))
    )
}


@Composable
fun CookingContent(
    state: CookingSessionState,
    onEvent: (CookingSessionEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9F5))
    ) {

        // Progress Indicator
        ProgressIndicator(
            progress = state.getProgressPercentage()
        )

        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connection Warnings
            if (!state.isConnected) {
                ConnectionWarningBanner()
            }

            if (state.session?.isCoopMode == true &&
                state.partnerConnectionStatus == PartnerConnectionStatus.DISCONNECTED) {
                PartnerDisconnectedBanner()
            }

            // Partner Status Card (Compact, Floating Design)
            if (state.session?.isCoopMode == true) {
                PartnerStatusCompactCard(
                    partnerStatus = state.partnerConnectionStatus,
                    isPartnerWaiting = state.isPartnerWaiting(),
                    isPartnerCompleted = state.isPartnerStepCompleted(),
                    statusMessage = state.getPartnerStatusMessage()
                )
            }

            // Current Step Card (Main Premium Card)
            PremiumStepCard(
                step = state.currentStep!!,
                stepNumber = (state.session?.currentStep ?: 0) + 1,
                totalSteps = state.session?.totalSteps ?: 0,
                isCoopMode = state.session?.isCoopMode ?: false,
                currentUserGender = state.currentUserGender
            )

            // Sync Status Visual (Coop Mode)
            if (state.session?.isCoopMode == true) {
                SyncStatusVisual(
                    myCompleted = state.isMyStepCompleted(),
                    partnerCompleted = state.isPartnerStepCompleted(),
                    currentUserGender = state.currentUserGender
                )
            }

            // Action Buttons
            ActionButtons(
                isMyStepCompleted = state.isMyStepCompleted(),
                isWaiting = state.amIWaiting(),
                canProceed = state.canProceedToNextStep(),
                isCoopMode = state.session?.isCoopMode ?: false,
                onComplete = { onEvent(CookingSessionEvent.CompleteCurrentStep) }
            )

            // Bottom spacing for safe area
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProgressIndicator(progress: Int) {
    LinearProgressIndicator(
        progress = { progress / 100f },
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp),
        color = Color(0xFFFF69B4),
        trackColor = Color(0xFFE0E0E0),
    )
}

@Composable
fun PartnerStatusCompactCard(
    partnerStatus: PartnerConnectionStatus,
    isPartnerWaiting: Boolean,
    isPartnerCompleted: Boolean,
    statusMessage: String
) {
    val backgroundColor = when {
        partnerStatus != PartnerConnectionStatus.ONLINE -> Color(0xFFFFEBEE)
        isPartnerCompleted -> Color(0xFFE8F5E9)
        isPartnerWaiting -> Color(0xFFFFF9C4)
        else -> Color.White
    }

    val statusIcon = when {
        partnerStatus != PartnerConnectionStatus.ONLINE -> "🔴"
        isPartnerCompleted -> "✅"
        isPartnerWaiting -> "⏳"
        else -> "🟢"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "👨‍🍳", fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Partner",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF95A5A6)
                )
                Text(
                    text = statusMessage,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C3E50)
                )
            }
            Text(text = statusIcon, fontSize = 20.sp)
        }
    }
}

@Composable
fun PremiumStepCard(
    step: com.emirhankarci.cookmate.data.model.RecipeStep,
    stepNumber: Int,
    totalSteps: Int,
    isCoopMode: Boolean,
    currentUserGender: Gender?
) {
    val isFinalStep = stepNumber == totalSteps

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lottie Animation based on step content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFE8F0),
                                Color(0xFFFFD0E0)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Determine animation based on step description
                val animationRes = when {
                    step.description.contains("wash", ignoreCase = true) || 
                    step.description.contains("clean", ignoreCase = true) -> R.raw.wash_the_veggies
                    
                    step.description.contains("cut", ignoreCase = true) || 
                    step.description.contains("chop", ignoreCase = true) || 
                    step.description.contains("dice", ignoreCase = true) -> R.raw.vegetable_cutting
                    
                    step.description.contains("heat", ignoreCase = true) || 
                    step.description.contains("pan", ignoreCase = true) -> R.raw.heat_the_pan
                    
                    step.description.contains("add", ignoreCase = true) || 
                    step.description.contains("put", ignoreCase = true) -> R.raw.add_food
                    
                    step.description.contains("sauté", ignoreCase = true) || 
                    step.description.contains("saute", ignoreCase = true) || 
                    step.description.contains("stir", ignoreCase = true) -> R.raw.saute_food
                    
                    step.description.contains("tomato", ignoreCase = true) -> R.raw.tomato
                    
                    step.description.contains("cook", ignoreCase = true) || 
                    step.description.contains("simmer", ignoreCase = true) -> R.raw.cook_the_food
                    
                    currentUserGender == Gender.MALE -> R.raw.man_cooking
                    currentUserGender == Gender.FEMALE -> R.raw.woman_cooking
                    else -> R.raw.cook_the_food
                }
                
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever
                )
                
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(180.dp)
                )
            }

            // Step Number Badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isFinalStep) Color(0xFF4CAF50) else Color(0xFFFF69B4),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isFinalStep) "🎉" else "$stepNumber",
                            fontSize = if (isFinalStep) 20.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Column {
                    Text(
                        text = if (isFinalStep) "Final Step!" else "Step $stepNumber",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50)
                    )
                }
            }

            // Step Description
            Text(
                text = step.description,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C3E50),
                lineHeight = 26.sp
            )

            // Tips Section (if available)
            if (step.tips.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8F9FA)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "💡", fontSize = 18.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tip",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C3E50)
                            )
                            Text(
                                text = step.tips,
                                fontSize = 13.sp,
                                color = Color(0xFF95A5A6),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepDetailBadge(icon: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 14.sp)
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF95A5A6)
        )
    }
}

@Composable
fun SyncStatusVisual(
    myCompleted: Boolean,
    partnerCompleted: Boolean,
    currentUserGender: Gender?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // My Progress
            SyncProfileRing(
                emoji = if (currentUserGender == Gender.FEMALE) "👩‍🍳" else "👨‍🍳",
                label = "You",
                isCompleted = myCompleted
            )

            // Connector
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(
                        if (myCompleted && partnerCompleted) Color(0xFF4CAF50)
                        else Color(0xFFE0E0E0)
                    )
            )

            // Partner Progress
            SyncProfileRing(
                emoji = if (currentUserGender == Gender.FEMALE) "👨‍🍳" else "👩‍🍳",
                label = "Partner",
                isCompleted = partnerCompleted
            )
        }
    }
}

@Composable
fun SyncProfileRing(
    emoji: String,
    label: String,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = when {
                        isCompleted -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        else -> Color(0xFFE0E0E0).copy(alpha = 0.5f)
                    },
                    shape = CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 28.sp)
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
            color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFF95A5A6)
        )
    }
}

@Composable
fun ActionButtons(
    isMyStepCompleted: Boolean,
    isWaiting: Boolean,
    canProceed: Boolean,
    isCoopMode: Boolean,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Primary Action Button
        PrimaryActionButton(
            isCompleted = isMyStepCompleted,
            isWaiting = isWaiting,
            canProceed = canProceed,
            isCoopMode = isCoopMode,
            onClick = onComplete
        )

        // Secondary Actions Row (Optional - for future enhancements)
        // SecondaryActionsRow()
    }
}

@Composable
fun PrimaryActionButton(
    isCompleted: Boolean,
    isWaiting: Boolean,
    canProceed: Boolean,
    isCoopMode: Boolean,
    onClick: () -> Unit
) {
    val buttonText = when {
        isWaiting -> "Waiting for Partner..."
        canProceed -> "Next Step →"
        isCompleted -> "Completed ✓"
        else -> "Complete Step"
    }

    val buttonColor = when {
        canProceed -> Color(0xFF4CAF50)
        isCompleted -> Color(0xFFE0E0E0)
        else -> Color.Transparent
    }

    val textColor = when {
        isCompleted && !canProceed -> Color(0xFF757575)
        else -> Color.White
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        enabled = !isCompleted || canProceed,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = Color(0xFFF5F5F5)
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (canProceed) 8.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isCompleted && !canProceed) {
                        Modifier.background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFB6C1),
                                    Color(0xFFFF69B4),
                                    Color(0xFFFF6B6B)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isWaiting) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF757575),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = buttonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF757575)
                    )
                }
            } else {
                Text(
                    text = buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun ConnectionWarningBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFA726)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⚠️", fontSize = 20.sp)
            Column {
                Text(
                    text = "Connection Lost",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Check your internet connection",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun PartnerDisconnectedBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFB74D)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⏸️", fontSize = 20.sp)
            Column {
                Text(
                    text = "Partner Disconnected",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Waiting for reconnection...",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun ErrorScreen(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9F5))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "❌", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Something Went Wrong",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            fontSize = 14.sp,
            color = Color(0xFF95A5A6),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF69B4)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Retry",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Go Back")
        }
    }
}

@Composable
fun LoadingContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = Color(0xFFFF69B4))
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color(0xFF95A5A6)
            )
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
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFFFFF9F5),
        icon = {
            Text(text = "🎉", fontSize = 64.sp)
        },
        title = {
            Text(
                text = "Congratulations!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "You've successfully completed the recipe!\nEnjoy your meal! 😋",
                fontSize = 14.sp,
                color = Color(0xFF95A5A6),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onBackToRecipes()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFB6C1),
                                    Color(0xFFFF69B4),
                                    Color(0xFFFF6B6B)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Back to Recipes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = Color(0xFF95A5A6)
                )
            }
        }
    )
}

@Composable
fun WaitingForPartnerContent(
    recipeName: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9F5))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⏳", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Waiting for Partner",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFE8F0)
                ) {
                    Text(
                        text = recipeName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF69B4),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Text(
                    text = "Your partner will be notified. You can start cooking together once they join!",
                    fontSize = 14.sp,
                    color = Color(0xFF95A5A6),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(48.dp),
                    color = Color(0xFFFF69B4)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Cancel & Go Back",
                fontSize = 16.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

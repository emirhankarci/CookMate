package com.emirhankarci.cookmate.presentation.cooking.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.emirhankarci.cookmate.presentation.components.ConfirmationDialog

@Composable
fun WaitingForPartnerDialog(
    recipeName: String,
    partnerName: String = "Eşiniz",
    onCancel: () -> Unit,
    onJoin: () -> Unit
) {
    // Confirmation dialog state
    var showCancelConfirmation by remember { mutableStateOf(false) }

    // Handle back button - show confirmation
    BackHandler {
        showCancelConfirmation = true
    }

    // Show confirmation dialog when back is pressed
    if (showCancelConfirmation) {
        ConfirmationDialog(
            title = "Emin Misiniz?",
            message = "Eşinizi beklemeyi iptal etmek istediğinize emin misiniz? Oturum sona erecektir.",
            confirmText = "Evet, Çık",
            dismissText = "İptal",
            onConfirm = {
                showCancelConfirmation = false
                onCancel()
            },
            onDismiss = {
                showCancelConfirmation = false
            }
        )
    }

    // Entrance animation
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialog_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dialog_alpha"
    )

    // Pulsing animation for icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_pulse"
    )

    // Button pulse animation
    val buttonPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "button_pulse"
    )

    Dialog(
        onDismissRequest = { /* Non-dismissible */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f * alpha))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .alpha(alpha),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF9F5)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 32.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFF6B6B)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔔", fontSize = 14.sp)
                            Text(
                                text = "New Invitation",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Animated Header Icon
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(iconScale)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFE8F0),
                                        Color(0xFFFFD0E0).copy(alpha = 0.5f),
                                        Color.Transparent
                                    ),
                                    center = Offset(50.dp.value, 50.dp.value),
                                    radius = 50.dp.value
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Emoji composition with sparkles
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "👨‍🍳", fontSize = 36.sp)
                            Text(text = "✨", fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title
                    Text(
                        text = "Your Partner Is Waiting! 💕",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50),
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    )

                    // Partner Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF0F5)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Partner avatar
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = Color(0xFFFFE8F0),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "👨‍🍳", fontSize = 28.sp)
                            }

                            // Partner details
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = partnerName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2C3E50)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🟢", fontSize = 10.sp)
                                    Text(
                                        text = "Online · Cooking",
                                        fontSize = 13.sp,
                                        color = Color(0xFF95A5A6)
                                    )
                                }
                            }
                        }
                    }

                    // Recipe Details
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Recipe name with emoji
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFE8F0)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🍝", fontSize = 20.sp)
                                Text(
                                    text = recipeName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFF69B4)
                                )
                            }
                        }

                        Text(
                            text = "Classic Recipe",
                            fontSize = 13.sp,
                            color = Color(0xFF95A5A6)
                        )
                    }

                    // Description
                    Text(
                        text = "Your partner has started cooking and needs you! Join now to cook together.",
                        fontSize = 15.sp,
                        color = Color(0xFF5F6368),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    // Time indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⏱️", fontSize = 14.sp)
                        Text(
                            text = "Just started cooking",
                            fontSize = 13.sp,
                            color = Color(0xFF95A5A6)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Primary Button - Join
                    Button(
                        onClick = {
                            visible = false
                            onJoin()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .scale(buttonPulse),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 12.dp,
                            pressedElevation = 6.dp
                        )
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
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🚀", fontSize = 22.sp)
                                Text(
                                    text = "Join & Start Cooking!",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Secondary Button - Maybe Later
                    OutlinedButton(
                        onClick = {
                            visible = false
                            onCancel()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE0E0E0),
                                    Color(0xFFE0E0E0)
                                )
                            )
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF757575)
                        )
                    ) {
                        Text(
                            text = "Maybe Later",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Subtle hint
                    Text(
                        text = "They're counting on you! 💪",
                        fontSize = 12.sp,
                        color = Color(0xFFBDBDBD),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

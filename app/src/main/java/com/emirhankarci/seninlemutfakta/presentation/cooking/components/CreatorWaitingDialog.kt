package com.emirhankarci.seninlemutfakta.presentation.cooking.components

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
import com.emirhankarci.seninlemutfakta.presentation.components.ConfirmationDialog

@Composable
fun CreatorWaitingDialog(
    recipeName: String,
    onCancel: () -> Unit
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

    // Rotating animation for waiting indicator
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_rotation"
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
                        color = Color(0xFF9C27B0)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⏳", fontSize = 14.sp)
                            Text(
                                text = "Waiting",
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
                                        Color(0xFFE1BEE7),
                                        Color(0xFFCE93D8).copy(alpha = 0.5f),
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
                            Text(text = "⏳", fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title
                    Text(
                        text = "Waiting for Partner... 💜",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50),
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    )

                    // Recipe Details
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Recipe name with emoji
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE1BEE7)
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
                                    color = Color(0xFF9C27B0)
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
                        text = "Your invitation has been sent! Your partner will be notified and can join you soon.",
                        fontSize = 15.sp,
                        color = Color(0xFF5F6368),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    // Waiting indicator
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF3E5F5)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⏰", fontSize = 16.sp)
                                Text(
                                    text = "Waiting for partner to join...",
                                    fontSize = 14.sp,
                                    color = Color(0xFF9C27B0),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Cancel Button
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
                            text = "Cancel Waiting",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Subtle hint
                    Text(
                        text = "Your partner will get a notification 📱",
                        fontSize = 12.sp,
                        color = Color(0xFFBDBDBD),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

package com.emirhankarci.cookmate.presentation.welcome

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.emirhankarci.cookmate.R
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    // App theme colors
    val primaryPink = Color(0xFFFF69B4)
    val lightPink = Color(0xFFFFB6C1)
    val darkGray = Color(0xFF2C3E50)

    // Current animation index (0, 1, 2)
    var currentAnimationIndex by remember { mutableStateOf(0) }

    // List of animation resources
    val animations = remember {
        listOf(
            R.raw.welcome_first,
            R.raw.welcome_second,
            R.raw.welcome_third
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top section - 65% for animations
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = currentAnimationIndex,
                    animationSpec = tween(durationMillis = 800),
                    label = "animation_crossfade"
                ) { index ->
                    LottieAnimationSection(
                        animationRes = animations[index],
                        onAnimationComplete = {
                            // Move to next animation, loop back to first after third
                            currentAnimationIndex = (currentAnimationIndex + 1) % animations.size
                        }
                    )
                }
            }

            // Bottom section - 35% for buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                // Title
                Text(
                    text = "CookMate",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkGray,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Cook with love, enjoy together.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = darkGray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Register Button
                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryPink,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Sign Up",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Login Button
                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = primaryPink
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = primaryPink
                    )
                ) {
                    Text(
                        text = "Log In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LottieAnimationSection(
    animationRes: Int,
    onAnimationComplete: () -> Unit
) {
    // Load composition
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(animationRes)
    )

    // Animation state
    var isPlaying by remember { mutableStateOf(true) }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        iterations = 1,
        speed = 0.8f // Slightly slower for smoother feel
    )

    // Detect when animation completes
    LaunchedEffect(progress) {
        if (progress >= 0.95f && composition != null) { // Trigger slightly before end
            isPlaying = false
            delay(300) // Shorter delay for smoother transition
            onAnimationComplete()
            isPlaying = true
        }
    }

    // Reset playing state when animation changes
    LaunchedEffect(animationRes) {
        isPlaying = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}

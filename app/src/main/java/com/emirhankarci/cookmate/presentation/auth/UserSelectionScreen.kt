package com.emirhankarci.cookmate.presentation.auth

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.RectangleShape
import com.airbnb.lottie.compose.*
import com.emirhankarci.cookmate.R
import com.emirhankarci.cookmate.data.model.Gender

@Composable
fun UserSelectionScreen(
    viewModel: UserSelectionViewModel,
    userId: String,
    coupleId: String,
    coupleName: String,
    onGenderSelected: (Gender) -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Observe couple data for real-time profile lock updates
    LaunchedEffect(coupleId) {
        viewModel.onEvent(UserSelectionEvent.ObserveCouple(coupleId))
    }

    // Disable back button
    BackHandler(enabled = true) {
        // Do nothing - back button is disabled
    }
    // Gradient colors
    val gradientColors = listOf(
        Color(0xFFFFB6C1), // Light Pink
        Color(0xFFFF69B4), // Hot Pink
        Color(0xFFFF6B6B)  // Coral/Light Red
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Logout butonu
        TextButton(
            onClick = onLogout,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
        ) {
            Text(
                text = "Log Out",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Icon
            Surface(
                modifier = Modifier.size(140.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.user_choice))
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = LottieConstants.IterateForever
                    )
                    
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = "Who’s Cooking?",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            // Subtitle
            Text(
                text = "Select your profile to continue",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Woman Button
            GenderButton(
                iconRes = R.drawable.female_cook_icon,
                label = "Female",
                subtitle = if (state.femaleProfileLocked) "In use on another device" else "Ready to work your magic?",
                isLocked = state.femaleProfileLocked,
                onClick = {
                    if (!state.femaleProfileLocked) {
                        viewModel.onEvent(UserSelectionEvent.SelectGender(Gender.FEMALE, userId))
                        onGenderSelected(Gender.FEMALE)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Man Button
            GenderButton(
                iconRes = R.drawable.male_cook_icon,
                label = "Male",
                subtitle = if (state.maleProfileLocked) "In use on another device" else "Get ready to work your magic",
                isLocked = state.maleProfileLocked,
                onClick = {
                    if (!state.maleProfileLocked) {
                        viewModel.onEvent(UserSelectionEvent.SelectGender(Gender.MALE, userId))
                        onGenderSelected(Gender.MALE)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Emergency unlock button (only show if both profiles are locked)
            if (state.femaleProfileLocked && state.maleProfileLocked) {
                OutlinedButton(
                    onClick = {
                        viewModel.onEvent(UserSelectionEvent.UnlockAllProfiles(coupleId))
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ Tüm Kilitleri Temizle",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Bottom text
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💕 Love that cooks in the same kitchen, lives in the same heart 💕",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun GenderButton(
    @DrawableRes iconRes: Int,
    label: String,
    subtitle: String,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .shadow(
                elevation = if (isLocked) 4.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFFFF69B4).copy(alpha = 0.3f)
            )
            .alpha(if (isLocked) 0.5f else 1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) Color(0xFFE0E0E0) else Color(0xFFFFF9F5) // Gray if locked
        ),
        onClick = onClick,
        enabled = !isLocked
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Icon with lock overlay if locked
            Box(
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(64.dp)
                )
                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center),
                        tint = Color(0xFF757575)
                    )
                }
            }

            // Text content
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLocked) Color(0xFF757575) else Color(0xFF2C3E50)
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = if (isLocked) Color(0xFF9E9E9E) else Color(0xFF95A5A6),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

package com.emirhankarci.seninlemutfakta.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emirhankarci.seninlemutfakta.data.model.Gender

@Composable
fun UserSelectionScreen(
    coupleName: String,
    onGenderSelected: (Gender) -> Unit,
    onLogout: () -> Unit
) {
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
                text = "Çıkış",
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
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👨‍🍳",
                        fontSize = 40.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Couple Name
            Text(
                text = "💕 $coupleName",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = "Who's Cooking?",
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
                emoji = "👩‍🍳",
                label = "Woman",
                subtitle = "Ready to create magic",
                onClick = { onGenderSelected(Gender.FEMALE) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Man Button
            GenderButton(
                emoji = "👨‍🍳",
                label = "Man",
                subtitle = "Ready to create magic",
                onClick = { onGenderSelected(Gender.MALE) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom text
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💕 Cooking together, loving together 💕",
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
    emoji: String,
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFFFF69B4).copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF9F5) // Cream white
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Emoji
            Text(
                text = emoji,
                fontSize = 48.sp
            )

            // Text content
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50) // Dark charcoal
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF95A5A6), // Warm gray
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

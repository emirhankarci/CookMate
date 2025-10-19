package com.emirhankarci.seninlemutfakta.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emirhankarci.seninlemutfakta.ui.theme.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // DEĞİŞİKLİK 2: Ana Column artık SADECE içeriği barındırıyor. Header buradan kaldırıldı.
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White) // İçerik arka planı beyaz olarak kalıyor, bu doğru.
            .verticalScroll(rememberScrollState())
            .padding(top = 24.dp) // Header kaldırıldığı için üste biraz boşluk ekleyelim.
    ) {
        // Profile options
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Ayarlar",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )

            ProfileMenuItem(
                icon = Icons.Default.Favorite,
                title = "Favori Tarifler",
                subtitle = "Beğendiğiniz tarifleri görüntüleyin",
                onClick = { /* TODO */ }
            )

            ProfileMenuItem(
                icon = Icons.Default.DateRange,
                title = "Yemek Geçmişi",
                subtitle = "Yaptığınız tarifleri inceleyin",
                onClick = { /* TODO */ }
            )

            ProfileMenuItem(
                icon = Icons.Default.Person,
                title = "Çift Ayarları",
                subtitle = "Çift bilgilerinizi yönetin",
                onClick = { /* TODO */ }
            )

            ProfileMenuItem(
                icon = Icons.Default.Notifications,
                title = "Bildirimler",
                subtitle = "Bildirim tercihlerinizi ayarlayın",
                onClick = { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Diğer",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )

            ProfileMenuItem(
                icon = Icons.Default.Info,
                title = "Hakkında",
                subtitle = "Uygulama hakkında bilgi edinin",
                onClick = { /* TODO */ }
            )

            ProfileMenuItem(
                icon = Icons.Default.Share,
                title = "Paylaş",
                subtitle = "Uygulamayı arkadaşlarınızla paylaşın",
                onClick = { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Logout button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavBarActiveIcon,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Çıkış Yap",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    coupleName: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NavBarIndicatorStart,
                        NavBarIndicatorEnd
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Profile avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(48.dp),
                    tint = NavBarActiveIcon
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = coupleName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NavBarPillBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = NavBarActiveIcon,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

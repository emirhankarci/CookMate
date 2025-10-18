package com.emirhankarci.seninlemutfakta.presentation.countries

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emirhankarci.seninlemutfakta.data.model.Country

@Composable
fun CountryListScreen(
    viewModel: CountryListViewModel,
    onCountryClick: (String) -> Unit,
    onLogout: () -> Unit = {},
    coupleInfo: String = ""
) {
    val state by viewModel.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var selectedFilter by remember { mutableStateOf("All Countries") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with gradient and logout
        CountryListHeader(
            selectedFilter = selectedFilter,
            onFilterChange = { selectedFilter = it },
            onLogout = onLogout
        )

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF9F5)),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    color = Color(0xFFFF69B4)
                )

                state.error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Error: ${state.error}")
                        Button(
                            onClick = { viewModel.onEvent(CountryListEvent.Retry) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF69B4)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Çift bilgileri kartı
                        if (coupleInfo.isNotEmpty()) {
                            item {
                                CoupleInfoCard(
                                    coupleInfo = coupleInfo,
                                    clipboardManager = clipboardManager
                                )
                            }
                        }

                        // Filtrelenmiş ülke listesi
                        val filteredCountries = when (selectedFilter) {
                            "Unlocked" -> state.countries.filter { !state.isCountryLocked(it.countryCode) }
                            "In Progress" -> state.countries.filter {
                                !state.isCountryLocked(it.countryCode) &&
                                state.getCompletedRecipesCount(it.countryCode) > 0 &&
                                state.getCompletedRecipesCount(it.countryCode) < it.totalRecipes
                            }
                            else -> state.countries
                        }

                        items(filteredCountries) { country ->
                            CountryCard(
                                country = country,
                                isLocked = state.isCountryLocked(country.countryCode),
                                completedRecipes = state.getCompletedRecipesCount(country.countryCode),
                                totalRecipes = country.totalRecipes,
                                onClick = {
                                    if (!state.isCountryLocked(country.countryCode)) {
                                        onCountryClick(country.countryCode)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountryListHeader(
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onLogout: () -> Unit
) {
    // Gradient colors
    val gradientColors = listOf(
        Color(0xFFFFB6C1),
        Color(0xFFFF69B4),
        Color(0xFFFF6B6B)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logout button at top right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onLogout,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "🚪 Çıkış",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = "Choose Your Culinary\nJourney",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Explore authentic recipes from around the world",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Filter chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                label = "All Countries",
                selected = selectedFilter == "All Countries",
                onClick = { onFilterChange("All Countries") }
            )
            FilterChip(
                label = "Unlocked",
                selected = selectedFilter == "Unlocked",
                onClick = { onFilterChange("Unlocked") }
            )
            FilterChip(
                label = "In Progress",
                selected = selectedFilter == "In Progress",
                onClick = { onFilterChange("In Progress") }
            )
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.3f),
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color(0xFFFF69B4) else Color.White
            )
        }
    }
}

@Composable
fun CoupleInfoCard(
    coupleInfo: String,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE8F0)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "💕 Çift Bilgileri",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF69B4)
            )

            Text(
                text = coupleInfo,
                fontSize = 14.sp,
                color = Color(0xFF2C3E50),
                textAlign = TextAlign.Center
            )

            // Davet kodu varsa kopyalama butonu ekle
            if (coupleInfo.contains("Davet Kodu:")) {
                Button(
                    onClick = {
                        // Davet kodunu çıkar ve kopyala
                        val inviteCode = coupleInfo.substringAfter("Davet Kodu: ")
                            .substringBefore("\n")
                            .trim()
                        clipboardManager.setText(AnnotatedString(inviteCode))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF69B4)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📋 Kodu Kopyala",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CountryCard(
    country: Country,
    isLocked: Boolean,
    completedRecipes: Int,
    totalRecipes: Int,
    onClick: () -> Unit
) {
    val progressPercentage = if (totalRecipes > 0) {
        (completedRecipes.toFloat() / totalRecipes * 100).toInt()
    } else 0

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isLocked) 180.dp else 200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) Color(0xFFE0E0E0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLocked) 2.dp else 8.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top badges row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left: Completion badge or empty space
                    if (!isLocked && progressPercentage == 100) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "✓",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(32.dp))
                    }

                    // Right: Featured/New badge
                    if (!isLocked) {
                        when (country.countryCode) {
                            "france", "italy" -> FeaturedBadge()
                            "india" -> NewBadge()
                            else -> {}
                        }
                    }
                }

                // Country flag/code
                if (isLocked) {
                    // Locked: Show lock icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔒",
                            fontSize = 32.sp
                        )
                    }
                } else {
                    // Unlocked: Show flag emoji or country code
                    Text(
                        text = country.flagEmoji,
                        fontSize = 48.sp
                    )
                }

                // Country name
                Text(
                    text = country.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLocked) Color(0xFF757575) else Color(0xFF2C3E50)
                )

                // Description
                Text(
                    text = country.description,
                    fontSize = 13.sp,
                    color = if (isLocked) Color(0xFF9E9E9E) else Color(0xFF95A5A6),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                if (isLocked) {
                    // Locked: Show unlock button
                    Button(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF757575)
                        )
                    ) {
                        Text(
                            text = "🔓 Unlock for ${country.price}₺",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Unlocked: Show progress and button
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Progress info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$completedRecipes/$totalRecipes recipes",
                                fontSize = 12.sp,
                                color = Color(0xFF95A5A6)
                            )
                            Text(
                                text = "$progressPercentage%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (progressPercentage == 100) Color(0xFF4CAF50) else Color(0xFFFF69B4)
                            )
                        }

                        // Progress bar
                        LinearProgressIndicator(
                            progress = progressPercentage / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (progressPercentage == 100) Color(0xFF4CAF50) else Color(0xFFFF69B4),
                            trackColor = Color(0xFFE0E0E0)
                        )

                        // View recipes button
                        Button(
                            onClick = onClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF69B4)
                            )
                        ) {
                            Text(
                                text = "View Recipes →",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedBadge() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFD700) // Gold
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⭐", fontSize = 12.sp)
            Text(
                text = "Featured",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun NewBadge() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFF69B4)
    ) {
        Text(
            text = "New",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

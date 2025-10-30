package com.emirhankarci.cookmate.presentation.countries

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emirhankarci.cookmate.data.model.Country

@Composable
fun CountryListScreen(
    viewModel: CountryListViewModel,
    onCountryClick: (String) -> Unit,
    selectedFilter: String,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    // Ekran gösterildiğinde ülkeleri yükle (Firebase Auth hazır olduktan sonra)
    LaunchedEffect(Unit) {
        viewModel.loadCountriesIfNeeded()
    }

    // DEĞİŞİKLİK 1: Bu Column artık SADECE ana içerik alanını temsil ediyor.
    // Bu yüzden arka planını orijinal beyazımsı renge ayarlıyoruz.
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9F5))
    ) {
        // İçerik alanı
        Box(
            modifier = Modifier.fillMaxSize(),
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
                        // Hata mesajının rengini daha koyu yapalım ki beyaz arka planda okunsun
                        Text("Error: ${state.error}", color = Color.DarkGray)
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
                        contentPadding = PaddingValues(all = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val filteredCountries = when (selectedFilter) {
                            "Unlocked" -> state.countries.filter { !state.isCountryLocked(it.countryCode) }
                            "Locked" -> state.countries.filter { state.isCountryLocked(it.countryCode) }
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
    onFilterChange: (String) -> Unit
) {
    // DEĞİŞİKLİK 2: Gradient fırçasını (Brush) doğrudan Header'ın içine taşıdık.
    // Hatırladığım renkler bunlardı.
    val brush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFB6C1), // Light pink
                Color(0xFFFF69B4), // Hot pink
                Color(0xFFFF6B6B)  // Coral
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // DEĞİŞİKLİK 3: Mavi arka planı kaldırıp yerine pembe gradient'i uyguluyoruz.
            .background(brush)
            .statusBarsPadding()
            .padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Your Journey",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Explore authentic recipes from around the world",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                label = "All",
                selected = selectedFilter == "All Countries",
                onClick = { onFilterChange("All Countries") }
            )
            FilterChip(
                label = "Unlocked",
                selected = selectedFilter == "Unlocked",
                onClick = { onFilterChange("Unlocked") }
            )
            FilterChip(
                label = "Locked",
                selected = selectedFilter == "Locked",
                onClick = { onFilterChange("Locked") }
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
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) Color(0xFFE0E0E0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLocked) 2.dp else 8.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLocked) {
                // Kilitli kartlar için basit layout
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Lock icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔒",
                            fontSize = 32.sp
                        )
                    }

                    // Country name
                    Text(
                        text = country.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF757575),
                        maxLines = 1
                    )

                    // Description
                    Text(
                        text = country.description,
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E),
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Unlock button
                    Button(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF757575),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "🔓 Unlock for ${country.price}₺",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Açık kartlar için mevcut layout
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        if (progressPercentage == 100) {
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
                        when (country.countryCode) {
                            "france", "italy" -> FeaturedBadge()
                            "india" -> NewBadge()
                            else -> {}
                        }
                    }

                    // Country flag
                    Text(
                        text = country.flagEmoji,
                        fontSize = 48.sp
                    )

                    // Country name
                    Text(
                        text = country.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50),
                        maxLines = 1
                    )

                    // Description
                    Text(
                        text = country.description,
                        fontSize = 13.sp,
                        color = Color(0xFF95A5A6),
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Unlocked: Show progress and button
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

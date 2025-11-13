package com.emirhankarci.cookmate.presentation.countryselect

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.emirhankarci.cookmate.R
import com.emirhankarci.cookmate.presentation.countries.CountryListViewModel
import com.emirhankarci.cookmate.presentation.countries.CountryListEvent

@Composable
fun CountrySelectScreen(
    viewModel: CountryListViewModel,
    onCountryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCountriesIfNeeded()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFFF69B4)
                )
            }
            
            state.error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.countries) { country ->
                        CountryGridCard(
                            country = country,
                            onClick = { onCountryClick(country.countryCode) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CountryGridCard(
    country: com.emirhankarci.cookmate.data.model.Country,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Country flag - Lottie animation
            val flagResource = when (country.countryCode) {
                "france" -> R.raw.france_flag
                "italy" -> R.raw.italy_flag
                "turkey" -> R.raw.turkiye_flag
                "japan" -> R.raw.japan_flag
                "mexico" -> R.raw.mexico_flag
                else -> R.raw.france_flag
            }
            
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(flagResource))
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )
            
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Country name
            Text(
                text = country.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Recipe count
            Text(
                text = "${country.totalRecipes} recipes",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
        }
    }
}
package com.emirhankarci.seninlemutfakta.presentation.countries

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emirhankarci.seninlemutfakta.data.model.Country

@Composable
fun CountryListScreen(
    viewModel: CountryListViewModel,
    onCountryClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isLoading -> CircularProgressIndicator()

            state.error != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Hata: ${state.error}")
                    Button(onClick = { viewModel.onEvent(CountryListEvent.Retry) }) {
                        Text("Tekrar Dene")
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Ülkeler",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(state.countries) { country ->
                        CountryCard(
                            country = country,
                            isLocked = state.isCountryLocked(country.countryCode),
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

@Composable
private fun CountryCard(
    country: Country,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = country.flagEmoji,
                    style = MaterialTheme.typography.headlineMedium
                )
                Column {
                    Text(
                        text = country.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = country.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (isLocked) {
                Text(
                    text = "🔒 ${country.price}₺",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "✅",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}
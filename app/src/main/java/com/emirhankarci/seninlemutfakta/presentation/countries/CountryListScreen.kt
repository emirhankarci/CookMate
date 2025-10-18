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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 48.dp), // Status bar için extra top padding
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Başlık ve çıkış yap butonu
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ülkeler",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            TextButton(
                                onClick = onLogout,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("🚪 Çıkış")
                            }
                        }
                    }

                    // Çift bilgileri kartı
                    if (coupleInfo.isNotEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "💕 Çift Bilgileri",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = coupleInfo,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    
                                    // Davet kodu varsa kopyalama butonu ekle
                                    if (coupleInfo.contains("Davet Kodu:")) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                // Davet kodunu çıkar ve kopyala
                                                val inviteCode = coupleInfo.substringAfter("Davet Kodu: ").substringBefore("\n").trim()
                                                clipboardManager.setText(AnnotatedString(inviteCode))
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        ) {
                                            Text("📋 Kodu Kopyala")
                                        }
                                    }
                                }
                            }
                        }
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
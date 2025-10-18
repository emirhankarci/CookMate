package com.emirhankarci.seninlemutfakta.presentation.couple

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.emirhankarci.seninlemutfakta.data.model.Gender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoupleSetupScreen(
    state: CoupleState,
    onEvent: (CoupleEvent) -> Unit,
    onCoupleReady: (coupleId: String, userGender: Gender) -> Unit,
    onLogout: () -> Unit
) {
    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    var inviteCode by remember { mutableStateOf("") }
    var showJoinForm by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    // Çift katılındığında (join) otomatik geçiş yap
    LaunchedEffect(state.isJoinSuccessful) {
        if (state.isJoinSuccessful && state.currentCouple != null && selectedGender != null) {
            onEvent(CoupleEvent.ClearSuccess)
            onCoupleReady(state.currentCouple.coupleId, selectedGender!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .padding(top = 32.dp), // Status bar için extra padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        // Çıkış yap butonu (status bar'dan uzak)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onLogout,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("🚪 Çıkış Yap")
            }
        }

        Text(
            text = "💕 Çift Oluştur",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Eşinle birlikte yemek yapmak için önce bir çift oluşturun veya mevcut çifte katılın.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cinsiyet seçimi
        Text(
            text = "Cinsiyetinizi seçin:",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilterChip(
                onClick = { selectedGender = Gender.FEMALE },
                label = { Text("👩 Kadın") },
                selected = selectedGender == Gender.FEMALE,
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                onClick = { selectedGender = Gender.MALE },
                label = { Text("👨 Erkek") },
                selected = selectedGender == Gender.MALE,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedGender != null) {
            // Çift oluştur butonu
            Button(
                onClick = {
                    onEvent(CoupleEvent.CreateCouple(selectedGender!!))
                },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (state.isLoading && !showJoinForm) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Yeni Çift Oluştur")
                }
            }

            // Veya ayırıcı
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(modifier = Modifier.weight(1f))
                Text(
                    text = " VEYA ",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Divider(modifier = Modifier.weight(1f))
            }

            // Çifte katıl butonu
            OutlinedButton(
                onClick = { showJoinForm = !showJoinForm },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showJoinForm) "İptal" else "Mevcut Çifte Katıl")
            }

            // Katılma formu
            if (showJoinForm) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    label = { Text("Davet Kodu (6 haneli)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (inviteCode.length == 6) {
                            onEvent(CoupleEvent.JoinCouple(inviteCode, selectedGender!!))
                        }
                    },
                    enabled = !state.isLoading && inviteCode.length == 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (state.isLoading && showJoinForm) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Çifte Katıl")
                    }
                }
            }
        }

        // Davet kodu göster (çift oluşturulduysa veya tamamlandıysa)
        if (state.inviteCode.isNotEmpty() && (state.isWaitingForPartner || (state.currentCouple?.isComplete == true))) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (state.currentCouple?.isComplete == true) "✅ Çift Tamamlandı!" else "🎉 Çift Oluşturuldu!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (state.currentCouple?.isComplete == true) "Davet Kodunuz:" else "Eşinizle paylaşın:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.inviteCode,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(state.inviteCode))
                            }
                        ) {
                            Text(
                                text = "📋",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    
                    Text(
                        text = if (state.currentCouple?.isComplete == true) 
                            "Artık birlikte yemek yapabilirsiniz!" 
                        else 
                            "Eşinizin katılmasını bekliyorsunuz...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            if (state.currentCouple != null && selectedGender != null) {
                                onEvent(CoupleEvent.ClearSuccess)
                                onCoupleReady(state.currentCouple.coupleId, selectedGender!!)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Devam Et")
                    }
                }
            }
        }

        // Hata mesajı
        if (state.error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

package com.emirhankarci.seninlemutfakta.presentation.countries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emirhankarci.seninlemutfakta.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountryListViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CountryListState())
    val state = _state.asStateFlow()

    private var hasLoadedOnce = false

    // init bloğunu kaldırdık - manuel tetikleme ile yüklenecek

    fun onEvent(event: CountryListEvent) {
        when (event) {
            is CountryListEvent.LoadCountries -> loadCountries()
            is CountryListEvent.UnlockCountry -> unlockCountry(event.countryCode)
            is CountryListEvent.SelectCountry -> selectCountry(event.countryCode)
            is CountryListEvent.Retry -> loadCountries()
        }
    }

    // Ekran ilk gösterildiğinde çağrılacak
    fun loadCountriesIfNeeded() {
        if (!hasLoadedOnce && _state.value.countries.isEmpty() && !_state.value.isLoading) {
            hasLoadedOnce = true
            loadCountries()
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            firebaseRepository.getAllCountries()
                .onSuccess { countries ->
                    _state.update {
                        it.copy(
                            countries = countries,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    val errorMessage = exception.message ?: "Bilinmeyen hata"

                    // Permission denied hatası alırsak, Firebase Auth henüz hazır değil demektir
                    // 1 saniye bekleyip otomatik retry yapalım
                    if (errorMessage.contains("permission", ignoreCase = true) && !hasLoadedOnce) {
                        kotlinx.coroutines.delay(1000) // 1 saniye bekle
                        loadCountries() // Tekrar dene
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = errorMessage
                            )
                        }
                    }
                }
        }
    }

    private fun unlockCountry(countryCode: String) {
        viewModelScope.launch {
            // TODO: Ödeme işlemi burada yapılacak
            // Şimdilik direkt unlock edelim (test için)

            val currentUnlocked = _state.value.userUnlockedCountries.toMutableList()
            if (!currentUnlocked.contains(countryCode)) {
                currentUnlocked.add(countryCode)

                _state.update {
                    it.copy(userUnlockedCountries = currentUnlocked)
                }

                // TODO: Firebase'e kaydet (kullanıcı profiline)
            }
        }
    }

    private fun selectCountry(countryCode: String) {
        // Navigation burada yapılacak
        // Şimdilik sadece log
        println("Ülke seçildi: $countryCode")
    }
}
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

    init {
        loadCountries()
    }

    fun onEvent(event: CountryListEvent) {
        when (event) {
            is CountryListEvent.LoadCountries -> loadCountries()
            is CountryListEvent.UnlockCountry -> unlockCountry(event.countryCode)
            is CountryListEvent.SelectCountry -> selectCountry(event.countryCode)
            is CountryListEvent.Retry -> loadCountries()
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
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Bilinmeyen hata"
                        )
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
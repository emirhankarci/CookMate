package com.emirhankarci.seninlemutfakta.presentation.countries

import com.emirhankarci.seninlemutfakta.data.model.Country

data class CountryListState(
    val countries: List<Country> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val userUnlockedCountries: List<String> = listOf("france", "italy") // Başlangıçta açık olanlar
) {
    // Helper: Ülke kilitli mi kontrol et
    fun isCountryLocked(countryCode: String): Boolean {
        return !userUnlockedCountries.contains(countryCode)
    }

    // Helper: Kaç ülke açık
    fun getUnlockedCountriesCount(): Int = userUnlockedCountries.size

    // Helper: Toplam ülke sayısı
    fun getTotalCountriesCount(): Int = countries.size
}

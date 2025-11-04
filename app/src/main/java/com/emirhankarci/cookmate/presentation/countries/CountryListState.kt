package com.emirhankarci.cookmate.presentation.countries

import com.emirhankarci.cookmate.data.model.Country

data class CountryListState(
    val countries: List<Country> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val userUnlockedCountries: List<String> = listOf("france", "italy", "turkey"), // Başlangıçta açık olanlar
    val completedRecipes: Map<String, Int> = emptyMap(), // Her ülke için tamamlanan tarif sayısı
    val isGridView: Boolean = false // Grid view durumu
) {
    // Helper: Ülke kilitli mi kontrol et
    fun isCountryLocked(countryCode: String): Boolean {
        // Önce Firebase'den gelen isLocked değerini kontrol et
        val country = countries.find { it.countryCode == countryCode }
        if (country != null && !country.isLocked) {
            return false // Firebase'de unlocked ise direkt false döndür
        }
        
        // Firebase'de locked ise veya ülke bulunamazsa, kullanıcının unlock ettiği listesini kontrol et
        return !userUnlockedCountries.contains(countryCode)
    }

    // Helper: Kaç ülke açık
    fun getUnlockedCountriesCount(): Int = userUnlockedCountries.size

    // Helper: Toplam ülke sayısı
    fun getTotalCountriesCount(): Int = countries.size

    // Helper: Tamamlanan tarif sayısını al
    fun getCompletedRecipesCount(countryCode: String): Int {
        return completedRecipes[countryCode] ?: 0
    }
}

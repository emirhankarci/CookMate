package com.emirhankarci.cookmate.presentation.countries

sealed class CountryListEvent {
    object LoadCountries : CountryListEvent()
    data class UnlockCountry(val countryCode: String) : CountryListEvent()
    data class SelectCountry(val countryCode: String) : CountryListEvent()
    object Retry : CountryListEvent()
}

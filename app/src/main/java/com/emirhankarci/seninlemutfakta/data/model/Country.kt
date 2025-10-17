package com.emirhankarci.seninlemutfakta.data.model

data class Country(
    val countryCode: String = "",
    val name: String = "",
    val flagEmoji: String = "",
    val flagUrl: String = "",
    val passportStampUrl: String = "",
    val isLocked: Boolean = true,
    val price: Double = 0.0,
    val order: Int = 0,
    val totalRecipes: Int = 0,
    val description: String = ""
) {
    // Firebase için boş constructor gerekli
    constructor() : this("", "", "", "", "", true, 0.0, 0, 0, "")
}
package com.emirhankarci.seninlemutfakta.data.model

data class Ingredient(
    val name: String = "",
    val amount: String = "",
    val iconUrl: String = ""
) {
    constructor() : this("", "", "")
}
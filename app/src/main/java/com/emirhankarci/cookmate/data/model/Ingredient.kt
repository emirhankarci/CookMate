package com.emirhankarci.cookmate.data.model

data class Ingredient(
    val name: String = "",
    val amount: String = "",
    val iconUrl: String = ""
) {
    constructor() : this("", "", "")
}

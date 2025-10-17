package com.emirhankarci.seninlemutfakta.data.model

import com.google.firebase.database.PropertyName

data class Recipe(
    val recipeId: String = "",
    val countryCode: String = "",
    val title: String = "",
    val titleTurkish: String = "",
    val description: String = "",
    val difficulty: Int = 1,
    val estimatedTime: Int = 0,
    val servings: Int = 0,
    val thumbnailUrl: String = "",
    val videoUrl: String = "",
    val order: Int = 0,

    @get:PropertyName("isLocked")
    @set:PropertyName("isLocked")
    var isLocked: Boolean = false,

    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<RecipeStep> = emptyList(),
    val femaleSteps: List<RecipeStep> = emptyList(),
    val maleSteps: List<RecipeStep> = emptyList()
) {
    constructor() : this(
        "", "", "", "", "", 1, 0, 0, "", "", 0, false,
        emptyList(), emptyList(), emptyList(), emptyList()
    )
}
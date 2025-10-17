package com.emirhankarci.seninlemutfakta.presentation.recipes

sealed class RecipeListEvent {
    data class LoadRecipes(val countryCode: String) : RecipeListEvent()
    data class SelectRecipe(val recipeId: String) : RecipeListEvent()
    data class ChangeSortType(val sortType: RecipeSortType) : RecipeListEvent()
    object Retry : RecipeListEvent()
}
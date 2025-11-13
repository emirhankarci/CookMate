package com.emirhankarci.cookmate.presentation.favourites

import com.emirhankarci.cookmate.data.model.Recipe

data class FavouriteRecipesState(
    val favouriteRecipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

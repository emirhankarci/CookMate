package com.emirhankarci.cookmate.presentation.favourites

sealed interface FavouriteRecipesEvent {
    data object LoadFavourites : FavouriteRecipesEvent
    data class AddFavourite(val recipeId: String) : FavouriteRecipesEvent
    data class RemoveFavourite(val recipeId: String) : FavouriteRecipesEvent
}

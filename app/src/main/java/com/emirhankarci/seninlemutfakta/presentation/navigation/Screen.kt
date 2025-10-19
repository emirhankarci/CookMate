package com.emirhankarci.seninlemutfakta.presentation.navigation

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object UserSelection : Screen()
    object CountryList : Screen()
    object RecipeList : Screen()
    object CoopModeSelection : Screen()
    object CookingSession : Screen()
    object Profile : Screen()
}
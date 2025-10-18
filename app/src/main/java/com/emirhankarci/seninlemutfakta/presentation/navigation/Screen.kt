package com.emirhankarci.seninlemutfakta.presentation.navigation

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object CoupleSetup : Screen()
    object CountryList : Screen()
    object RecipeList : Screen()
    object CoopModeSelection : Screen()
    object GenderSelection : Screen()
    object CookingSession : Screen()
}
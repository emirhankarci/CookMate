package com.emirhankarci.cookmate.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    // Auth Graph - contains all authentication related screens
    @Serializable
    data object AuthGraph : Route
    
    @Serializable
    data object WelcomeScreen : Route
    
    @Serializable
    data object LoginScreen : Route
    
    @Serializable
    data object RegisterScreen : Route
    
    @Serializable
    data object UserSelectionScreen : Route
    
    // Main App Graph - contains all main app screens
    @Serializable
    data object MainGraph : Route
    
    @Serializable
    data object CountryListScreen : Route
    
    @Serializable
    data class RecipeListScreen(val countryCode: String) : Route
    
    @Serializable
    data class CoopModeSelectionScreen(
        val recipeId: String,
        val recipeName: String
    ) : Route
    
    @Serializable
    data class CookingSessionScreen(
        val recipeId: String,
        val isCoopMode: Boolean
    ) : Route
    
    @Serializable
    data object ProfileScreen : Route
}

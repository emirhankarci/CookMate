package com.emirhankarci.cookmate.presentation.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.emirhankarci.cookmate.data.model.Gender
import com.emirhankarci.cookmate.presentation.auth.AuthEvent
import com.emirhankarci.cookmate.presentation.auth.AuthViewModel
import com.emirhankarci.cookmate.presentation.auth.LoginScreen
import com.emirhankarci.cookmate.presentation.auth.RegisterScreen
import com.emirhankarci.cookmate.presentation.auth.UserSelectionScreen
import com.emirhankarci.cookmate.presentation.auth.UserSelectionViewModel
import com.emirhankarci.cookmate.presentation.cooking.CookingSessionViewModel
import com.emirhankarci.cookmate.presentation.cooking.screens.CookingSessionScreen
import com.emirhankarci.cookmate.presentation.cooking.screens.CoopModeSelectionScreen
import com.emirhankarci.cookmate.presentation.countries.CountryListScreen
import com.emirhankarci.cookmate.presentation.countries.CountryListViewModel
import com.emirhankarci.cookmate.presentation.couple.CoupleViewModel
import com.emirhankarci.cookmate.presentation.profile.ProfileScreen
import com.emirhankarci.cookmate.presentation.recipes.RecipeListEvent
import com.emirhankarci.cookmate.presentation.recipes.RecipeListScreen
import com.emirhankarci.cookmate.presentation.recipes.RecipeListViewModel
import com.emirhankarci.cookmate.presentation.welcome.WelcomeScreen

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    coupleViewModel: CoupleViewModel,
    countryListViewModel: CountryListViewModel,
    recipeListViewModel: RecipeListViewModel,
    cookingSessionViewModel: CookingSessionViewModel,
    userSelectionViewModel: UserSelectionViewModel
) {
    val navController = rememberNavController()
    val authState by authViewModel.state.collectAsState()
    
    // Determine start destination based on auth state
    val startDestination = if (authState.isLoggedIn) Route.MainGraph else Route.AuthGraph
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Graph - Authentication flow
        navigation<Route.AuthGraph>(
            startDestination = Route.WelcomeScreen
        ) {
            composable<Route.WelcomeScreen> {
                WelcomeScreen(
                    onNavigateToLogin = {
                        navController.navigate(Route.LoginScreen)
                    },
                    onNavigateToRegister = {
                        navController.navigate(Route.RegisterScreen)
                    }
                )
            }
            
            composable<Route.LoginScreen> {
                val authState by authViewModel.state.collectAsState()
                
                LoginScreen(
                    state = authState,
                    onEvent = authViewModel::onEvent,
                    onNavigateToRegister = {
                        navController.navigate(Route.RegisterScreen)
                    },
                    onLoginSuccess = {
                        navController.navigate(Route.MainGraph) {
                            popUpTo(Route.AuthGraph) { inclusive = true }
                        }
                    },
                    onBackToWelcome = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable<Route.RegisterScreen> {
                val authState by authViewModel.state.collectAsState()
                
                RegisterScreen(
                    state = authState,
                    onEvent = authViewModel::onEvent,
                    onNavigateToLogin = {
                        navController.navigate(Route.LoginScreen)
                    },
                    onRegisterSuccess = {
                        navController.navigate(Route.MainGraph) {
                            popUpTo(Route.AuthGraph) { inclusive = true }
                        }
                    }
                )
            }
        }
        
        // Main App Graph - Main application flow
        navigation<Route.MainGraph>(
            startDestination = Route.UserSelectionScreen
        ) {
            composable<Route.UserSelectionScreen> {
                val coupleState by coupleViewModel.state.collectAsState()
                
                UserSelectionScreen(
                    viewModel = userSelectionViewModel,
                    userId = authState.currentUser?.uid ?: "",
                    coupleId = authState.currentUser?.uid ?: "",
                    coupleName = coupleState.currentCouple?.coupleName ?: "",
                    onGenderSelected = { gender ->
                        navController.navigate(Route.CountryListScreen)
                    },
                    onLogout = {
                        authViewModel.onEvent(AuthEvent.Logout)
                        navController.navigate(Route.AuthGraph) {
                            popUpTo(Route.MainGraph) { inclusive = true }
                        }
                    }
                )
            }
            
            composable<Route.CountryListScreen> {
                CountryListScreen(
                    viewModel = countryListViewModel,
                    onCountryClick = { countryCode ->
                        recipeListViewModel.onEvent(
                            RecipeListEvent.LoadRecipes(countryCode)
                        )
                        navController.navigate(Route.RecipeListScreen(countryCode))
                    },
                    selectedFilter = "All Countries"
                )
            }
            
            composable<Route.RecipeListScreen> { backStackEntry ->
                val args = backStackEntry.toRoute<Route.RecipeListScreen>()
                
                RecipeListScreen(
                    viewModel = recipeListViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onRecipeClick = { recipeId ->
                        val recipeState = recipeListViewModel.state.value
                        val recipe = recipeState.recipes.find { it.recipeId == recipeId }
                        val recipeName = recipe?.titleTurkish ?: recipe?.title ?: ""
                        
                        navController.navigate(
                            Route.CoopModeSelectionScreen(
                                recipeId = recipeId,
                                recipeName = recipeName
                            )
                        )
                    },
                    selectedFilter = com.emirhankarci.cookmate.presentation.recipes.RecipeFilter.ALL
                )
            }
            
            composable<Route.CoopModeSelectionScreen> { backStackEntry ->
                val args = backStackEntry.toRoute<Route.CoopModeSelectionScreen>()
                
                CoopModeSelectionScreen(
                    recipeName = args.recipeName,
                    onDismiss = {
                        navController.popBackStack()
                    },
                    onSoloMode = {
                        navController.navigate(
                            Route.CookingSessionScreen(
                                recipeId = args.recipeId,
                                isCoopMode = false
                            )
                        )
                    },
                    onCoopMode = {
                        navController.navigate(
                            Route.CookingSessionScreen(
                                recipeId = args.recipeId,
                                isCoopMode = true
                            )
                        )
                    }
                )
            }
            
            composable<Route.CookingSessionScreen> { backStackEntry ->
                val args = backStackEntry.toRoute<Route.CookingSessionScreen>()
                
                // Note: Recipe loading should be handled via CookingSessionEvent
                // Example: cookingSessionViewModel.onEvent(CookingSessionEvent.StartSession(...))
                
                CookingSessionScreen(
                    state = cookingSessionViewModel.state.collectAsState().value,
                    onEvent = cookingSessionViewModel::onEvent,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable<Route.ProfileScreen> {
                ProfileScreen(
                    onLogout = {
                        authViewModel.onEvent(AuthEvent.Logout)
                        navController.navigate(Route.AuthGraph) {
                            popUpTo(Route.MainGraph) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

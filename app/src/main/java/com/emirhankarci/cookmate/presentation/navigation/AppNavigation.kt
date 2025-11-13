package com.emirhankarci.cookmate.presentation.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.emirhankarci.cookmate.presentation.countryselect.CountrySelectScreen
import com.emirhankarci.cookmate.presentation.couple.CoupleViewModel
import com.emirhankarci.cookmate.presentation.favourites.FavouriteRecipesScreen
import com.emirhankarci.cookmate.presentation.favourites.FavouriteRecipesViewModel
import com.emirhankarci.cookmate.presentation.profile.ProfileScreen
import com.emirhankarci.cookmate.presentation.recipes.RecipeListEvent
import com.emirhankarci.cookmate.presentation.recipes.RecipeListScreen
import com.emirhankarci.cookmate.presentation.recipes.RecipeListViewModel
import com.emirhankarci.cookmate.presentation.welcome.WelcomeScreen
import com.emirhankarci.cookmate.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    coupleViewModel: CoupleViewModel,
    countryListViewModel: CountryListViewModel,
    recipeListViewModel: RecipeListViewModel,
    cookingSessionViewModel: CookingSessionViewModel,
    userSelectionViewModel: UserSelectionViewModel,
    favouriteRecipesViewModel: FavouriteRecipesViewModel
) {
    val navController = rememberNavController()
    val authState by authViewModel.state.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    // Determine start destination based on auth state
    val startDestination = if (authState.isLoggedIn) Route.MainGraph else Route.AuthGraph
    
    // Determine if bottom bar should be shown
    val showBottomBar = currentRoute?.contains("AuthGraph") == false &&
            currentRoute?.contains("WelcomeScreen") == false &&
            currentRoute?.contains("LoginScreen") == false &&
            currentRoute?.contains("RegisterScreen") == false &&
            currentRoute?.contains("UserSelectionScreen") == false
    
    Scaffold(
        containerColor = Color(0xFFF4F3F2),
        topBar = {
            if (showBottomBar){
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Image(
                                painter = painterResource(id = R.drawable.appicon),
                                contentDescription = "Uygulama Logosu",
                                modifier = Modifier.size(26.dp),
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "CookMate",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate(Route.ProfileScreen) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAlert,
                                contentDescription = "Bildirimler"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFF4F3F2)
                    )
                )
            }

        },
        bottomBar = {
            if (showBottomBar) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF4F3F2))
                        .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.navigationBars)
                ) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFE0E0E0)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.navigate(Route.CountryListScreen) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Home",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(
                            onClick = { navController.navigate(Route.FavouriteRecipesScreen) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Favourites",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(
                            onClick = { navController.navigate(Route.CountrySelectScreen) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Countries",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        IconButton(
                            onClick = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "BottomBar",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(
                            onClick = { navController.navigate(Route.ProfileScreen) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
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
                    onFavouriteClick = { recipeId ->
                        favouriteRecipesViewModel.addFavourite(recipeId)
                    }
                )
            }
            
            composable<Route.CountrySelectScreen> {
                CountrySelectScreen(
                    viewModel = countryListViewModel,
                    onCountryClick = { countryCode ->
                        recipeListViewModel.onEvent(
                            RecipeListEvent.LoadRecipes(countryCode)
                        )
                        navController.navigate(Route.RecipeListScreen(countryCode))
                    }
                )
            }
            
            composable<Route.FavouriteRecipesScreen> {
                FavouriteRecipesScreen(
                    viewModel = favouriteRecipesViewModel,
                    onRecipeClick = { recipeId ->
                        // TODO: Navigate to recipe detail or cooking session
                    }
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
                    onFavouriteClick = { recipeId ->
                        favouriteRecipesViewModel.addFavourite(recipeId)
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
}

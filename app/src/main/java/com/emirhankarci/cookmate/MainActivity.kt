package com.emirhankarci.cookmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.emirhankarci.cookmate.presentation.auth.AuthViewModel
import com.emirhankarci.cookmate.presentation.auth.UserSelectionViewModel
import com.emirhankarci.cookmate.presentation.cooking.CookingSessionViewModel
import com.emirhankarci.cookmate.presentation.countries.CountryListViewModel
import com.emirhankarci.cookmate.presentation.couple.CoupleViewModel
import com.emirhankarci.cookmate.presentation.navigation.AppNavigation
import com.emirhankarci.cookmate.presentation.recipes.RecipeListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val coupleViewModel: CoupleViewModel by viewModels()
    private val userSelectionViewModel: UserSelectionViewModel by viewModels()
    private val countryListViewModel: CountryListViewModel by viewModels()
    private val recipeListViewModel: RecipeListViewModel by viewModels()
    private val cookingSessionViewModel: CookingSessionViewModel by viewModels()
    private val favouriteRecipesViewModel: com.emirhankarci.cookmate.presentation.favourites.FavouriteRecipesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    coupleViewModel = coupleViewModel,
                    countryListViewModel = countryListViewModel,
                    recipeListViewModel = recipeListViewModel,
                    cookingSessionViewModel = cookingSessionViewModel,
                    userSelectionViewModel = userSelectionViewModel,
                    favouriteRecipesViewModel = favouriteRecipesViewModel
                )
            }
        }
    }
}

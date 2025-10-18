package com.emirhankarci.seninlemutfakta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.emirhankarci.seninlemutfakta.presentation.auth.AuthViewModel
import com.emirhankarci.seninlemutfakta.presentation.cooking.CookingSessionViewModel
import com.emirhankarci.seninlemutfakta.presentation.countries.CountryListViewModel
import com.emirhankarci.seninlemutfakta.presentation.couple.CoupleViewModel
import com.emirhankarci.seninlemutfakta.presentation.navigation.AppNavigation
import com.emirhankarci.seninlemutfakta.presentation.recipes.RecipeListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val coupleViewModel: CoupleViewModel by viewModels()
    private val countryListViewModel: CountryListViewModel by viewModels()
    private val recipeListViewModel: RecipeListViewModel by viewModels()
    private val cookingSessionViewModel: CookingSessionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    coupleViewModel = coupleViewModel,
                    countryListViewModel = countryListViewModel,
                    recipeListViewModel = recipeListViewModel,
                    cookingSessionViewModel = cookingSessionViewModel
                )
            }
        }
    }
}
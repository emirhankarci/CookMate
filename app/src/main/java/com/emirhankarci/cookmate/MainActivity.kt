package com.emirhankarci.cookmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.emirhankarci.cookmate.data.model.Gender
import com.emirhankarci.cookmate.presentation.auth.AuthViewModel
import com.emirhankarci.cookmate.presentation.auth.UserSelectionEvent
import com.emirhankarci.cookmate.presentation.auth.UserSelectionViewModel
import com.emirhankarci.cookmate.presentation.cooking.CookingSessionViewModel
import com.emirhankarci.cookmate.presentation.countries.CountryListViewModel
import com.emirhankarci.cookmate.presentation.couple.CoupleViewModel
import com.emirhankarci.cookmate.presentation.navigation.AppNavigation
import com.emirhankarci.cookmate.presentation.recipes.RecipeListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val coupleViewModel: CoupleViewModel by viewModels()
    private val userSelectionViewModel: UserSelectionViewModel by viewModels()
    private val countryListViewModel: CountryListViewModel by viewModels()
    private val recipeListViewModel: RecipeListViewModel by viewModels()
    private val cookingSessionViewModel: CookingSessionViewModel by viewModels()

    // Track current user selection for cleanup
    private var currentUserGender: Gender? = null
    private var currentCoupleId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge to edge with transparent status bar
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
                    onUserGenderChanged = { gender, coupleId ->
                        currentUserGender = gender
                        currentCoupleId = coupleId
                    }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Unlock profile when app goes to background/closes
        unlockCurrentProfile()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Additional safety measure
        unlockCurrentProfile()
    }

    private fun unlockCurrentProfile() {
        val gender = currentUserGender
        val coupleId = currentCoupleId

        if (gender != null && coupleId != null) {
            lifecycleScope.launch {
                userSelectionViewModel.onEvent(
                    UserSelectionEvent.UnlockProfile(
                        coupleId = coupleId,
                        gender = gender
                    )
                )
            }
        }
    }
}

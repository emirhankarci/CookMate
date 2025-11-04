package com.emirhankarci.cookmate.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.emirhankarci.cookmate.presentation.navigation.BottomNavigationBarTopLine
import com.emirhankarci.cookmate.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    topBar: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    val shouldShowBottomNav = currentScreen in listOf(
        Screen.CountryList,
        Screen.RecipeList,
        Screen.Profile
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = {
            if (shouldShowBottomNav) {
                BottomNavigationBarTopLine(
                    currentScreen = currentScreen,
                    onNavigate = onNavigate
                )
            }
        }
    ) { paddingValues ->
        content(Modifier.padding(paddingValues))
    }
}

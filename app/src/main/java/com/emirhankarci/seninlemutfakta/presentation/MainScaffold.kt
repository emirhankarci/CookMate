package com.emirhankarci.seninlemutfakta.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emirhankarci.seninlemutfakta.presentation.navigation.BottomNavigationBarTopLine
import com.emirhankarci.seninlemutfakta.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onBackClick: () -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    val shouldShowBottomNav = currentScreen in listOf(
        Screen.CountryList,
        Screen.RecipeList,
        Screen.Profile
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            when (currentScreen) {
                Screen.RecipeList, Screen.Profile, Screen.CoopModeSelection -> {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = currentScreen.title,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            if (currentScreen != Screen.Profile) {
                                IconButton(onClick = onBackClick) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        // DEĞİŞİKLİK: TopAppBar'ın arka plan rengini görmeniz için
                        // Mavi olarak ayarladık.
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Blue
                        )
                    )
                }
                else -> {
                    // Diğer ekranlar için TopAppBar yok.
                }
            }
        },
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
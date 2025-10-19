package com.emirhankarci.seninlemutfakta.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.emirhankarci.seninlemutfakta.data.model.CookingSession
import com.emirhankarci.seninlemutfakta.data.model.Gender
import com.emirhankarci.seninlemutfakta.presentation.auth.AuthViewModel
import com.emirhankarci.seninlemutfakta.presentation.auth.LoginScreen
import com.emirhankarci.seninlemutfakta.presentation.auth.RegisterScreen
import com.emirhankarci.seninlemutfakta.presentation.auth.UserSelectionScreen
import com.emirhankarci.seninlemutfakta.presentation.couple.CoupleViewModel
import com.emirhankarci.seninlemutfakta.presentation.cooking.CookingSessionEvent
import com.emirhankarci.seninlemutfakta.presentation.cooking.CookingSessionViewModel
import com.emirhankarci.seninlemutfakta.presentation.cooking.components.WaitingForPartnerDialog
import com.emirhankarci.seninlemutfakta.presentation.cooking.screens.CookingSessionScreen
import com.emirhankarci.seninlemutfakta.presentation.cooking.screens.CoopModeSelectionScreen
import com.emirhankarci.seninlemutfakta.presentation.countries.CountryListEvent
import com.emirhankarci.seninlemutfakta.presentation.countries.CountryListScreen
import com.emirhankarci.seninlemutfakta.presentation.countries.CountryListViewModel
import com.emirhankarci.seninlemutfakta.presentation.MainScaffold
import com.emirhankarci.seninlemutfakta.presentation.profile.ProfileScreen
import com.emirhankarci.seninlemutfakta.presentation.recipes.RecipeListEvent
import com.emirhankarci.seninlemutfakta.presentation.recipes.RecipeListScreen
import com.emirhankarci.seninlemutfakta.presentation.recipes.RecipeListViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    coupleViewModel: CoupleViewModel,
    countryListViewModel: CountryListViewModel,
    recipeListViewModel: RecipeListViewModel,
    cookingSessionViewModel: CookingSessionViewModel
) {
    val authState by authViewModel.state.collectAsState()
    val coupleState by coupleViewModel.state.collectAsState()
    
    // Authentication durumuna göre başlangıç ekranını belirle
    var currentScreen by remember {
        mutableStateOf<Screen>(
            if (!authState.isLoggedIn) Screen.Login else Screen.UserSelection
        )
    }

    // Auth durumu değiştiğinde ekranı güncelle
    LaunchedEffect(authState.isLoggedIn) {
        currentScreen = if (!authState.isLoggedIn) Screen.Login else Screen.UserSelection
    }

    var selectedCountry by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf("") }
    var selectedRecipeName by remember { mutableStateOf("") }
    var isCoopMode by remember { mutableStateOf(false) }

    // Kullanıcı bilgileri - Firebase Auth'dan al
    val currentUserId = authState.currentUser?.uid ?: ""
    var currentUserGender by remember { mutableStateOf<Gender?>(null) } // Profile seçilene kadar null
    val coupleId = authState.currentUser?.uid ?: "" // Couple ID = Firebase UID

    val cookingState by cookingSessionViewModel.state.collectAsState()

    // UserSelection'dan sonra waiting session'ları real-time dinle
    LaunchedEffect(coupleId, currentUserGender) {
        if (coupleId.isNotEmpty() && currentUserGender != null) {
            // Real-time listener başlat
            cookingSessionViewModel.observeWaitingSessionForCouple(coupleId)
        }
    }

    // CookingSession ekranına girildiğinde waiting listener'ı durdur
    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.CookingSession) {
            cookingSessionViewModel.stopObservingWaitingSession()
        }
    }

    // Use MainScaffold for all screens
    when (currentScreen) {
        Screen.CountryList -> {
            MainScaffold(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                onBackClick = { /* No back for home screen */ }
            ) { modifier ->
                CountryListScreen(
                    viewModel = countryListViewModel,
                    onCountryClick = { countryCode ->
                        selectedCountry = countryCode
                        recipeListViewModel.onEvent(
                            RecipeListEvent.LoadRecipes(countryCode)
                        )
                        currentScreen = Screen.RecipeList
                    },
                    modifier = modifier
                )
            }
        }

        Screen.RecipeList -> {
            val recipeState by recipeListViewModel.state.collectAsState()

            MainScaffold(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                onBackClick = { currentScreen = Screen.CountryList }
            ) { modifier ->
                RecipeListScreen(
                    viewModel = recipeListViewModel,
                    onBack = { currentScreen = Screen.CountryList },
                    onRecipeClick = { recipeId ->
                        selectedRecipe = recipeId

                        val recipe = recipeState.recipes.find { it.recipeId == recipeId }
                        selectedRecipeName = recipe?.titleTurkish ?: recipe?.title ?: ""

                        currentScreen = Screen.CoopModeSelection
                    },
                    modifier = modifier
                )
            }
        }

        Screen.Profile -> {
            MainScaffold(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                onBackClick = { /* No back for profile */ }
            ) { modifier ->
                ProfileScreen(
                    coupleName = coupleState.currentCouple?.coupleName ?: "Çiftiniz",
                    userName = authState.currentUser?.email?.substringBefore("@") ?: "Kullanıcı",
                    onLogout = {
                        // Önce tüm Firebase listener'ları durdur
                        cookingSessionViewModel.stopObservingWaitingSession()

                        // Sonra logout yap
                        authViewModel.onEvent(com.emirhankarci.seninlemutfakta.presentation.auth.AuthEvent.Logout)
                        coupleViewModel.clearCoupleData()
                        currentUserGender = null
                        currentScreen = Screen.Login
                    },
                    modifier = modifier
                )
            }
        }

        Screen.Login -> {
            MainScaffold(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                onBackClick = { /* No back for login */ }
            ) { modifier ->
                LoginScreen(
                    state = authState,
                    onEvent = authViewModel::onEvent,
                    onNavigateToRegister = { currentScreen = Screen.Register },
                    onLoginSuccess = { currentScreen = Screen.UserSelection }
                )
            }
        }

        Screen.Register -> {
            MainScaffold(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                onBackClick = { /* No back for register */ }
            ) { modifier ->
                RegisterScreen(
                    state = authState,
                    onEvent = authViewModel::onEvent,
                    onNavigateToLogin = { currentScreen = Screen.Login },
                    onRegisterSuccess = { currentScreen = Screen.UserSelection }
                )
            }
        }

        Screen.UserSelection -> {
            MainScaffold(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                onBackClick = { /* No back for user selection */ }
            ) { modifier ->
                UserSelectionScreen(
                    coupleName = coupleState.currentCouple?.coupleName ?: "Çiftiniz",
                    onGenderSelected = { gender ->
                        currentUserGender = gender
                        currentScreen = Screen.CountryList
                    },
                    onLogout = {
                        // Önce tüm Firebase listener'ları durdur
                        cookingSessionViewModel.stopObservingWaitingSession()

                        // Sonra logout yap
                        authViewModel.onEvent(com.emirhankarci.seninlemutfakta.presentation.auth.AuthEvent.Logout)
                        coupleViewModel.clearCoupleData()
                        currentUserGender = null
                        currentScreen = Screen.Login
                    }
                )
            }
        }

        Screen.CoopModeSelection -> {
            val scope = rememberCoroutineScope()

            MainScaffold(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                onBackClick = { currentScreen = Screen.RecipeList }
            ) { modifier ->
                CoopModeSelectionScreen(
                    recipeName = selectedRecipeName,
                    onSoloMode = {
                        isCoopMode = false
                        // Solo mode: Profil genderını kullan, direkt session'a git
                        currentUserGender?.let { gender ->
                            val femaleId = if (gender == Gender.FEMALE) currentUserId else "waiting_for_partner"
                            val maleId = if (gender == Gender.MALE) currentUserId else "waiting_for_partner"

                            cookingSessionViewModel.onEvent(
                                CookingSessionEvent.StartSession(
                                    recipeId = selectedRecipe,
                                    countryCode = selectedCountry,
                                    isCoopMode = false,
                                    coupleId = coupleId,
                                    femaleUserId = femaleId,
                                    maleUserId = maleId,
                                    currentUserGender = gender
                                )
                            )
                            currentScreen = Screen.CookingSession
                        }
                    },
                    onCoopMode = {
                        isCoopMode = true
                        // Coop mode: Profil genderını kullan, session oluştur/katıl
                        currentUserGender?.let { gender ->
                            scope.launch {
                                // 1. Önce mevcut waiting session kontrolü yap
                                val existingSession = cookingSessionViewModel.checkAndGetWaitingSession(coupleId, selectedRecipe)

                                if (existingSession != null) {
                                    // Mevcut session bulundu, katıl
                                    cookingSessionViewModel.onEvent(
                                        CookingSessionEvent.JoinWaitingSession(
                                            sessionId = existingSession.sessionId,
                                            currentUserGender = gender
                                        )
                                    )
                                } else {
                                    // 2. Waiting session yok, atomic session creation dene
                                    val femaleId = if (gender == Gender.FEMALE) currentUserId else "waiting_for_partner"
                                    val maleId = if (gender == Gender.MALE) currentUserId else "waiting_for_partner"

                                    // Atomic session creation/join - race condition önleyici
                                    cookingSessionViewModel.onEvent(
                                        CookingSessionEvent.CreateOrJoinSession(
                                            recipeId = selectedRecipe,
                                            countryCode = selectedCountry,
                                            isCoopMode = true,
                                            coupleId = coupleId,
                                            femaleUserId = femaleId,
                                            maleUserId = maleId,
                                            currentUserGender = gender
                                        )
                                    )
                                }
                                currentScreen = Screen.CookingSession
                            }
                        }
                    }
                )
            }
        }

        Screen.CookingSession -> {
            MainScaffold(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                onBackClick = { currentScreen = Screen.RecipeList }
            ) { modifier ->
                CookingSessionScreen(
                    state = cookingState,
                    onEvent = cookingSessionViewModel::onEvent,
                    onBack = { currentScreen = Screen.RecipeList }
                )
            }
        }
    }

    // Waiting Dialog - Eş bekliyor bildirimi
    if (cookingState.showWaitingForPartnerDialog && currentScreen != Screen.CookingSession) {
        WaitingForPartnerDialog(
            recipeName = cookingState.recipe?.titleTurkish ?: cookingState.recipe?.title ?: "Tarif",
            partnerName = "Eşiniz",
            onCancel = {
                cookingSessionViewModel.onEvent(CookingSessionEvent.DismissWaitingDialog)
            },
            onJoin = {
                val session = cookingState.session
                if (session != null && currentUserGender != null) {
                    // Session varsa bilgileri ayarla ve direkt katıl
                    selectedCountry = session.countryCode
                    selectedRecipe = session.recipeId
                    isCoopMode = session.isCoopMode

                    // Dialog'u kapat
                    cookingSessionViewModel.onEvent(CookingSessionEvent.DismissWaitingDialog)

                    // Profil genderı ile session'a katıl
                    cookingSessionViewModel.onEvent(
                        CookingSessionEvent.JoinWaitingSession(
                            sessionId = session.sessionId,
                            currentUserGender = currentUserGender!!
                        )
                    )

                    // CookingSession ekranına git
                    currentScreen = Screen.CookingSession
                }
            }
        )
    }
}
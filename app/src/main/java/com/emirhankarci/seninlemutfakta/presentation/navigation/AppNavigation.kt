package com.emirhankarci.seninlemutfakta.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.emirhankarci.seninlemutfakta.data.model.Gender
import com.emirhankarci.seninlemutfakta.presentation.MainScaffold
import com.emirhankarci.seninlemutfakta.presentation.auth.AuthViewModel
import com.emirhankarci.seninlemutfakta.presentation.auth.LoginScreen
import com.emirhankarci.seninlemutfakta.presentation.auth.RegisterScreen
import com.emirhankarci.seninlemutfakta.presentation.auth.UserSelectionScreen
import com.emirhankarci.seninlemutfakta.presentation.cooking.CookingSessionEvent
import com.emirhankarci.seninlemutfakta.presentation.cooking.CookingSessionViewModel
import com.emirhankarci.seninlemutfakta.presentation.cooking.components.WaitingForPartnerDialog
import com.emirhankarci.seninlemutfakta.presentation.cooking.screens.CookingSessionScreen
import com.emirhankarci.seninlemutfakta.presentation.cooking.screens.CoopModeSelectionScreen
import com.emirhankarci.seninlemutfakta.presentation.countries.CountryListHeader
import com.emirhankarci.seninlemutfakta.presentation.countries.CountryListScreen
import com.emirhankarci.seninlemutfakta.presentation.countries.CountryListViewModel
import com.emirhankarci.seninlemutfakta.presentation.couple.CoupleViewModel
import com.emirhankarci.seninlemutfakta.presentation.profile.ProfileScreen
import com.emirhankarci.seninlemutfakta.presentation.recipes.RecipeListEvent
import com.emirhankarci.seninlemutfakta.presentation.recipes.RecipeListHeader
import com.emirhankarci.seninlemutfakta.presentation.recipes.RecipeListScreen
import com.emirhankarci.seninlemutfakta.presentation.recipes.RecipeListViewModel
import kotlinx.coroutines.launch
import com.emirhankarci.seninlemutfakta.presentation.recipes.RecipeFilter // DÜZELTME: Bu import satırı hatayı giderir.

@OptIn(ExperimentalMaterial3Api::class)
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

    var currentScreen by remember {
        mutableStateOf<Screen>(
            if (!authState.isLoggedIn) Screen.Login else Screen.UserSelection
        )
    }

    LaunchedEffect(authState.isLoggedIn) {
        currentScreen = if (!authState.isLoggedIn) Screen.Login else Screen.UserSelection
    }

    var selectedCountry by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf("") }
    var selectedRecipeName by remember { mutableStateOf("") }
    var isCoopMode by remember { mutableStateOf(false) }

    val currentUserId = authState.currentUser?.uid ?: ""
    var currentUserGender by remember { mutableStateOf<Gender?>(null) }
    val coupleId = authState.currentUser?.uid ?: ""

    val cookingState by cookingSessionViewModel.state.collectAsState()

    LaunchedEffect(coupleId, currentUserGender) {
        if (coupleId.isNotEmpty() && currentUserGender != null) {
            cookingSessionViewModel.observeWaitingSessionForCouple(coupleId)
        }
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.CookingSession) {
            cookingSessionViewModel.stopObservingWaitingSession()
        }
    }

    when (currentScreen) {
        Screen.CountryList -> {
            var selectedFilter by remember { mutableStateOf("All Countries") }

            MainScaffold(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                topBar = {
                    CountryListHeader(
                        selectedFilter = selectedFilter,
                        onFilterChange = { newFilter -> selectedFilter = newFilter }
                    )
                }
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
                    selectedFilter = selectedFilter,
                    modifier = modifier
                )
            }
        }

        Screen.RecipeList -> {
            val recipeState by recipeListViewModel.state.collectAsState()
            // DEĞİŞİKLİK 1: 'selectedFilter' state'ini buraya taşıdık.
            var selectedFilter by remember { mutableStateOf(RecipeFilter.ALL) }

            MainScaffold(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                // DEĞİŞİKLİK 2: 'topBar' parametresine RecipeListHeader'ı veriyoruz.
                topBar = {
                    RecipeListHeader(
                        countryName = recipeState.countryName,
                        countryFlag = recipeState.countryFlagEmoji,
                        totalRecipes = recipeState.recipes.size,
                        completedRecipes = recipeState.getCompletedCount(),
                        progressPercentage = recipeState.getProgressPercentage(),
                        selectedFilter = selectedFilter,
                        onFilterChange = { newFilter -> selectedFilter = newFilter },
                        onBack = { currentScreen = Screen.CountryList }
                    )
                }
            ) { modifier ->
                // DEĞİŞİKLİK 3: RecipeListScreen'i yeni parametrelerle çağırıyoruz.
                RecipeListScreen(
                    viewModel = recipeListViewModel,
                    onBack = { currentScreen = Screen.CountryList },
                    onRecipeClick = { recipeId ->
                        selectedRecipe = recipeId
                        val recipe = recipeState.recipes.find { it.recipeId == recipeId }
                        selectedRecipeName = recipe?.titleTurkish ?: recipe?.title ?: ""
                        currentScreen = Screen.CoopModeSelection
                    },
                    selectedFilter = selectedFilter, // State'i aşağıya paslıyoruz
                    modifier = modifier
                )
            }
        }

        Screen.Profile -> {
            MainScaffold(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = currentScreen.title,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            ) { modifier ->
                ProfileScreen(
                    coupleName = coupleState.currentCouple?.coupleName ?: "Çiftiniz",
                    userName = authState.currentUser?.email?.substringBefore("@") ?: "Kullanıcı",
                    onLogout = {
                        cookingSessionViewModel.stopObservingWaitingSession()
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
                topBar = {}
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
                topBar = {}
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
                topBar = {}
            ) { modifier ->
                UserSelectionScreen(
                    coupleName = coupleState.currentCouple?.coupleName ?: "Çiftiniz",
                    onGenderSelected = { gender ->
                        currentUserGender = gender
                        currentScreen = Screen.CountryList
                    },
                    onLogout = {
                        cookingSessionViewModel.stopObservingWaitingSession()
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
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = currentScreen.title,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { currentScreen = Screen.RecipeList }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            ) { modifier ->
                CoopModeSelectionScreen(
                    recipeName = selectedRecipeName,
                    onSoloMode = {
                        isCoopMode = false
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
                        currentUserGender?.let { gender ->
                            scope.launch {
                                val existingSession = cookingSessionViewModel.checkAndGetWaitingSession(coupleId, selectedRecipe)

                                if (existingSession != null) {
                                    cookingSessionViewModel.onEvent(
                                        CookingSessionEvent.JoinWaitingSession(
                                            sessionId = existingSession.sessionId,
                                            currentUserGender = gender
                                        )
                                    )
                                } else {
                                    val femaleId = if (gender == Gender.FEMALE) currentUserId else "waiting_for_partner"
                                    val maleId = if (gender == Gender.MALE) currentUserId else "waiting_for_partner"

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
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = currentScreen.title,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { currentScreen = Screen.RecipeList }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            ) { modifier ->
                CookingSessionScreen(
                    state = cookingState,
                    onEvent = cookingSessionViewModel::onEvent,
                    onBack = { currentScreen = Screen.RecipeList }
                )
            }
        }
    }

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
                    selectedCountry = session.countryCode
                    selectedRecipe = session.recipeId
                    isCoopMode = session.isCoopMode

                    cookingSessionViewModel.onEvent(CookingSessionEvent.DismissWaitingDialog)

                    cookingSessionViewModel.onEvent(
                        CookingSessionEvent.JoinWaitingSession(
                            sessionId = session.sessionId,
                            currentUserGender = currentUserGender!!
                        )
                    )
                    currentScreen = Screen.CookingSession
                }
            }
        )
    }
}
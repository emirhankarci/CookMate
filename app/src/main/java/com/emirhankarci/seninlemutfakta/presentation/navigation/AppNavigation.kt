package com.emirhankarci.seninlemutfakta.presentation.navigation

import androidx.activity.compose.BackHandler
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
import com.emirhankarci.seninlemutfakta.data.model.SessionStatus
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
import com.emirhankarci.seninlemutfakta.presentation.cooking.screens.CookingScreenHeader
import com.emirhankarci.seninlemutfakta.presentation.profile.ProfileHeader
import com.emirhankarci.seninlemutfakta.presentation.welcome.WelcomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    coupleViewModel: CoupleViewModel,
    countryListViewModel: CountryListViewModel,
    recipeListViewModel: RecipeListViewModel,
    cookingSessionViewModel: CookingSessionViewModel,
    userSelectionViewModel: com.emirhankarci.seninlemutfakta.presentation.auth.UserSelectionViewModel,
    onUserGenderChanged: (Gender?, String?) -> Unit = { _, _ -> }
) {
    val authState by authViewModel.state.collectAsState()
    val coupleState by coupleViewModel.state.collectAsState()

    var currentScreen by remember {
        mutableStateOf<Screen>(
            if (!authState.isLoggedIn) Screen.Welcome else Screen.UserSelection
        )
    }

    LaunchedEffect(authState.isLoggedIn) {
        currentScreen = if (!authState.isLoggedIn) Screen.Welcome else Screen.UserSelection
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
            // Clean up old sessions first
            cookingSessionViewModel.cleanUpOldSessions(coupleId)
            // Then start observing
            cookingSessionViewModel.observeWaitingSessionForCouple(coupleId, currentUserId, currentUserGender)
        }
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.CookingSession) {
            cookingSessionViewModel.stopObservingWaitingSession()
        }
    }

    // Navigate to CookingSession when session status becomes IN_PROGRESS (for creator)
    LaunchedEffect(cookingState.session?.status, cookingState.recipe, cookingState.isCreatorWaitingForPartner) {
        val session = cookingState.session
        val recipe = cookingState.recipe
        // Creator waiting for partner: Navigate when status becomes IN_PROGRESS
        if (session != null &&
            recipe != null &&
            session.status == SessionStatus.IN_PROGRESS &&
            currentScreen != Screen.CookingSession &&
            cookingState.isCreatorWaitingForPartner) {
            currentScreen = Screen.CookingSession
        }
    }

    when (currentScreen) {
        Screen.CountryList -> {
            var selectedFilter by remember { mutableStateOf("All Countries") }

            // Handle back button - go to user selection
            BackHandler {
                // Unlock profile when going back to user selection
                currentUserGender?.let { gender ->
                    userSelectionViewModel.onEvent(
                        com.emirhankarci.seninlemutfakta.presentation.auth.UserSelectionEvent.UnlockProfile(
                            coupleId = coupleId,
                            gender = gender
                        )
                    )
                }
                currentUserGender = null
                onUserGenderChanged(null, null)
                currentScreen = Screen.UserSelection
            }

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

            // Handle back button - go to country list
            BackHandler {
                currentScreen = Screen.CountryList
            }

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
            // Handle back button - go to country list
            BackHandler {
                currentScreen = Screen.CountryList
            }

            MainScaffold(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                // DEĞİŞİKLİK 2: 'topBar' slotunu yeni ProfileHeader'ımız ile değiştiriyoruz.
                topBar = {
                    ProfileHeader(
                        userName = authState.currentUser?.email?.substringBefore("@") ?: "Kullanıcı",
                        coupleName = coupleState.currentCouple?.coupleName ?: "Çiftiniz",
                        userGender = currentUserGender
                    )
                }
            ) { modifier ->
                // DEĞİŞİKLİK 3: ProfileScreen artık sadece logout fonksiyonuna ve modifier'a ihtiyaç duyuyor.
                ProfileScreen(
                    onLogout = {
                        // Unlock profile before logging out
                        currentUserGender?.let { gender ->
                            userSelectionViewModel.onEvent(
                                com.emirhankarci.seninlemutfakta.presentation.auth.UserSelectionEvent.UnlockProfile(
                                    coupleId = coupleId,
                                    gender = gender
                                )
                            )
                        }
                        cookingSessionViewModel.stopObservingWaitingSession()
                        authViewModel.onEvent(com.emirhankarci.seninlemutfakta.presentation.auth.AuthEvent.Logout)
                        coupleViewModel.clearCoupleData()
                        currentUserGender = null
                        onUserGenderChanged(null, null)
                        currentScreen = Screen.Welcome
                    },
                    modifier = modifier
                )
            }
        }

        Screen.Welcome -> {
            WelcomeScreen(
                onNavigateToLogin = { currentScreen = Screen.Login },
                onNavigateToRegister = { currentScreen = Screen.Register }
            )
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
                    onLoginSuccess = { currentScreen = Screen.UserSelection },
                    onBackToWelcome = { currentScreen = Screen.Welcome }
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
                    viewModel = userSelectionViewModel,
                    userId = currentUserId,
                    coupleId = coupleId,
                    coupleName = coupleState.currentCouple?.coupleName ?: "Çiftiniz",
                    onGenderSelected = { gender ->
                        currentUserGender = gender
                        onUserGenderChanged(gender, coupleId)
                        currentScreen = Screen.CountryList
                    },
                    onLogout = {
                        // Unlock profile before logging out
                        currentUserGender?.let { gender ->
                            userSelectionViewModel.onEvent(
                                com.emirhankarci.seninlemutfakta.presentation.auth.UserSelectionEvent.UnlockProfile(
                                    coupleId = coupleId,
                                    gender = gender
                                )
                            )
                        }
                        cookingSessionViewModel.stopObservingWaitingSession()
                        authViewModel.onEvent(com.emirhankarci.seninlemutfakta.presentation.auth.AuthEvent.Logout)
                        coupleViewModel.clearCoupleData()
                        currentUserGender = null
                        onUserGenderChanged(null, null)
                        currentScreen = Screen.Login
                    }
                )
            }
        }

        Screen.CoopModeSelection -> {
            val scope = rememberCoroutineScope()

            // Handle back button - go to recipe list
            BackHandler {
                currentScreen = Screen.RecipeList
            }

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
                    onDismiss = {
                        currentScreen = Screen.RecipeList
                    },
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
                                    // Joining existing session - navigate immediately
                                    cookingSessionViewModel.onEvent(
                                        CookingSessionEvent.JoinWaitingSession(
                                            sessionId = existingSession.sessionId,
                                            currentUserGender = gender
                                        )
                                    )
                                    currentScreen = Screen.CookingSession
                                } else {
                                    // Creating new session - don't navigate yet, wait for partner
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
                                    // Navigation will happen automatically via LaunchedEffect when partner joins
                                }
                            }
                        }
                    }
                )
            }
        }

        Screen.CookingSession -> {
            var showExitConfirmation by remember { mutableStateOf(false) }

            // Handle back button - show confirmation
            BackHandler {
                showExitConfirmation = true
            }

            // Exit confirmation dialog
            if (showExitConfirmation) {
                com.emirhankarci.seninlemutfakta.presentation.components.ConfirmationDialog(
                    title = "Emin Misiniz?",
                    message = "Pişirme oturumundan çıkmak istediğinize emin misiniz? Kaydedilmemiş ilerlemeniz kaybolabilir.",
                    confirmText = "Evet, Çık",
                    dismissText = "İptal",
                    onConfirm = {
                        showExitConfirmation = false
                        // Reset session state and restart observers
                        cookingSessionViewModel.onEvent(CookingSessionEvent.ResetSessionState)
                        if (coupleId.isNotEmpty() && currentUserGender != null) {
                            cookingSessionViewModel.observeWaitingSessionForCouple(coupleId, currentUserId, currentUserGender)
                        }
                        currentScreen = Screen.RecipeList
                    },
                    onDismiss = {
                        showExitConfirmation = false
                    }
                )
            }

            MainScaffold(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                // DEĞİŞİKLİK: 'topBar' slotuna yeni ve temiz Header'ımızı yerleştiriyoruz.
                topBar = {
                    val recipeName = cookingState.recipe?.titleTurkish
                        ?: cookingState.recipe?.title
                        ?: "Pişirme Ekranı"

                    CookingScreenHeader(
                        recipeName = recipeName,
                        onBack = {
                            // Reset session state and restart observers
                            cookingSessionViewModel.onEvent(CookingSessionEvent.ResetSessionState)
                            if (coupleId.isNotEmpty() && currentUserGender != null) {
                                cookingSessionViewModel.observeWaitingSessionForCouple(coupleId, currentUserId, currentUserGender)
                            }
                            currentScreen = Screen.RecipeList
                        }
                    )
                }
            ) { modifier ->
                // Scaffold'un content'i olarak CookingSessionScreen'i veriyoruz.
                CookingSessionScreen(
                    state = cookingState,
                    onEvent = cookingSessionViewModel::onEvent,
                    onBack = {
                        // Reset session state and restart observers
                        cookingSessionViewModel.onEvent(CookingSessionEvent.ResetSessionState)
                        if (coupleId.isNotEmpty() && currentUserGender != null) {
                            cookingSessionViewModel.observeWaitingSessionForCouple(coupleId, currentUserId, currentUserGender)
                        }
                        currentScreen = Screen.RecipeList
                    }
                )
            }
        }
    }

    // Show dialog for partner who needs to join
    if (cookingState.showWaitingForPartnerDialog && currentScreen != Screen.CookingSession) {
        WaitingForPartnerDialog(
            recipeName = cookingState.recipe?.titleTurkish ?: cookingState.recipe?.title ?: "Tarif",
            partnerName = "Eşiniz",
            onCancel = {
                cookingSessionViewModel.onEvent(CookingSessionEvent.CancelWaitingSession)
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

    // Show dialog for creator who is waiting for partner
    if (cookingState.isCreatorWaitingForPartner && currentScreen != Screen.CookingSession) {
        com.emirhankarci.seninlemutfakta.presentation.cooking.components.CreatorWaitingDialog(
            recipeName = cookingState.recipe?.titleTurkish ?: cookingState.recipe?.title ?: "Tarif",
            onCancel = {
                cookingSessionViewModel.onEvent(CookingSessionEvent.CancelWaitingSession)
            }
        )
    }

    // Show dialog when partner leaves during cooking session
    if (cookingState.showPartnerLeftDialog) {
        com.emirhankarci.seninlemutfakta.presentation.cooking.components.PartnerLeftDialog(
            onBackToRecipes = {
                cookingSessionViewModel.onEvent(CookingSessionEvent.DismissPartnerLeftDialog)
                // Reset session state and restart observers
                cookingSessionViewModel.onEvent(CookingSessionEvent.ResetSessionState)
                if (coupleId.isNotEmpty() && currentUserGender != null) {
                    cookingSessionViewModel.observeWaitingSessionForCouple(coupleId, currentUserId, currentUserGender)
                }
                currentScreen = Screen.RecipeList
            }
        )
    }
}
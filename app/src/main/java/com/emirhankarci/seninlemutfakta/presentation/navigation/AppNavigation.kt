package com.emirhankarci.seninlemutfakta.presentation.navigation

import androidx.compose.runtime.*
import com.emirhankarci.seninlemutfakta.data.model.CookingSession
import com.emirhankarci.seninlemutfakta.data.model.Gender
import com.emirhankarci.seninlemutfakta.presentation.auth.AuthViewModel
import com.emirhankarci.seninlemutfakta.presentation.auth.LoginScreen
import com.emirhankarci.seninlemutfakta.presentation.auth.RegisterScreen
import com.emirhankarci.seninlemutfakta.presentation.couple.CoupleSetupScreen
import com.emirhankarci.seninlemutfakta.presentation.couple.CoupleViewModel
import com.emirhankarci.seninlemutfakta.presentation.cooking.CookingSessionEvent
import com.emirhankarci.seninlemutfakta.presentation.cooking.CookingSessionViewModel
import com.emirhankarci.seninlemutfakta.presentation.cooking.components.WaitingForPartnerDialog
import com.emirhankarci.seninlemutfakta.presentation.cooking.screens.CookingSessionScreen
import com.emirhankarci.seninlemutfakta.presentation.cooking.screens.CoopModeSelectionScreen
import com.emirhankarci.seninlemutfakta.presentation.cooking.screens.GenderSelectionScreen
import com.emirhankarci.seninlemutfakta.presentation.countries.CountryListEvent
import com.emirhankarci.seninlemutfakta.presentation.countries.CountryListScreen
import com.emirhankarci.seninlemutfakta.presentation.countries.CountryListViewModel
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
    
    // Authentication ve couple durumuna göre başlangıç ekranını belirle
    var currentScreen by remember { 
        mutableStateOf<Screen>(
            when {
                !authState.isLoggedIn -> Screen.Login
                !coupleState.hasCouple -> Screen.CoupleSetup
                else -> Screen.CountryList
            }
        )
    }

    // Auth ve couple durumu değiştiğinde ekranı güncelle
    LaunchedEffect(authState.isLoggedIn, coupleState.hasCouple) {
        currentScreen = when {
            !authState.isLoggedIn -> Screen.Login
            !coupleState.hasCouple -> Screen.CoupleSetup
            else -> Screen.CountryList
        }
    }
    var selectedCountry by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf("") }
    var selectedRecipeName by remember { mutableStateOf("") }
    var isCoopMode by remember { mutableStateOf(false) }

    // Kullanıcı bilgileri - Firebase Auth ve Couple'dan al
    val currentUserId = authState.currentUser?.uid ?: ""
    val currentUserGender = coupleState.currentCouple?.getUserGender(currentUserId) ?: Gender.FEMALE
    val coupleId = coupleState.currentCouple?.coupleId ?: ""

    val cookingState by cookingSessionViewModel.state.collectAsState()

    // User seçilince couple için waiting session kontrol et
    LaunchedEffect(coupleId) {
        if (coupleId.isNotEmpty()) {
            cookingSessionViewModel.checkAnyWaitingSessionForCouple(coupleId)
        }
    }

    when (currentScreen) {
        Screen.Login -> {
            LoginScreen(
                state = authState,
                onEvent = authViewModel::onEvent,
                onNavigateToRegister = { currentScreen = Screen.Register },
                onLoginSuccess = { currentScreen = Screen.CoupleSetup }
            )
        }

        Screen.Register -> {
            RegisterScreen(
                state = authState,
                onEvent = authViewModel::onEvent,
                onNavigateToLogin = { currentScreen = Screen.Login },
                onRegisterSuccess = { currentScreen = Screen.CoupleSetup }
            )
        }

        Screen.CoupleSetup -> {
            CoupleSetupScreen(
                state = coupleState,
                onEvent = coupleViewModel::onEvent,
                onCoupleReady = { _, _ ->
                    currentScreen = Screen.CountryList
                },
                onLogout = {
                    authViewModel.onEvent(com.emirhankarci.seninlemutfakta.presentation.auth.AuthEvent.Logout)
                    coupleViewModel.clearCoupleData() // Couple data'sını temizle
                    currentScreen = Screen.Login
                }
            )
        }

        Screen.CountryList -> {
            // Çift bilgilerini hazırla
            val coupleInfo = coupleState.currentCouple?.let { couple ->
                when {
                    couple.isComplete -> "✅ Çift Tamamlandı!\nDavet Kodu: ${couple.inviteCode}"
                    couple.needsPartner() -> "⏳ Eş Bekleniyor...\nDavet Kodu: ${couple.inviteCode}\n(Eşinizle paylaşın)"
                    else -> "💕 Çift Aktif"
                }
            } ?: ""

            CountryListScreen(
                viewModel = countryListViewModel,
                onCountryClick = { countryCode ->
                    selectedCountry = countryCode
                    recipeListViewModel.onEvent(
                        RecipeListEvent.LoadRecipes(countryCode)
                    )
                    currentScreen = Screen.RecipeList
                },
                onLogout = {
                    authViewModel.onEvent(com.emirhankarci.seninlemutfakta.presentation.auth.AuthEvent.Logout)
                    coupleViewModel.clearCoupleData() // Couple data'sını temizle
                    currentScreen = Screen.Login
                },
                coupleInfo = coupleInfo
            )
        }

        Screen.RecipeList -> {
            val recipeState by recipeListViewModel.state.collectAsState()

            RecipeListScreen(
                viewModel = recipeListViewModel,
                onBack = { currentScreen = Screen.CountryList },
                onRecipeClick = { recipeId ->
                    selectedRecipe = recipeId

                    val recipe = recipeState.recipes.find { it.recipeId == recipeId }
                    selectedRecipeName = recipe?.titleTurkish ?: recipe?.title ?: ""

                    currentScreen = Screen.CoopModeSelection
                }
            )
        }

        Screen.CoopModeSelection -> {
            var shouldProceedToGenderSelection by remember { mutableStateOf(false) }

            CoopModeSelectionScreen(
                recipeName = selectedRecipeName,
                onSoloMode = {
                    isCoopMode = false
                    currentScreen = Screen.GenderSelection
                },
                onCoopMode = {
                    isCoopMode = true
                    // Coop mode seçildiğinde bu tarif için waiting session kontrolü yap
                    cookingSessionViewModel.checkWaitingSessionForCouple(coupleId, selectedRecipe)
                    shouldProceedToGenderSelection = true
                }
            )

            // Waiting session kontrolü sonrası yönlendirme
            LaunchedEffect(shouldProceedToGenderSelection, cookingState.showWaitingForPartnerDialog) {
                if (shouldProceedToGenderSelection) {
                    // State güncellenmesi için kısa bir süre bekle
                    kotlinx.coroutines.delay(500)

                    // Eğer waiting session dialog gösterilmediyse GenderSelection'a git
                    if (!cookingState.showWaitingForPartnerDialog) {
                        currentScreen = Screen.GenderSelection
                    }
                    shouldProceedToGenderSelection = false
                }
            }
        }

        Screen.GenderSelection -> {
            val scope = rememberCoroutineScope()

            GenderSelectionScreen(
                onGenderSelected = { gender ->
                    if (isCoopMode) {
                        // CoopMode: Atomic session creation/join işlemi
                        scope.launch {
                            // 1. Önce mevcut session kontrolü yap
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
                                        isCoopMode = isCoopMode,
                                        coupleId = coupleId,
                                        femaleUserId = femaleId,
                                        maleUserId = maleId,
                                        currentUserGender = gender
                                    )
                                )
                            }
                            currentScreen = Screen.CookingSession
                        }
                    } else {
                        // Solo mode: Direkt yeni session oluştur
                        val femaleId = if (gender == Gender.FEMALE) currentUserId else "waiting_for_partner"
                        val maleId = if (gender == Gender.MALE) currentUserId else "waiting_for_partner"

                        cookingSessionViewModel.onEvent(
                            CookingSessionEvent.StartSession(
                                recipeId = selectedRecipe,
                                countryCode = selectedCountry,
                                isCoopMode = isCoopMode,
                                coupleId = coupleId,
                                femaleUserId = femaleId,
                                maleUserId = maleId,
                                currentUserGender = gender
                            )
                        )
                        currentScreen = Screen.CookingSession
                    }
                }
            )
        }

        Screen.CookingSession -> {
            CookingSessionScreen(
                state = cookingState,
                onEvent = cookingSessionViewModel::onEvent,
                onBack = { currentScreen = Screen.RecipeList }
            )
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
                if (session != null) {
                    // Session varsa bilgileri ayarla ve GenderSelection'a git
                    selectedCountry = session.countryCode
                    selectedRecipe = session.recipeId
                    isCoopMode = session.isCoopMode

                    // Dialog'u kapat
                    cookingSessionViewModel.onEvent(CookingSessionEvent.DismissWaitingDialog)

                    // Gender seçimi için GenderSelection ekranına git
                    currentScreen = Screen.GenderSelection
                }
            }
        )
    }
}
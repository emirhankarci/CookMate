package com.emirhankarci.seninlemutfakta.presentation.navigation

import androidx.compose.runtime.*
import com.emirhankarci.seninlemutfakta.data.model.CookingSession
import com.emirhankarci.seninlemutfakta.data.model.Gender
import com.emirhankarci.seninlemutfakta.presentation.auth.UserSelectionScreen
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
    countryListViewModel: CountryListViewModel,
    recipeListViewModel: RecipeListViewModel,
    cookingSessionViewModel: CookingSessionViewModel
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.UserSelection) }
    var selectedCountry by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf("") }
    var selectedRecipeName by remember { mutableStateOf("") }
    var isCoopMode by remember { mutableStateOf(false) }

    // Kullanıcı bilgileri
    var currentUserId by remember { mutableStateOf("") }
    var currentUserGender by remember { mutableStateOf(Gender.FEMALE) }
    var coupleId by remember { mutableStateOf("") }

    val cookingState by cookingSessionViewModel.state.collectAsState()

    // User seçilince couple için waiting session kontrol et
    LaunchedEffect(coupleId) {
        if (coupleId.isNotEmpty()) {
            cookingSessionViewModel.checkAnyWaitingSessionForCouple(coupleId)
        }
    }

    when (currentScreen) {
        Screen.UserSelection -> {
            UserSelectionScreen(
                onUserSelected = { userId, gender, couple ->
                    currentUserId = userId
                    currentUserGender = gender
                    coupleId = couple
                    currentScreen = Screen.CountryList
                }
            )
        }

        Screen.CountryList -> {
            CountryListScreen(
                viewModel = countryListViewModel,
                onCountryClick = { countryCode ->
                    selectedCountry = countryCode
                    recipeListViewModel.onEvent(
                        RecipeListEvent.LoadRecipes(countryCode)
                    )
                    currentScreen = Screen.RecipeList
                }
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
                    // ÖNEMLI: Session oluşturmadan önce son bir kez daha waiting session kontrolü yap
                    // Bu race condition'ı önler
                    if (isCoopMode) {
                        // CoopMode'da son bir kontrol yap (suspend fonksiyon kullan)
                        scope.launch {
                            val existingSession = cookingSessionViewModel.checkAndGetWaitingSession(coupleId, selectedRecipe)

                            if (existingSession != null) {
                                // Mevcut session bulundu, katıl
                                cookingSessionViewModel.onEvent(
                                    CookingSessionEvent.JoinWaitingSession(
                                        sessionId = existingSession.sessionId,
                                        currentUserGender = gender
                                    )
                                )
                                currentScreen = Screen.CookingSession
                            } else {
                                // Waiting session yok, yeni session oluştur
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
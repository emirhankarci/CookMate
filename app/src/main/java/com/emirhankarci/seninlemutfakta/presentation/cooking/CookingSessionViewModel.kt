package com.emirhankarci.seninlemutfakta.presentation.cooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emirhankarci.seninlemutfakta.data.model.Gender
import com.emirhankarci.seninlemutfakta.data.model.RecipeStep
import com.emirhankarci.seninlemutfakta.data.model.SessionStatus
import com.emirhankarci.seninlemutfakta.data.repository.CookingSessionRepository
import com.emirhankarci.seninlemutfakta.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CookingSessionViewModel @Inject constructor(
    private val cookingSessionRepository: CookingSessionRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CookingSessionState())
    val state = _state.asStateFlow()

    private var sessionObserverJob: Job? = null
    private var connectionCheckJob: Job? = null

    init {
        checkForWaitingSession()
    }

    fun onEvent(event: CookingSessionEvent) {
        when (event) {
            is CookingSessionEvent.StartSession -> startSession(
                recipeId = event.recipeId,
                countryCode = event.countryCode,
                isCoopMode = event.isCoopMode,
                coupleId = event.coupleId,
                femaleUserId = event.femaleUserId,
                maleUserId = event.maleUserId,
                currentUserGender = event.currentUserGender
            )

            is CookingSessionEvent.JoinWaitingSession -> joinWaitingSession(
                sessionId = event.sessionId,
                currentUserGender = event.currentUserGender
            )

            is CookingSessionEvent.CompleteCurrentStep -> completeCurrentStep()
            is CookingSessionEvent.MoveToNextStep -> moveToNextStep()
            is CookingSessionEvent.PauseSession -> pauseSession()
            is CookingSessionEvent.ResumeSession -> resumeSession()
            is CookingSessionEvent.CompleteSession -> completeSession()

            // Dialog event'leri
            is CookingSessionEvent.ShowCoopModeDialog -> showCoopModeDialog()  // ← EKLE
            is CookingSessionEvent.DismissCoopDialog -> dismissCoopDialog()
            is CookingSessionEvent.DismissWaitingDialog -> dismissWaitingDialog()
            is CookingSessionEvent.DismissCompletionDialog -> dismissCompletionDialog()

            is CookingSessionEvent.ClearError -> clearError()
        }
    }

    // ==================== SESSION BAŞLATMA ====================

    private fun startSession(
        recipeId: String,
        countryCode: String,
        isCoopMode: Boolean,
        coupleId: String,
        femaleUserId: String,
        maleUserId: String,
        currentUserGender: Gender
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Önce tarifi yükle
            firebaseRepository.getRecipe(countryCode, recipeId)
                .onSuccess { recipe ->
                    if (recipe == null) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Tarif bulunamadı"
                            )
                        }
                        return@launch
                    }

                    val totalSteps = recipe.steps.size

                    // Session oluştur
                    cookingSessionRepository.createSession(
                        recipeId = recipeId,
                        countryCode = countryCode,
                        accountId = coupleId,
                        isCoopMode = isCoopMode,
                        femaleUserId = femaleUserId,
                        maleUserId = maleUserId,
                        totalSteps = totalSteps
                    )
                        .onSuccess { sessionId ->
                            _state.update {
                                it.copy(
                                    recipe = recipe,
                                    currentUserGender = currentUserGender,
                                    currentUserId = if (currentUserGender == Gender.FEMALE) femaleUserId else maleUserId,
                                    partnerUserId = if (currentUserGender == Gender.FEMALE) maleUserId else femaleUserId,
                                    isLoading = false
                                )
                            }

                            if (!isCoopMode) {
                                cookingSessionRepository.startSession(sessionId)
                            }

                            // Real-time dinlemeyi başlat
                            observeSession(sessionId)

                            // Connection check başlat
                            startConnectionCheck(sessionId)
                        }
                        .onFailure { exception ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = exception.message ?: "Session oluşturulamadı"
                                )
                            }
                        }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Tarif yüklenemedi"
                        )
                    }
                }
        }
    }

    // ==================== SESSION'A KATILMA ====================

    private fun joinWaitingSession(sessionId: String, currentUserGender: Gender) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Session'ı IN_PROGRESS yap
            cookingSessionRepository.startSession(sessionId)

            // Kullanıcı bilgilerini güncelle
            _state.update {
                it.copy(
                    currentUserGender = currentUserGender,
                    isLoading = false,
                    showWaitingForPartnerDialog = false
                )
            }

            // Real-time dinlemeyi başlat
            observeSession(sessionId)

            // Connection check başlat
            startConnectionCheck(sessionId)
        }
    }

    // ==================== GERÇEK ZAMANLI DİNLEME 🔥 ====================

    private fun observeSession(sessionId: String) {
        sessionObserverJob?.cancel()

        sessionObserverJob = viewModelScope.launch {
            cookingSessionRepository.observeSession(sessionId)
                .collect { session ->
                    if (session == null) return@collect

                    val currentGender = _state.value.currentUserGender

                    // Mevcut kullanıcı ve eş progress'ini ayır
                    val myProgress = session.getProgressByGender(currentGender)
                    val partnerProgress = session.getPartnerProgress(currentGender)

                    // Tarifi yükle (ilk seferde)
                    if (_state.value.recipe == null) {
                        loadRecipe(session.countryCode, session.recipeId)
                    }

                    // Mevcut adımı güncelle
                    val currentStep = _state.value.recipe?.steps?.getOrNull(session.currentStep)

                    // Partner connection status
                    val partnerStatus = when {
                        !partnerProgress.isOnline -> PartnerConnectionStatus.OFFLINE
                        System.currentTimeMillis() - partnerProgress.lastSeen > 30000 -> PartnerConnectionStatus.DISCONNECTED
                        else -> PartnerConnectionStatus.ONLINE
                    }

                    _state.update {
                        it.copy(
                            session = session,
                            currentStep = currentStep,
                            myProgress = myProgress,
                            partnerProgress = partnerProgress,
                            partnerConnectionStatus = partnerStatus
                        )
                    }

                    // İkisi de tamamladıysa otomatik geç
                    if (session.canProceedToNextStep() &&
                        session.status == SessionStatus.IN_PROGRESS) {
                        delay(1000) // 1 saniye bekle (animasyon için)
                        moveToNextStep()
                    }

                    // Session tamamlandıysa dialog göster
                    if (session.status == SessionStatus.COMPLETED) {
                        _state.update { it.copy(showCompletionDialog = true) }
                    }
                }
        }
    }

    // ==================== ADIM TAMAMLAMA ====================

    private fun completeCurrentStep() {
        viewModelScope.launch {
            val session = _state.value.session ?: return@launch
            val currentGender = _state.value.currentUserGender
            val currentStepIndex = session.currentStep

            cookingSessionRepository.completeStep(
                sessionId = session.sessionId,
                gender = currentGender,
                stepIndex = currentStepIndex
            )
                .onFailure { exception ->
                    _state.update {
                        it.copy(error = exception.message ?: "Adım tamamlanamadı")
                    }
                }
        }
    }

    // ==================== SONRAKİ ADIMA GEÇ ====================

    private fun moveToNextStep() {
        viewModelScope.launch {
            val session = _state.value.session ?: return@launch
            val nextStepIndex = session.currentStep + 1

            // Son adımsa session'ı tamamla
            if (nextStepIndex >= session.totalSteps) {
                completeSession()
                return@launch
            }

            cookingSessionRepository.moveToNextStep(
                sessionId = session.sessionId,
                nextStepIndex = nextStepIndex
            )
                .onFailure { exception ->
                    _state.update {
                        it.copy(error = exception.message ?: "Sonraki adıma geçilemedi")
                    }
                }
        }
    }

    // ==================== SESSION YÖNETİMİ ====================

    private fun pauseSession() {
        viewModelScope.launch {
            val session = _state.value.session ?: return@launch

            cookingSessionRepository.pauseSession(session.sessionId)
                .onFailure { exception ->
                    _state.update {
                        it.copy(error = exception.message ?: "Session durdurulamadı")
                    }
                }
        }
    }

    private fun resumeSession() {
        viewModelScope.launch {
            val session = _state.value.session ?: return@launch

            cookingSessionRepository.startSession(session.sessionId)
                .onFailure { exception ->
                    _state.update {
                        it.copy(error = exception.message ?: "Session devam ettirilemedi")
                    }
                }
        }
    }

    private fun completeSession() {
        viewModelScope.launch {
            val session = _state.value.session ?: return@launch

            cookingSessionRepository.completeSession(session.sessionId)
                .onSuccess {
                    _state.update { it.copy(showCompletionDialog = true) }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(error = exception.message ?: "Session tamamlanamadı")
                    }
                }
        }
    }

    // ==================== CONNECTION CHECK ====================

    private fun startConnectionCheck(sessionId: String) {
        connectionCheckJob?.cancel()

        connectionCheckJob = viewModelScope.launch {
            while (true) {
                val currentGender = _state.value.currentUserGender

                cookingSessionRepository.updateOnlineStatus(
                    sessionId = sessionId,
                    gender = currentGender,
                    isOnline = true
                )

                delay(10000) // Her 10 saniyede bir güncelle
            }
        }
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun loadRecipe(countryCode: String, recipeId: String) {
        viewModelScope.launch {
            firebaseRepository.getRecipe(countryCode, recipeId)
                .onSuccess { recipe ->
                    _state.update { it.copy(recipe = recipe) }
                }
        }
    }

    private fun checkForWaitingSession() {
        viewModelScope.launch {
            // TEST için şimdilik female user ID kullan
            val userId = "test_female_001"  // Gerçekte currentUserId olacak

            cookingSessionRepository.getWaitingSessionForUser(userId)
                .onSuccess { session ->
                    if (session != null && session.status == SessionStatus.WAITING) {
                        // Eş bekliyor, dialog göster
                        _state.update {
                            it.copy(
                                session = session,
                                showWaitingForPartnerDialog = true
                            )
                        }

                        // Recipe bilgisini yükle
                        loadRecipe(session.countryCode, session.recipeId)
                    }
                }
        }
    }

    private fun showCoopModeDialog() {
        _state.update { it.copy(showCoopModeDialog = true) }
    }

    private fun dismissCoopDialog() {
        _state.update { it.copy(showCoopModeDialog = false) }
    }

    private fun dismissWaitingDialog() {
        _state.update { it.copy(showWaitingForPartnerDialog = false) }
    }

    private fun dismissCompletionDialog() {
        _state.update { it.copy(showCompletionDialog = false) }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    // ==================== CLEANUP ====================

    override fun onCleared() {
        super.onCleared()

        // Session sonlanırken offline işaretle
        viewModelScope.launch {
            val session = _state.value.session
            val currentGender = _state.value.currentUserGender

            if (session != null) {
                cookingSessionRepository.updateOnlineStatus(
                    sessionId = session.sessionId,
                    gender = currentGender,
                    isOnline = false
                )
            }
        }

        sessionObserverJob?.cancel()
        connectionCheckJob?.cancel()
    }
}
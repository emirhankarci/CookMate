package com.emirhankarci.seninlemutfakta.presentation.cooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emirhankarci.seninlemutfakta.data.model.CookingSession
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
    private var connectionObserverJob: Job? = null  // ← YENİ EKLE
    private var timeoutCheckJob: Job? = null


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

            is CookingSessionEvent.JoinSession -> joinSession(  // ← YENİ EKLE
                sessionId = event.sessionId,
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

                    // Total steps: Coop mode'da female/male steps, solo'da normal steps
                    val totalSteps = if (isCoopMode) {
                        // Coop mode: Female ve male steps aynı sayıda olmalı
                        maxOf(recipe.femaleSteps.size, recipe.maleSteps.size)
                    } else {
                        // Solo mode: Normal steps
                        recipe.steps.size
                    }

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

                            // Connection observer başlat
                            startConnectionObserver()

                            // Timeout check başlat
                            startTimeoutCheck()
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

    private fun joinSession(sessionId: String, currentUserGender: Gender) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Session'ı başlat
            cookingSessionRepository.startSession(sessionId)

            // Kullanıcı bilgilerini güncelle
            _state.update {
                it.copy(
                    currentUserGender = currentUserGender,
                    isLoading = false
                )
            }

            // Real-time dinlemeyi başlat
            observeSession(sessionId)

            // Connection check başlat
            startConnectionCheck(sessionId)

            // Connection observer başlat
            startConnectionObserver()

            // Timeout check başlat
            startTimeoutCheck()
        }
    }

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

            // ← YENİ EKLE: Connection observer başlat
            startConnectionObserver()

            // ← YENİ EKLE: Timeout check başlat
            startTimeoutCheck()
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

                    // Mevcut adımı güncelle - Coop mode'a göre farklı adım
                    val currentStep = if (session.isCoopMode) {
                        // Coop mode: Gender'a göre adım seç
                        when (currentGender) {
                            Gender.FEMALE -> _state.value.recipe?.femaleSteps?.getOrNull(session.currentStep)
                            Gender.MALE -> _state.value.recipe?.maleSteps?.getOrNull(session.currentStep)
                        }
                    } else {
                        // Solo mode: Normal steps
                        _state.value.recipe?.steps?.getOrNull(session.currentStep)
                    }

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

                    // Debug: İlerleme kontrolü
                    val canProceed = session.canProceedToNextStep()
                    val status = session.status
                    println("🔍 DEBUG: canProceed=$canProceed, status=$status, isCoopMode=${session.isCoopMode}")
                    println("🔍 Female completed: ${session.femaleProgress.isCompleted}")
                    println("🔍 Male completed: ${session.maleProgress.isCompleted}")

                    // İkisi de tamamladıysa otomatik geç
                    if (session.isCoopMode &&
                        canProceed &&
                        status == SessionStatus.IN_PROGRESS) {
                        delay(1000)
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

                delay(5000) // Her 5 saniyede bir güncelle
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

    // Belirli bir tarif için waiting session kontrolü (CoopMode seçildiğinde çağrılır)
    fun checkWaitingSessionForCouple(coupleId: String, recipeId: String) {
        viewModelScope.launch {
            cookingSessionRepository.getWaitingSessionForCouple(coupleId, recipeId)
                .onSuccess { session ->
                    if (session != null && session.status == SessionStatus.WAITING) {
                        _state.update {
                            it.copy(
                                session = session,
                                showWaitingForPartnerDialog = true
                            )
                        }
                        loadRecipe(session.countryCode, session.recipeId)
                    }
                }
        }
    }

    // Suspend fonksiyon: Waiting session kontrolü yap ve sonucu bekle
    suspend fun checkAndGetWaitingSession(coupleId: String, recipeId: String): CookingSession? {
        var foundSession: CookingSession? = null

        cookingSessionRepository.getWaitingSessionForCouple(coupleId, recipeId)
            .onSuccess { session ->
                if (session != null && session.status == SessionStatus.WAITING) {
                    foundSession = session
                    _state.update {
                        it.copy(
                            session = session,
                            showWaitingForPartnerDialog = false  // Dialog gösterme, direkt join olacak
                        )
                    }
                    loadRecipe(session.countryCode, session.recipeId)
                }
            }

        return foundSession
    }

    // Couple için herhangi bir waiting session kontrolü (User seçildiğinde çağrılır)
    fun checkAnyWaitingSessionForCouple(coupleId: String) {
        viewModelScope.launch {
            cookingSessionRepository.getAnyWaitingSessionForCouple(coupleId)
                .onSuccess { session ->
                    if (session != null && session.status == SessionStatus.WAITING) {
                        _state.update {
                            it.copy(
                                session = session,
                                showWaitingForPartnerDialog = true
                            )
                        }
                        loadRecipe(session.countryCode, session.recipeId)
                    }
                }
        }
    }


    // ==================== CONNECTION MONITORING ====================

    private fun startConnectionObserver() {
        connectionObserverJob?.cancel()

        connectionObserverJob = viewModelScope.launch {
            cookingSessionRepository.observeConnectionStatus()
                .collect { isConnected ->
                    _state.update { it.copy(isConnected = isConnected) }

                    if (!isConnected) {
                        // Bağlantı kesildi, session'ı duraklat
                        val session = _state.value.session
                        if (session != null && session.status == SessionStatus.IN_PROGRESS) {
                            cookingSessionRepository.pauseSession(session.sessionId)
                        }
                    } else {
                        // Bağlantı geri geldi, session'ı devam ettir
                        val session = _state.value.session
                        if (session != null && session.status == SessionStatus.PAUSED) {
                            cookingSessionRepository.startSession(session.sessionId)
                        }
                    }
                }
        }
    }

    private fun startTimeoutCheck() {
        timeoutCheckJob?.cancel()

        timeoutCheckJob = viewModelScope.launch {
            while (true) {
                delay(5000) // Her 5 saniyede kontrol et

                val session = _state.value.session
                val currentGender = _state.value.currentUserGender

                if (session != null && session.isCoopMode) {
                    val partnerProgress = session.getPartnerProgress(currentGender)

                    // Partner timeout kontrolü (60 saniye)
                    val isTimeout = cookingSessionRepository.isPartnerTimeout(
                        lastSeen = partnerProgress.lastSeen,
                        timeoutSeconds = 60  //  60 saniye
                    )

                    val newStatus = when {
                        !partnerProgress.isOnline -> PartnerConnectionStatus.OFFLINE
                        isTimeout -> PartnerConnectionStatus.DISCONNECTED
                        else -> PartnerConnectionStatus.ONLINE
                    }

                    _state.update {
                        it.copy(partnerConnectionStatus = newStatus)
                    }
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

        // Tüm job'ları iptal et
        sessionObserverJob?.cancel()
        connectionCheckJob?.cancel()
        connectionObserverJob?.cancel()
        timeoutCheckJob?.cancel()
    }
}
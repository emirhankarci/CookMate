package com.emirhankarci.seninlemutfakta.presentation.couple

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emirhankarci.seninlemutfakta.data.model.Gender
import com.emirhankarci.seninlemutfakta.data.repository.AuthRepository
import com.emirhankarci.seninlemutfakta.data.repository.CoupleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoupleViewModel @Inject constructor(
    private val coupleRepository: CoupleRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CoupleState())
    val state: StateFlow<CoupleState> = _state.asStateFlow()

    init {
        // Uygulama açıldığında kullanıcının çiftini kontrol et
        loadUserCouple()
    }

    fun onEvent(event: CoupleEvent) {
        when (event) {
            is CoupleEvent.CreateCouple -> createCouple(event.userGender)
            is CoupleEvent.JoinCouple -> joinCouple(event.inviteCode, event.userGender)
            is CoupleEvent.LoadUserCouple -> loadUserCouple()
            is CoupleEvent.ClearError -> clearError()
            is CoupleEvent.ClearSuccess -> clearSuccess()
        }
    }

    private fun createCouple(userGender: Gender) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _state.update { it.copy(error = "Kullanıcı girişi yapılmamış") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) }

            coupleRepository.createCouple(currentUser.uid, userGender)
                .onSuccess { couple ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentCouple = couple,
                            inviteCode = couple.inviteCode,
                            isCreateSuccessful = true,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Çift oluşturulamadı",
                            isCreateSuccessful = false
                        )
                    }
                }
        }
    }

    private fun joinCouple(inviteCode: String, userGender: Gender) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _state.update { it.copy(error = "Kullanıcı girişi yapılmamış") }
                return@launch
            }

            println("🔍 JOIN COUPLE DEBUG: inviteCode=$inviteCode, userId=${currentUser.uid}, gender=$userGender")
            _state.update { it.copy(isLoading = true, error = null) }

            coupleRepository.joinCoupleByInviteCode(inviteCode, currentUser.uid, userGender)
                .onSuccess { couple ->
                    println("✅ JOIN SUCCESS: couple=$couple")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentCouple = couple,
                            inviteCode = couple.inviteCode,
                            isJoinSuccessful = true,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    println("❌ JOIN FAILED: ${exception.message}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Çifte katılamadı",
                            isJoinSuccessful = false
                        )
                    }
                }
        }
    }

    private fun loadUserCouple() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) return@launch

            _state.update { it.copy(isLoading = true) }

            coupleRepository.getUserCouple(currentUser.uid)
                .onSuccess { couple ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentCouple = couple,
                            inviteCode = couple?.inviteCode ?: ""
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                }
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun clearSuccess() {
        _state.update { 
            it.copy(
                isCreateSuccessful = false,
                isJoinSuccessful = false
            )
        }
    }

    // Logout olduğunda couple state'ini temizle
    fun clearCoupleData() {
        _state.update { 
            CoupleState() // Tüm state'i sıfırla
        }
    }
}

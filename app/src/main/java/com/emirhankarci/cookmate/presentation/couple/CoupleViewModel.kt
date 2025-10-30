package com.emirhankarci.cookmate.presentation.couple

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emirhankarci.cookmate.data.repository.AuthRepository
import com.emirhankarci.cookmate.data.repository.CoupleRepository
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
            is CoupleEvent.LoadUserCouple -> loadUserCouple()
            is CoupleEvent.ClearError -> clearError()
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
                            currentCouple = couple
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

    // Logout olduğunda couple state'ini temizle
    fun clearCoupleData() {
        _state.update {
            CoupleState() // Tüm state'i sıfırla
        }
    }
}

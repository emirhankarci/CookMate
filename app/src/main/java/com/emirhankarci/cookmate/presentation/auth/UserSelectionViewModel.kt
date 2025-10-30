package com.emirhankarci.cookmate.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emirhankarci.cookmate.data.model.Couple
import com.emirhankarci.cookmate.data.model.Gender
import com.emirhankarci.cookmate.data.repository.CoupleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserSelectionState(
    val couple: Couple? = null,
    val femaleProfileLocked: Boolean = false,
    val maleProfileLocked: Boolean = false,
    val selectedGender: Gender? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class UserSelectionEvent {
    data class SelectGender(val gender: Gender, val userId: String) : UserSelectionEvent()
    data class ObserveCouple(val coupleId: String) : UserSelectionEvent()
    data class UnlockProfile(val coupleId: String, val gender: Gender) : UserSelectionEvent()
    data class UnlockAllProfiles(val coupleId: String) : UserSelectionEvent()
}

@HiltViewModel
class UserSelectionViewModel @Inject constructor(
    private val coupleRepository: CoupleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserSelectionState())
    val state: StateFlow<UserSelectionState> = _state.asStateFlow()

    fun onEvent(event: UserSelectionEvent) {
        when (event) {
            is UserSelectionEvent.SelectGender -> selectGender(event.gender, event.userId)
            is UserSelectionEvent.ObserveCouple -> observeCouple(event.coupleId)
            is UserSelectionEvent.UnlockProfile -> unlockProfile(event.coupleId, event.gender)
            is UserSelectionEvent.UnlockAllProfiles -> unlockAllProfiles(event.coupleId)
        }
    }

    private fun observeCouple(coupleId: String) {
        viewModelScope.launch {
            coupleRepository.observeCouple(coupleId).collect { couple ->
                _state.update {
                    it.copy(
                        couple = couple,
                        femaleProfileLocked = couple?.femaleProfileLocked ?: false,
                        maleProfileLocked = couple?.maleProfileLocked ?: false
                    )
                }
            }
        }
    }

    private fun selectGender(gender: Gender, userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val coupleId = _state.value.couple?.coupleId ?: return@launch

            // Profili kilitle
            coupleRepository.lockProfile(coupleId, gender, userId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            selectedGender = gender,
                            isLoading = false
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

    private fun unlockProfile(coupleId: String, gender: Gender) {
        viewModelScope.launch {
            coupleRepository.unlockProfile(coupleId, gender)
                .onSuccess {
                    _state.update {
                        it.copy(selectedGender = null)
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(error = exception.message)
                    }
                }
        }
    }

    private fun unlockAllProfiles(coupleId: String) {
        viewModelScope.launch {
            coupleRepository.unlockAllProfiles(coupleId)
                .onSuccess {
                    _state.update {
                        it.copy(selectedGender = null)
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(error = exception.message)
                    }
                }
        }
    }
}

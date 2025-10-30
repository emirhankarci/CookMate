package com.emirhankarci.cookmate.presentation.auth

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
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val coupleRepository: CoupleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        // Uygulama açıldığında mevcut kullanıcıyı kontrol et
        checkCurrentUser()
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.Login -> login(event.email, event.password)
            is AuthEvent.Register -> register(
                event.email,
                event.password,
                event.coupleName
            )
            is AuthEvent.SendPasswordReset -> sendPasswordReset(event.email)
            is AuthEvent.Logout -> logout()
            is AuthEvent.ClearError -> clearError()
            is AuthEvent.ClearSuccess -> clearSuccess()
        }
    }

    private fun checkCurrentUser() {
        val currentUser = authRepository.getCurrentUser()
        _state.update { 
            it.copy(currentUser = currentUser)
        }
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            authRepository.loginWithEmail(email, password)
                .onSuccess { user ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            isLoginSuccessful = true,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = getErrorMessage(exception),
                            isLoginSuccessful = false
                        )
                    }
                }
        }
    }

    private fun register(
        email: String,
        password: String,
        coupleName: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Tek email ile çift hesabı oluştur
            authRepository.registerWithEmail(email, password)
                .onSuccess { user ->
                    // Couple profilini Firebase'e kaydet
                    coupleRepository.createCouple(
                        userId = user.uid,
                        email = email,
                        coupleName = coupleName
                    ).onSuccess {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                currentUser = user,
                                isRegisterSuccessful = true,
                                error = null
                            )
                        }
                    }.onFailure { coupleException ->
                        // Couple oluşturulamadı ama Firebase Auth başarılı
                        // Kullanıcıyı silip hata döndürebiliriz ya da sadece uyarı verebiliriz
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Hesap oluşturuldu ancak profil kaydedilemedi: ${coupleException.message}",
                                isRegisterSuccessful = false
                            )
                        }
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = getErrorMessage(exception),
                            isRegisterSuccessful = false
                        )
                    }
                }
        }
    }

    private fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = getErrorMessage(exception)
                        )
                    }
                }
        }
    }

    private fun logout() {
        authRepository.logout()
        _state.update {
            it.copy(
                currentUser = null,
                isLoginSuccessful = false,
                isRegisterSuccessful = false,
                error = null
            )
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun clearSuccess() {
        _state.update { 
            it.copy(
                isLoginSuccessful = false,
                isRegisterSuccessful = false
            )
        }
    }

    private fun getErrorMessage(exception: Throwable): String {
        return when (exception.message) {
            "The email address is badly formatted." -> "Geçersiz email formatı"
            "The password is invalid or the user does not have a password." -> "Hatalı şifre"
            "There is no user record corresponding to this identifier." -> "Bu email ile kayıtlı kullanıcı bulunamadı"
            "The email address is already in use by another account." -> "Bu email zaten kullanımda"
            "The password must be 6 characters long or more." -> "Şifre en az 6 karakter olmalı"
            else -> exception.message ?: "Bilinmeyen hata"
        }
    }
}

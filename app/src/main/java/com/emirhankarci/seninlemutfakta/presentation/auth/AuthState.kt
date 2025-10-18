package com.emirhankarci.seninlemutfakta.presentation.auth

import com.google.firebase.auth.FirebaseUser

data class AuthState(
    val isLoading: Boolean = false,
    val currentUser: FirebaseUser? = null,
    val error: String? = null,
    val isLoginSuccessful: Boolean = false,
    val isRegisterSuccessful: Boolean = false
) {
    val isLoggedIn: Boolean
        get() = currentUser != null
}
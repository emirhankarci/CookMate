package com.emirhankarci.seninlemutfakta.presentation.auth

sealed class AuthEvent {
    data class Login(
        val email: String,
        val password: String
    ) : AuthEvent()

    data class Register(
        val email: String,
        val password: String,
        val coupleName: String
    ) : AuthEvent()

    data class SendPasswordReset(
        val email: String
    ) : AuthEvent()

    object Logout : AuthEvent()
    object ClearError : AuthEvent()
    object ClearSuccess : AuthEvent()
}

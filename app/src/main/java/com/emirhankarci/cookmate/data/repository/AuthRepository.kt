package com.emirhankarci.cookmate.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    // Mevcut kullanıcıyı al
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    // Kullanıcı giriş yapmış mı?
    fun isUserLoggedIn(): Boolean = getCurrentUser() != null

    // Email/Password ile kayıt ol
    suspend fun registerWithEmail(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Kullanıcı oluşturulamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Email/Password ile giriş yap
    suspend fun loginWithEmail(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Giriş yapılamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Çıkış yap
    fun logout() {
        firebaseAuth.signOut()
    }

    // Şifre sıfırlama emaili gönder
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

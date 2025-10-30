package com.emirhankarci.cookmate.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class AuthRepositoryTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser

    @Before
    fun setup() {
        firebaseAuth = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)

        authRepository = AuthRepository(firebaseAuth)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getCurrentUser returns current user when logged in`() {
        // Given
        every { firebaseAuth.currentUser } returns mockUser

        // When
        val result = authRepository.getCurrentUser()

        // Then
        assertThat(result).isEqualTo(mockUser)
    }

    @Test
    fun `getCurrentUser returns null when not logged in`() {
        // Given
        every { firebaseAuth.currentUser } returns null

        // When
        val result = authRepository.getCurrentUser()

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `isUserLoggedIn returns true when user is logged in`() {
        // Given
        every { firebaseAuth.currentUser } returns mockUser

        // When
        val result = authRepository.isUserLoggedIn()

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `isUserLoggedIn returns false when user is not logged in`() {
        // Given
        every { firebaseAuth.currentUser } returns null

        // When
        val result = authRepository.isUserLoggedIn()

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `logout calls firebase signOut`() {
        // When
        authRepository.logout()

        // Then
        verify { firebaseAuth.signOut() }
    }
}

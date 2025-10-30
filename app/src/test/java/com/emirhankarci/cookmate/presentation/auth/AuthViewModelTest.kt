package com.emirhankarci.cookmate.presentation.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.emirhankarci.cookmate.data.repository.AuthRepository
import com.emirhankarci.cookmate.data.repository.CoupleRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AuthViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var coupleRepository: CoupleRepository
    private lateinit var mockUser: FirebaseUser

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        authRepository = mockk(relaxed = true)
        coupleRepository = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)

        every { authRepository.getCurrentUser() } returns null
        every { mockUser.uid } returns "test-uid"

        viewModel = AuthViewModel(authRepository, coupleRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `initial state is correct`() = runTest {
        // When
        val state = viewModel.state.value

        // Then
        assertThat(state.isLoading).isFalse()
        assertThat(state.currentUser).isNull()
        assertThat(state.error).isNull()
        assertThat(state.isLoginSuccessful).isFalse()
        assertThat(state.isRegisterSuccessful).isFalse()
    }

    @Test
    fun `checkCurrentUser sets user when logged in`() = runTest {
        // Given
        every { authRepository.getCurrentUser() } returns mockUser
        val viewModelWithUser = AuthViewModel(authRepository, coupleRepository)

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModelWithUser.state.value.currentUser).isEqualTo(mockUser)
    }

    @Test
    fun `login success updates state correctly`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "password123"
        coEvery { authRepository.loginWithEmail(email, password) } returns Result.success(mockUser)

        // When
        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.isLoading).isFalse()

            viewModel.onEvent(AuthEvent.Login(email, password))

            // Should see loading state
            val loadingState = awaitItem()
            assertThat(loadingState.isLoading).isTrue()

            testDispatcher.scheduler.advanceUntilIdle()

            // Should see success state
            val successState = awaitItem()
            assertThat(successState.isLoading).isFalse()
            assertThat(successState.currentUser).isEqualTo(mockUser)
            assertThat(successState.isLoginSuccessful).isTrue()
            assertThat(successState.error).isNull()

            cancelAndConsumeRemainingEvents()
        }

        // Verify
        coVerify { authRepository.loginWithEmail(email, password) }
    }

    @Test
    fun `login failure updates state with error`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "wrong"
        val errorMessage = "The password is invalid or the user does not have a password."
        coEvery {
            authRepository.loginWithEmail(email, password)
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(AuthEvent.Login(email, password))
            awaitItem() // loading state

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertThat(errorState.isLoading).isFalse()
            assertThat(errorState.error).isEqualTo("Hatalı şifre")
            assertThat(errorState.isLoginSuccessful).isFalse()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `register success updates state correctly`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "password123"
        val coupleName = "Test Couple"
        val mockCouple = mockk<com.emirhankarci.cookmate.data.model.Couple>(relaxed = true)

        coEvery { authRepository.registerWithEmail(email, password) } returns Result.success(mockUser)
        coEvery {
            coupleRepository.createCouple(mockUser.uid, email, coupleName)
        } returns Result.success(mockCouple)

        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(AuthEvent.Register(email, password, coupleName))
            awaitItem() // loading state

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertThat(successState.isLoading).isFalse()
            assertThat(successState.currentUser).isEqualTo(mockUser)
            assertThat(successState.isRegisterSuccessful).isTrue()
            assertThat(successState.error).isNull()

            cancelAndConsumeRemainingEvents()
        }

        // Verify both repository calls
        coVerify { authRepository.registerWithEmail(email, password) }
        coVerify { coupleRepository.createCouple(mockUser.uid, email, coupleName) }
    }

    @Test
    fun `register fails when auth registration fails`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "short"
        val coupleName = "Test Couple"
        val errorMessage = "The password must be 6 characters long or more."

        coEvery {
            authRepository.registerWithEmail(email, password)
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(AuthEvent.Register(email, password, coupleName))
            awaitItem() // loading state

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertThat(errorState.isLoading).isFalse()
            assertThat(errorState.error).isEqualTo("Şifre en az 6 karakter olmalı")
            assertThat(errorState.isRegisterSuccessful).isFalse()

            cancelAndConsumeRemainingEvents()
        }

        // Verify couple creation was not called
        coVerify(exactly = 0) { coupleRepository.createCouple(any(), any(), any()) }
    }

    @Test
    fun `register fails when couple creation fails`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "password123"
        val coupleName = "Test Couple"

        coEvery { authRepository.registerWithEmail(email, password) } returns Result.success(mockUser)
        coEvery {
            coupleRepository.createCouple(mockUser.uid, email, coupleName)
        } returns Result.failure(Exception("Database error"))

        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(AuthEvent.Register(email, password, coupleName))
            awaitItem() // loading state

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertThat(errorState.isLoading).isFalse()
            assertThat(errorState.error).contains("Hesap oluşturuldu ancak profil kaydedilemedi")
            assertThat(errorState.isRegisterSuccessful).isFalse()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `sendPasswordReset updates state correctly on success`() = runTest {
        // Given
        val email = "test@test.com"
        coEvery { authRepository.sendPasswordResetEmail(email) } returns Result.success(Unit)

        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(AuthEvent.SendPasswordReset(email))
            awaitItem() // loading state

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertThat(successState.isLoading).isFalse()
            assertThat(successState.error).isNull()

            cancelAndConsumeRemainingEvents()
        }

        coVerify { authRepository.sendPasswordResetEmail(email) }
    }

    @Test
    fun `sendPasswordReset updates state with error on failure`() = runTest {
        // Given
        val email = "invalid@test.com"
        val errorMessage = "There is no user record corresponding to this identifier."
        coEvery {
            authRepository.sendPasswordResetEmail(email)
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(AuthEvent.SendPasswordReset(email))
            awaitItem() // loading state

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertThat(errorState.isLoading).isFalse()
            assertThat(errorState.error).isEqualTo("Bu email ile kayıtlı kullanıcı bulunamadı")

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `logout clears user state`() = runTest {
        // Given - First login
        val email = "test@test.com"
        val password = "password123"
        coEvery { authRepository.loginWithEmail(email, password) } returns Result.success(mockUser)

        viewModel.onEvent(AuthEvent.Login(email, password))
        testDispatcher.scheduler.advanceUntilIdle()

        every { authRepository.getCurrentUser() } returns null
        justRun { authRepository.logout() }

        // When
        viewModel.state.test {
            skipItems(1) // Skip current state

            viewModel.onEvent(AuthEvent.Logout)

            val logoutState = awaitItem()
            assertThat(logoutState.currentUser).isNull()
            assertThat(logoutState.isLoginSuccessful).isFalse()
            assertThat(logoutState.isRegisterSuccessful).isFalse()
            assertThat(logoutState.error).isNull()

            cancelAndConsumeRemainingEvents()
        }

        verify { authRepository.logout() }
    }

    @Test
    fun `clearError removes error from state`() = runTest {
        // Given - Create error state
        val email = "test@test.com"
        val password = "wrong"
        coEvery {
            authRepository.loginWithEmail(email, password)
        } returns Result.failure(Exception("Login failed"))

        viewModel.onEvent(AuthEvent.Login(email, password))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.state.test {
            skipItems(1) // Skip current state with error

            viewModel.onEvent(AuthEvent.ClearError)

            val clearedState = awaitItem()
            assertThat(clearedState.error).isNull()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `clearSuccess removes success flags from state`() = runTest {
        // Given - Create success state
        val email = "test@test.com"
        val password = "password123"
        coEvery { authRepository.loginWithEmail(email, password) } returns Result.success(mockUser)

        viewModel.onEvent(AuthEvent.Login(email, password))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.state.test {
            skipItems(1) // Skip current state with success flags

            viewModel.onEvent(AuthEvent.ClearSuccess)

            val clearedState = awaitItem()
            assertThat(clearedState.isLoginSuccessful).isFalse()
            assertThat(clearedState.isRegisterSuccessful).isFalse()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `error messages are translated correctly`() = runTest {
        val testCases = mapOf(
            "The email address is badly formatted." to "Geçersiz email formatı",
            "The password is invalid or the user does not have a password." to "Hatalı şifre",
            "There is no user record corresponding to this identifier." to "Bu email ile kayıtlı kullanıcı bulunamadı",
            "The email address is already in use by another account." to "Bu email zaten kullanımda",
            "The password must be 6 characters long or more." to "Şifre en az 6 karakter olmalı",
            "Unknown error" to "Unknown error"
        )

        testCases.forEach { (firebaseError, expectedError) ->
            // Given
            coEvery {
                authRepository.loginWithEmail(any(), any())
            } returns Result.failure(Exception(firebaseError))

            // When
            viewModel.state.test {
                awaitItem() // initial state

                viewModel.onEvent(AuthEvent.Login("test@test.com", "password"))
                awaitItem() // loading state

                testDispatcher.scheduler.advanceUntilIdle()

                val errorState = awaitItem()
                assertThat(errorState.error).isEqualTo(expectedError)

                cancelAndConsumeRemainingEvents()
            }
        }
    }
}

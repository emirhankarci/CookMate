package com.emirhankarci.cookmate.presentation.cooking

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.emirhankarci.cookmate.data.model.*
import com.emirhankarci.cookmate.data.repository.CookingSessionRepository
import com.emirhankarci.cookmate.data.repository.FirebaseRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class CookingSessionViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CookingSessionViewModel
    private lateinit var cookingSessionRepository: CookingSessionRepository
    private lateinit var firebaseRepository: FirebaseRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        cookingSessionRepository = mockk(relaxed = true)
        firebaseRepository = mockk(relaxed = true)

        // Setup default flow emissions
        coEvery { cookingSessionRepository.observeSession(any()) } returns flowOf(null)
        coEvery { cookingSessionRepository.observeWaitingSessionForCouple(any()) } returns flowOf(null)
        coEvery { cookingSessionRepository.observeConnectionStatus() } returns flowOf(true)

        viewModel = CookingSessionViewModel(cookingSessionRepository, firebaseRepository)
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
        assertThat(state.session).isNull()
        assertThat(state.recipe).isNull()
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
        assertThat(state.isConnected).isTrue()
    }

    @Test
    fun `clearError removes error from state`() = runTest {
        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(CookingSessionEvent.ClearError)

            val clearedState = awaitItem()
            assertThat(clearedState.error).isNull()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `dismissCoopDialog updates dialog state`() = runTest {
        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(CookingSessionEvent.DismissCoopDialog)

            val updatedState = awaitItem()
            assertThat(updatedState.showCoopModeDialog).isFalse()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `dismissWaitingDialog updates dialog state`() = runTest {
        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(CookingSessionEvent.DismissWaitingDialog)

            val updatedState = awaitItem()
            assertThat(updatedState.showWaitingForPartnerDialog).isFalse()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `dismissCompletionDialog updates dialog state`() = runTest {
        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(CookingSessionEvent.DismissCompletionDialog)

            val updatedState = awaitItem()
            assertThat(updatedState.showCompletionDialog).isFalse()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `showCoopModeDialog updates dialog state`() = runTest {
        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(CookingSessionEvent.ShowCoopModeDialog)

            val updatedState = awaitItem()
            assertThat(updatedState.showCoopModeDialog).isTrue()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `startSession loads recipe and creates session`() = runTest {
        // Given
        val recipeId = "recipe123"
        val countryCode = "france"
        val coupleId = "couple123"
        val femaleUserId = "user_female"
        val maleUserId = "user_male"
        val currentUserGender = Gender.FEMALE
        val sessionId = "session123"

        val mockRecipe = Recipe(
            recipeId = recipeId,
            countryCode = countryCode,
            title = "Test Recipe",
            titleTurkish = "Test Tarifi",
            description = "Test description",
            difficulty = 3,
            estimatedTime = 45,
            servings = 4,
            thumbnailUrl = "",
            videoUrl = "",
            order = 1,
            isLocked = false,
            ingredients = emptyList(),
            steps = listOf(
                RecipeStep(
                    stepNumber = 1,
                    assignedTo = "",
                    description = "Step 1",
                    animationUrl = "",
                    imageUrl = "",
                    estimatedTime = 10,
                    tips = "",
                    syncWith = 0
                )
            ),
            femaleSteps = listOf(
                RecipeStep(
                    stepNumber = 1,
                    assignedTo = "FEMALE",
                    description = "Female Step 1",
                    animationUrl = "",
                    imageUrl = "",
                    estimatedTime = 10,
                    tips = "",
                    syncWith = 0
                )
            ),
            maleSteps = listOf(
                RecipeStep(
                    stepNumber = 1,
                    assignedTo = "MALE",
                    description = "Male Step 1",
                    animationUrl = "",
                    imageUrl = "",
                    estimatedTime = 10,
                    tips = "",
                    syncWith = 0
                )
            )
        )

        val mockSession = CookingSession(
            sessionId = sessionId,
            recipeId = recipeId,
            countryCode = countryCode,
            accountId = coupleId,
            isCoopMode = true,
            femaleUserId = femaleUserId,
            maleUserId = maleUserId,
            totalSteps = 1,
            currentStep = 0,
            status = SessionStatus.IN_PROGRESS,
            startedAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis(),
            femaleProgress = StepProgress(),
            maleProgress = StepProgress()
        )

        coEvery { firebaseRepository.getRecipe(countryCode, recipeId) } returns Result.success(mockRecipe)
        coEvery {
            cookingSessionRepository.createSession(
                recipeId = recipeId,
                countryCode = countryCode,
                accountId = coupleId,
                isCoopMode = true,
                femaleUserId = femaleUserId,
                maleUserId = maleUserId,
                totalSteps = any()
            )
        } returns Result.success(sessionId)
        coEvery { cookingSessionRepository.observeSession(sessionId) } returns flowOf(mockSession)
        coEvery { cookingSessionRepository.updateOnlineStatus(any(), any(), any()) } returns Result.success(Unit)

        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(
                CookingSessionEvent.StartSession(
                    recipeId = recipeId,
                    countryCode = countryCode,
                    isCoopMode = true,
                    coupleId = coupleId,
                    femaleUserId = femaleUserId,
                    maleUserId = maleUserId,
                    currentUserGender = currentUserGender
                )
            )

            // Loading state
            val loadingState = awaitItem()
            assertThat(loadingState.isLoading).isTrue()

            testDispatcher.scheduler.advanceUntilIdle()

            // Success state
            val successState = awaitItem()
            assertThat(successState.isLoading).isFalse()
            assertThat(successState.recipe).isEqualTo(mockRecipe)
            assertThat(successState.currentUserGender).isEqualTo(currentUserGender)

            cancelAndConsumeRemainingEvents()
        }

        coVerify { firebaseRepository.getRecipe(countryCode, recipeId) }
        coVerify { cookingSessionRepository.createSession(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `startSession shows error when recipe not found`() = runTest {
        // Given
        val recipeId = "invalid_recipe"
        val countryCode = "france"

        coEvery { firebaseRepository.getRecipe(countryCode, recipeId) } returns Result.success(null)

        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(
                CookingSessionEvent.StartSession(
                    recipeId = recipeId,
                    countryCode = countryCode,
                    isCoopMode = false,
                    coupleId = "couple123",
                    femaleUserId = "user_female",
                    maleUserId = "user_male",
                    currentUserGender = Gender.FEMALE
                )
            )

            awaitItem() // loading state

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertThat(errorState.isLoading).isFalse()
            assertThat(errorState.error).isEqualTo("Tarif bulunamadı")

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `completeCurrentStep calls repository with correct parameters`() = runTest {
        // Given
        val sessionId = "session123"
        val currentGender = Gender.FEMALE
        val stepIndex = 0

        val mockSession = CookingSession(
            sessionId = sessionId,
            recipeId = "recipe123",
            countryCode = "france",
            accountId = "couple123",
            isCoopMode = true,
            femaleUserId = "user_female",
            maleUserId = "user_male",
            totalSteps = 3,
            currentStep = stepIndex,
            status = SessionStatus.IN_PROGRESS,
            startedAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis(),
            femaleProgress = StepProgress(),
            maleProgress = StepProgress()
        )

        val mockRecipe = Recipe(
            recipeId = "recipe123",
            countryCode = "france",
            title = "Test",
            titleTurkish = "Test",
            description = "Test",
            difficulty = 2,
            estimatedTime = 30,
            servings = 4,
            thumbnailUrl = "",
            videoUrl = "",
            order = 1,
            isLocked = false,
            ingredients = emptyList(),
            steps = listOf(
                RecipeStep(
                    stepNumber = 1,
                    assignedTo = "",
                    description = "Step",
                    animationUrl = "",
                    imageUrl = "",
                    estimatedTime = 10,
                    tips = "",
                    syncWith = 0
                )
            ),
            femaleSteps = emptyList(),
            maleSteps = emptyList()
        )

        // Setup state with session
        coEvery { firebaseRepository.getRecipe(any(), any()) } returns Result.success(mockRecipe)
        coEvery { cookingSessionRepository.createSession(any(), any(), any(), any(), any(), any(), any()) } returns Result.success(sessionId)
        coEvery { cookingSessionRepository.observeSession(sessionId) } returns flowOf(mockSession)
        coEvery { cookingSessionRepository.updateOnlineStatus(any(), any(), any()) } returns Result.success(Unit)
        coEvery { cookingSessionRepository.completeStep(sessionId, currentGender, stepIndex) } returns Result.success(Unit)

        viewModel.onEvent(
            CookingSessionEvent.StartSession(
                recipeId = "recipe123",
                countryCode = "france",
                isCoopMode = true,
                coupleId = "couple123",
                femaleUserId = "user_female",
                maleUserId = "user_male",
                currentUserGender = currentGender
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onEvent(CookingSessionEvent.CompleteCurrentStep)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { cookingSessionRepository.completeStep(sessionId, currentGender, stepIndex) }
    }

    @Test
    fun `resetSessionState clears session data`() = runTest {
        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(CookingSessionEvent.ResetSessionState)

            val resetState = awaitItem()
            assertThat(resetState.session).isNull()
            assertThat(resetState.recipe).isNull()
            assertThat(resetState.showCompletionDialog).isFalse()
            assertThat(resetState.showWaitingForPartnerDialog).isFalse()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `cleanUpOldSessions calls repository with correct account id`() = runTest {
        // Given
        val accountId = "couple123"
        coJustRun { cookingSessionRepository.cleanUpOldSessionsForCouple(accountId) }

        // When
        viewModel.onEvent(CookingSessionEvent.CleanUpOldSessions(accountId))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { cookingSessionRepository.cleanUpOldSessionsForCouple(accountId) }
    }

    @Test
    fun `pauseSession calls repository pause`() = runTest {
        // Given
        val sessionId = "session123"
        val mockSession = CookingSession(
            sessionId = sessionId,
            recipeId = "recipe123",
            countryCode = "france",
            accountId = "couple123",
            isCoopMode = false,
            femaleUserId = "user_female",
            maleUserId = "user_male",
            totalSteps = 1,
            currentStep = 0,
            status = SessionStatus.IN_PROGRESS,
            startedAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis(),
            femaleProgress = StepProgress(),
            maleProgress = StepProgress()
        )

        val mockRecipe = Recipe(
            recipeId = "recipe123",
            countryCode = "france",
            title = "Test",
            titleTurkish = "Test",
            description = "Test",
            difficulty = 2,
            estimatedTime = 30,
            servings = 4,
            thumbnailUrl = "",
            videoUrl = "",
            order = 1,
            isLocked = false,
            ingredients = emptyList(),
            steps = listOf(
                RecipeStep(
                    stepNumber = 1,
                    assignedTo = "",
                    description = "Step",
                    animationUrl = "",
                    imageUrl = "",
                    estimatedTime = 10,
                    tips = "",
                    syncWith = 0
                )
            ),
            femaleSteps = emptyList(),
            maleSteps = emptyList()
        )

        coEvery { firebaseRepository.getRecipe(any(), any()) } returns Result.success(mockRecipe)
        coEvery { cookingSessionRepository.createSession(any(), any(), any(), any(), any(), any(), any()) } returns Result.success(sessionId)
        coEvery { cookingSessionRepository.observeSession(sessionId) } returns flowOf(mockSession)
        coEvery { cookingSessionRepository.updateOnlineStatus(any(), any(), any()) } returns Result.success(Unit)
        coEvery { cookingSessionRepository.pauseSession(sessionId) } returns Result.success(Unit)

        viewModel.onEvent(
            CookingSessionEvent.StartSession(
                recipeId = "recipe123",
                countryCode = "france",
                isCoopMode = false,
                coupleId = "couple123",
                femaleUserId = "user_female",
                maleUserId = "user_male",
                currentUserGender = Gender.FEMALE
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onEvent(CookingSessionEvent.PauseSession)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { cookingSessionRepository.pauseSession(sessionId) }
    }

    @Test
    fun `dismissPartnerLeftDialog updates dialog state`() = runTest {
        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(CookingSessionEvent.DismissPartnerLeftDialog)

            val updatedState = awaitItem()
            assertThat(updatedState.showPartnerLeftDialog).isFalse()

            cancelAndConsumeRemainingEvents()
        }
    }
}

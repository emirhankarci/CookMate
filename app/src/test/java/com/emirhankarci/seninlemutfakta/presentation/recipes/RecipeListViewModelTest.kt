package com.emirhankarci.seninlemutfakta.presentation.recipes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.emirhankarci.seninlemutfakta.data.model.Country
import com.emirhankarci.seninlemutfakta.data.model.Recipe
import com.emirhankarci.seninlemutfakta.data.repository.FirebaseRepository
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
class RecipeListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: RecipeListViewModel
    private lateinit var firebaseRepository: FirebaseRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        firebaseRepository = mockk(relaxed = true)
        viewModel = RecipeListViewModel(firebaseRepository)
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
        assertThat(state.recipes).isEmpty()
        assertThat(state.countryCode).isEmpty()
        assertThat(state.countryName).isEmpty()
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `loadRecipes success updates state with country and recipes`() = runTest {
        // Given
        val countryCode = "france"
        val mockCountry = Country(
            countryCode = countryCode,
            name = "France",
            flagEmoji = "🇫🇷",
            flagUrl = "",
            passportStampUrl = "",
            isLocked = false,
            price = 0.0,
            order = 1,
            totalRecipes = 2,
            description = "French cuisine"
        )
        val mockRecipes = listOf(
            Recipe(
                recipeId = "recipe1",
                countryCode = countryCode,
                title = "Ratatouille",
                titleTurkish = "Ratatuy",
                description = "French dish",
                difficulty = 3,
                estimatedTime = 45,
                servings = 4,
                thumbnailUrl = "",
                videoUrl = "",
                order = 1,
                isLocked = false,
                ingredients = emptyList(),
                steps = emptyList(),
                femaleSteps = emptyList(),
                maleSteps = emptyList()
            ),
            Recipe(
                recipeId = "recipe2",
                countryCode = countryCode,
                title = "Croissant",
                titleTurkish = "Kruvasan",
                description = "French pastry",
                difficulty = 4,
                estimatedTime = 120,
                servings = 8,
                thumbnailUrl = "",
                videoUrl = "",
                order = 2,
                isLocked = false,
                ingredients = emptyList(),
                steps = emptyList(),
                femaleSteps = emptyList(),
                maleSteps = emptyList()
            )
        )

        coEvery { firebaseRepository.getCountry(countryCode) } returns Result.success(mockCountry)
        coEvery { firebaseRepository.getRecipesByCountry(countryCode) } returns Result.success(mockRecipes)

        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(RecipeListEvent.LoadRecipes(countryCode))

            // Loading state
            val loadingState = awaitItem()
            assertThat(loadingState.isLoading).isTrue()
            assertThat(loadingState.countryCode).isEqualTo(countryCode)

            testDispatcher.scheduler.advanceUntilIdle()

            // Country loaded state
            val countryLoadedState = awaitItem()
            assertThat(countryLoadedState.countryName).isEqualTo("France")
            assertThat(countryLoadedState.countryFlagEmoji).isEqualTo("🇫🇷")

            // Success state with recipes
            val successState = awaitItem()
            assertThat(successState.isLoading).isFalse()
            assertThat(successState.recipes).hasSize(2)
            assertThat(successState.recipes[0].title).isEqualTo("Ratatouille")
            assertThat(successState.error).isNull()

            cancelAndConsumeRemainingEvents()
        }

        coVerify { firebaseRepository.getCountry(countryCode) }
        coVerify { firebaseRepository.getRecipesByCountry(countryCode) }
    }

    @Test
    fun `loadRecipes failure updates state with error`() = runTest {
        // Given
        val countryCode = "france"
        val errorMessage = "Network error"

        coEvery { firebaseRepository.getCountry(countryCode) } returns Result.success(null)
        coEvery {
            firebaseRepository.getRecipesByCountry(countryCode)
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(RecipeListEvent.LoadRecipes(countryCode))
            awaitItem() // loading state

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertThat(errorState.isLoading).isFalse()
            assertThat(errorState.error).isEqualTo(errorMessage)
            assertThat(errorState.recipes).isEmpty()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `changeSortType updates sort type in state`() = runTest {
        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEvent(RecipeListEvent.ChangeSortType(RecipeSortType.DIFFICULTY_ASC))

            val updatedState = awaitItem()
            assertThat(updatedState.sortBy).isEqualTo(RecipeSortType.DIFFICULTY_ASC)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `loadCompletedRecipes updates completed recipes list`() = runTest {
        // Given
        val completedRecipes = listOf("recipe1", "recipe2", "recipe3")

        // When
        viewModel.state.test {
            awaitItem() // initial state

            viewModel.loadCompletedRecipes(completedRecipes)

            val updatedState = awaitItem()
            assertThat(updatedState.completedRecipes).hasSize(3)
            assertThat(updatedState.completedRecipes).contains("recipe1")
            assertThat(updatedState.completedRecipes).contains("recipe2")

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `retry event reloads recipes with same country code`() = runTest {
        // Given
        val countryCode = "italy"
        val mockCountry = Country(
            countryCode = countryCode,
            name = "Italy",
            flagEmoji = "🇮🇹",
            flagUrl = "",
            passportStampUrl = "",
            isLocked = false,
            price = 0.0,
            order = 2,
            totalRecipes = 1,
            description = "Italian cuisine"
        )
        val mockRecipes = listOf(
            Recipe(
                recipeId = "pasta1",
                countryCode = countryCode,
                title = "Pasta Carbonara",
                titleTurkish = "Karbonaralı Makarna",
                description = "Classic Italian pasta",
                difficulty = 2,
                estimatedTime = 30,
                servings = 2,
                thumbnailUrl = "",
                videoUrl = "",
                order = 1,
                isLocked = false,
                ingredients = emptyList(),
                steps = emptyList(),
                femaleSteps = emptyList(),
                maleSteps = emptyList()
            )
        )

        // First load fails
        coEvery { firebaseRepository.getCountry(countryCode) } returns Result.success(mockCountry)
        coEvery {
            firebaseRepository.getRecipesByCountry(countryCode)
        } returns Result.failure(Exception("Network error"))

        viewModel.onEvent(RecipeListEvent.LoadRecipes(countryCode))
        testDispatcher.scheduler.advanceUntilIdle()

        // Retry succeeds
        coEvery { firebaseRepository.getRecipesByCountry(countryCode) } returns Result.success(mockRecipes)

        // When
        viewModel.state.test {
            skipItems(1) // Skip current error state

            viewModel.onEvent(RecipeListEvent.Retry)
            awaitItem() // loading state

            testDispatcher.scheduler.advanceUntilIdle()

            // Skip country loaded (might not emit if already loaded)
            skipItems(1)

            val successState = awaitItem()
            assertThat(successState.isLoading).isFalse()
            assertThat(successState.recipes).hasSize(1)
            assertThat(successState.error).isNull()

            cancelAndConsumeRemainingEvents()
        }

        // Verify repository was called twice (initial + retry)
        coVerify(exactly = 2) { firebaseRepository.getRecipesByCountry(countryCode) }
    }

    @Test
    fun `selectRecipe logs recipe selection`() = runTest {
        // Given
        val recipeId = "recipe123"

        // When - This just logs, no state change
        viewModel.onEvent(RecipeListEvent.SelectRecipe(recipeId))

        // Then - No state change expected, just verify no crash
        assertThat(viewModel.state.value).isNotNull()
    }

    @Test
    fun `state helpers work correctly`() = runTest {
        // Given
        val mockRecipes = listOf(
            Recipe(
                recipeId = "recipe1",
                countryCode = "test",
                title = "Recipe 1",
                titleTurkish = "Tarif 1",
                description = "Test recipe 1",
                difficulty = 2,
                estimatedTime = 30,
                servings = 4,
                thumbnailUrl = "",
                videoUrl = "",
                order = 1,
                isLocked = false,
                ingredients = emptyList(),
                steps = emptyList(),
                femaleSteps = emptyList(),
                maleSteps = emptyList()
            ),
            Recipe(
                recipeId = "recipe2",
                countryCode = "test",
                title = "Recipe 2",
                titleTurkish = "Tarif 2",
                description = "Test recipe 2",
                difficulty = 4,
                estimatedTime = 60,
                servings = 2,
                thumbnailUrl = "",
                videoUrl = "",
                order = 2,
                isLocked = false,
                ingredients = emptyList(),
                steps = emptyList(),
                femaleSteps = emptyList(),
                maleSteps = emptyList()
            )
        )
        val completedRecipes = listOf("recipe1")

        coEvery { firebaseRepository.getCountry(any()) } returns Result.success(null)
        coEvery { firebaseRepository.getRecipesByCountry(any()) } returns Result.success(mockRecipes)

        viewModel.onEvent(RecipeListEvent.LoadRecipes("test"))
        viewModel.loadCompletedRecipes(completedRecipes)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val state = viewModel.state.value

        // Then
        assertThat(state.isRecipeCompleted("recipe1")).isTrue()
        assertThat(state.isRecipeCompleted("recipe2")).isFalse()
        assertThat(state.getCompletedCount()).isEqualTo(1)
        assertThat(state.getProgressPercentage()).isEqualTo(50)
        assertThat(state.getBadgeCount()).isEqualTo(0) // 1 recipe = 0 badges (need 2)
    }

    @Test
    fun `getSortedRecipes sorts correctly by difficulty`() = runTest {
        // Given
        val mockRecipes = listOf(
            Recipe(
                recipeId = "recipe1",
                countryCode = "test",
                title = "Hard Recipe",
                titleTurkish = "Zor Tarif",
                description = "Hard recipe",
                difficulty = 4,
                estimatedTime = 30,
                servings = 4,
                thumbnailUrl = "",
                videoUrl = "",
                order = 1,
                isLocked = false,
                ingredients = emptyList(),
                steps = emptyList(),
                femaleSteps = emptyList(),
                maleSteps = emptyList()
            ),
            Recipe(
                recipeId = "recipe2",
                countryCode = "test",
                title = "Easy Recipe",
                titleTurkish = "Kolay Tarif",
                description = "Easy recipe",
                difficulty = 2,
                estimatedTime = 60,
                servings = 2,
                thumbnailUrl = "",
                videoUrl = "",
                order = 2,
                isLocked = false,
                ingredients = emptyList(),
                steps = emptyList(),
                femaleSteps = emptyList(),
                maleSteps = emptyList()
            )
        )

        coEvery { firebaseRepository.getCountry(any()) } returns Result.success(null)
        coEvery { firebaseRepository.getRecipesByCountry(any()) } returns Result.success(mockRecipes)

        viewModel.onEvent(RecipeListEvent.LoadRecipes("test"))
        viewModel.onEvent(RecipeListEvent.ChangeSortType(RecipeSortType.DIFFICULTY_ASC))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val state = viewModel.state.value
        val sortedRecipes = state.getSortedRecipes()

        // Then
        assertThat(sortedRecipes.first().title).isEqualTo("Easy Recipe")
        assertThat(sortedRecipes.last().title).isEqualTo("Hard Recipe")
    }
}

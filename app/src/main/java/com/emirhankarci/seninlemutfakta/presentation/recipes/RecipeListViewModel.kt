package com.emirhankarci.seninlemutfakta.presentation.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emirhankarci.seninlemutfakta.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeListState())
    val state = _state.asStateFlow()

    fun onEvent(event: RecipeListEvent) {
        when (event) {
            is RecipeListEvent.LoadRecipes -> loadRecipes(event.countryCode)
            is RecipeListEvent.SelectRecipe -> selectRecipe(event.recipeId)
            is RecipeListEvent.ChangeSortType -> changeSortType(event.sortType)
            is RecipeListEvent.Retry -> retryLoad()
        }
    }

    private fun loadRecipes(countryCode: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    countryCode = countryCode
                )
            }

            // Ülke bilgisini al
            firebaseRepository.getCountry(countryCode)
                .onSuccess { country ->
                    _state.update {
                        it.copy(
                            countryName = country?.name ?: "",
                            countryFlagEmoji = country?.flagEmoji ?: ""
                        )
                    }
                }

            // Tarifleri al
            firebaseRepository.getRecipesByCountry(countryCode)
                .onSuccess { recipes ->
                    _state.update {
                        it.copy(
                            recipes = recipes,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Tarifler yüklenirken hata oluştu"
                        )
                    }
                }
        }
    }

    private fun changeSortType(sortType: RecipeSortType) {
        _state.update { it.copy(sortBy = sortType) }
    }

    private fun selectRecipe(recipeId: String) {
        // TODO: Navigation - Tarif detay ekranına git
        println("Tarif seçildi: $recipeId")
    }

    private fun retryLoad() {
        val countryCode = _state.value.countryCode
        if (countryCode.isNotEmpty()) {
            loadRecipes(countryCode)
        }
    }

    // Kullanıcının tamamladığı tarifleri yükle (CoupleAccount'tan gelecek)
    fun loadCompletedRecipes(completedRecipes: List<String>) {
        _state.update { it.copy(completedRecipes = completedRecipes) }
    }
}
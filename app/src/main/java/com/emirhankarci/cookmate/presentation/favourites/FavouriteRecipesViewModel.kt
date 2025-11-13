package com.emirhankarci.cookmate.presentation.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emirhankarci.cookmate.data.repository.AuthRepository
import com.emirhankarci.cookmate.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouriteRecipesViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavouriteRecipesState())
    val state = _state.asStateFlow()

    fun onEvent(event: FavouriteRecipesEvent) {
        when (event) {
            is FavouriteRecipesEvent.LoadFavourites -> loadFavouriteRecipes()
            is FavouriteRecipesEvent.AddFavourite -> addFavourite(event.recipeId)
            is FavouriteRecipesEvent.RemoveFavourite -> removeFavourite(event.recipeId)
        }
    }

    fun loadFavouriteRecipes() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.uid ?: return@launch
            _state.update { it.copy(isLoading = true) }

            firebaseRepository.getFavouriteRecipes(userId)
                .onSuccess { recipes ->
                    _state.update {
                        it.copy(
                            favouriteRecipes = recipes,
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

    fun addFavourite(recipeId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.uid ?: return@launch
            
            firebaseRepository.addFavouriteRecipe(userId, recipeId)
                .onSuccess {
                    loadFavouriteRecipes()
                }
        }
    }

    private fun removeFavourite(recipeId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.uid ?: return@launch
            
            firebaseRepository.removeFavouriteRecipe(userId, recipeId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            favouriteRecipes = it.favouriteRecipes.filter { recipe ->
                                recipe.recipeId != recipeId
                            }
                        )
                    }
                }
        }
    }
}

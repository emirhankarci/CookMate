package com.emirhankarci.seninlemutfakta.presentation.recipes

import com.emirhankarci.seninlemutfakta.data.model.Recipe

data class RecipeListState(
    val recipes: List<Recipe> = emptyList(),
    val countryCode: String = "",
    val countryName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val completedRecipes: List<String> = emptyList(), // Kullanıcının tamamladığı tarifler
    val sortBy: RecipeSortType = RecipeSortType.ORDER
) {
    // Helper: Tarif tamamlandı mı?
    fun isRecipeCompleted(recipeId: String): Boolean {
        return completedRecipes.contains(recipeId)
    }

    // Helper: Kaç tarif tamamlandı?
    fun getCompletedCount(): Int = completedRecipes.count { recipeId ->
        recipes.any { it.recipeId == recipeId }
    }

    // Helper: Progress yüzdesi
    fun getProgressPercentage(): Int {
        return if (recipes.isEmpty()) 0
        else (getCompletedCount().toFloat() / recipes.size * 100).toInt()
    }

    // Helper: Rozet kazanıldı mı? (her 2 tarif = 1 rozet)
    fun getBadgeCount(): Int = getCompletedCount() / 2

    // Helper: Sıralanmış tarifler
    fun getSortedRecipes(): List<Recipe> {
        return when (sortBy) {
            RecipeSortType.ORDER -> recipes.sortedBy { it.order }
            RecipeSortType.DIFFICULTY_ASC -> recipes.sortedBy { it.difficulty }
            RecipeSortType.DIFFICULTY_DESC -> recipes.sortedByDescending { it.difficulty }
            RecipeSortType.TIME_ASC -> recipes.sortedBy { it.estimatedTime }
            RecipeSortType.TIME_DESC -> recipes.sortedByDescending { it.estimatedTime }
        }
    }
}

enum class RecipeSortType {
    ORDER,              // Varsayılan sıra
    DIFFICULTY_ASC,     // Kolaydan zora
    DIFFICULTY_DESC,    // Zordan kolaya
    TIME_ASC,           // Kısa süre önce
    TIME_DESC           // Uzun süre önce
}
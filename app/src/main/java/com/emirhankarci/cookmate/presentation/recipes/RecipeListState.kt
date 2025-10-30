package com.emirhankarci.cookmate.presentation.recipes

import com.emirhankarci.cookmate.data.model.Recipe

data class RecipeListState(
    val recipes: List<Recipe> = emptyList(),
    val countryCode: String = "",
    val countryName: String = "",
    val countryFlagEmoji: String = "",
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

    // Helper: Tarif durumu
    fun getRecipeStatus(recipeId: String): RecipeStatus {
        return when {
            isRecipeCompleted(recipeId) -> RecipeStatus.COMPLETED
            // TODO: In-progress kontrolü - cooking session'dan gelmeli
            else -> RecipeStatus.NOT_STARTED
        }
    }

    // Helper: Tarif kilitli mi? (şimdilik tümü açık, ileride unlock mekanizması eklenebilir)
    fun isRecipeLocked(recipe: Recipe): Boolean {
        return recipe.isLocked
    }

    // Helper: Filtrelenmiş tarifler
    fun getFilteredRecipes(filter: RecipeFilter): List<Recipe> {
        val sorted = getSortedRecipes()
        return when (filter) {
            RecipeFilter.ALL -> sorted
            RecipeFilter.NOT_STARTED -> sorted.filter { getRecipeStatus(it.recipeId) == RecipeStatus.NOT_STARTED && !isRecipeLocked(it) }
            RecipeFilter.IN_PROGRESS -> sorted.filter { getRecipeStatus(it.recipeId) == RecipeStatus.IN_PROGRESS }
            RecipeFilter.COMPLETED -> sorted.filter { getRecipeStatus(it.recipeId) == RecipeStatus.COMPLETED }
        }
    }

    // Helper: Zorluk seviyesi text
    fun getDifficultyText(difficulty: Int): String {
        return when (difficulty) {
            1 -> "Very Easy"
            2 -> "Easy"
            3 -> "Medium"
            4 -> "Hard"
            5 -> "Very Hard"
            else -> "Medium"
        }
    }

    // Helper: Zorluk rengi
    fun getDifficultyColor(difficulty: Int): String {
        return when (difficulty) {
            1, 2 -> "#4CAF50" // Green
            3 -> "#FFA726" // Orange
            4, 5 -> "#FF6B6B" // Red
            else -> "#FFA726"
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

enum class RecipeStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}

enum class RecipeFilter {
    ALL,
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}

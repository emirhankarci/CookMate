package com.emirhankarci.cookmate.data.model

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val gender: Gender = Gender.FEMALE,
    val avatarUrl: String = "",
    val completedRecipes: List<String> = emptyList(),
    val badges: List<Badge> = emptyList(),
    val unlockedCountries: List<String> = listOf("france", "italy"),
    val passportStamps: List<String> = emptyList()
) {
    constructor() : this(
        "", "", Gender.FEMALE, "", emptyList(), emptyList(),
        listOf("france", "italy"), emptyList()
    )

    // Helper functions
    fun hasCompletedRecipe(recipeId: String): Boolean {
        return completedRecipes.contains(recipeId)
    }

    fun hasUnlockedCountry(countryCode: String): Boolean {
        return unlockedCountries.contains(countryCode)
    }

    fun getBadgeCount(): Int = badges.size
}

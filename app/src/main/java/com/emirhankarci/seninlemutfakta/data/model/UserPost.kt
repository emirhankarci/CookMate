package com.emirhankarci.seninlemutfakta.data.model

data class Comment(
    val commentId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", System.currentTimeMillis())
}

data class UserPost(
    val postId: String = "",
    val coupleId: String = "",
    val recipeId: String = "",
    val recipeName: String = "",
    val countryCode: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val comments: List<Comment> = emptyList()
) {
    constructor() : this(
        "", "", "", "", "", "", "",
        System.currentTimeMillis(), 0, emptyList()
    )

    // Helper functions
    fun getCommentCount(): Int = comments.size

    fun hasLiked(userId: String): Boolean {
        // Bu özellik için ayrı bir "likes" collection'ı gerekebilir
        return false
    }
}

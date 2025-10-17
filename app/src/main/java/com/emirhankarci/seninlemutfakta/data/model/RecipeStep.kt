package com.emirhankarci.seninlemutfakta.data.model

data class RecipeStep(
    val stepNumber: Int = 0,
    val assignedTo: String = "",  // "FEMALE", "MALE", ya da "" (ikisi için)
    val description: String = "",
    val animationUrl: String = "",
    val imageUrl: String = "",
    val estimatedTime: Int = 0,  // dakika
    val tips: String = "",
    val syncWith: Int = 0  // Eş zamanlı modda hangi adımla senkron
) {
    constructor() : this(0, "", "", "", "", 0, "", 0)
}
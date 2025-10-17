package com.emirhankarci.seninlemutfakta.data.model

data class Badge(
    val badgeId: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val countryCode: String = "",
    val requirement: String = "",
    val earnedAt: Long = 0
) {
    constructor() : this("", "", "", "", "", "", 0)
}
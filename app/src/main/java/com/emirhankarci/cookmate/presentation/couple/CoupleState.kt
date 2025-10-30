package com.emirhankarci.cookmate.presentation.couple

import com.emirhankarci.cookmate.data.model.Couple

data class CoupleState(
    val isLoading: Boolean = false,
    val currentCouple: Couple? = null,
    val error: String? = null
) {
    val hasCouple: Boolean
        get() = currentCouple != null
}

package com.emirhankarci.seninlemutfakta.presentation.couple

import com.emirhankarci.seninlemutfakta.data.model.Couple

data class CoupleState(
    val isLoading: Boolean = false,
    val currentCouple: Couple? = null,
    val error: String? = null,
    val isCreateSuccessful: Boolean = false,
    val isJoinSuccessful: Boolean = false,
    val inviteCode: String = ""
) {
    val hasCouple: Boolean
        get() = currentCouple != null

    val isWaitingForPartner: Boolean
        get() = currentCouple?.needsPartner() == true
}

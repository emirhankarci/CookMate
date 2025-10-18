package com.emirhankarci.seninlemutfakta.presentation.couple

import com.emirhankarci.seninlemutfakta.data.model.Gender

sealed class CoupleEvent {
    data class CreateCouple(val userGender: Gender) : CoupleEvent()
    data class JoinCouple(val inviteCode: String, val userGender: Gender) : CoupleEvent()
    object LoadUserCouple : CoupleEvent()
    object ClearError : CoupleEvent()
    object ClearSuccess : CoupleEvent()
}

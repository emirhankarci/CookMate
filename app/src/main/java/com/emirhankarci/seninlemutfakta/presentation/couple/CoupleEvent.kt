package com.emirhankarci.seninlemutfakta.presentation.couple

sealed class CoupleEvent {
    object LoadUserCouple : CoupleEvent()
    object ClearError : CoupleEvent()
}

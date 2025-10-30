package com.emirhankarci.cookmate.presentation.couple

sealed class CoupleEvent {
    object LoadUserCouple : CoupleEvent()
    object ClearError : CoupleEvent()
}

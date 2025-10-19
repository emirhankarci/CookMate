package com.emirhankarci.seninlemutfakta.presentation.cooking

import com.emirhankarci.seninlemutfakta.data.model.Gender

sealed class CookingSessionEvent {
    // Session başlatma
    data class StartSession(
        val recipeId: String,
        val countryCode: String,
        val isCoopMode: Boolean,
        val coupleId: String,
        val femaleUserId: String,
        val maleUserId: String,
        val currentUserGender: Gender
    ) : CookingSessionEvent()

    // Atomic session creation/join
    data class CreateOrJoinSession(
        val recipeId: String,
        val countryCode: String,
        val isCoopMode: Boolean,
        val coupleId: String,
        val femaleUserId: String,
        val maleUserId: String,
        val currentUserGender: Gender
    ) : CookingSessionEvent()

    // Mevcut session'a katıl (genel)
    data class JoinSession(
        val sessionId: String,
        val currentUserGender: Gender
    ) : CookingSessionEvent()

    // Mevcut session'a katıl
    data class JoinWaitingSession(
        val sessionId: String,
        val currentUserGender: Gender
    ) : CookingSessionEvent()

    // Adım tamamlama
    object CompleteCurrentStep : CookingSessionEvent()

    // Sonraki adıma geç
    object MoveToNextStep : CookingSessionEvent()

    // Session'ı duraklat
    object PauseSession : CookingSessionEvent()

    // Session'ı devam ettir
    object ResumeSession : CookingSessionEvent()

    // Session'ı tamamla
    object CompleteSession : CookingSessionEvent()

    // Dialogları kapat
    object DismissCoopDialog : CookingSessionEvent()
    object DismissWaitingDialog : CookingSessionEvent()
    object DismissCompletionDialog : CookingSessionEvent()
    object ShowCoopModeDialog : CookingSessionEvent()

    // Session yönetimi
    object CancelWaitingSession : CookingSessionEvent()
    data class CleanUpOldSessions(val accountId: String) : CookingSessionEvent()

    // Hata durumunu temizle
    object ClearError : CookingSessionEvent()
}

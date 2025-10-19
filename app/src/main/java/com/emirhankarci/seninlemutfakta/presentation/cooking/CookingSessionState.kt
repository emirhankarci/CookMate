package com.emirhankarci.seninlemutfakta.presentation.cooking

import com.emirhankarci.seninlemutfakta.data.model.*

data class CookingSessionState(
    // Session bilgileri
    val session: CookingSession? = null,
    val recipe: Recipe? = null,
    val isLoading: Boolean = false,
    val error: String? = null,

    // Kullanıcı bilgisi
    val currentUserGender: Gender = Gender.FEMALE,
    val currentUserId: String = "",
    val partnerUserId: String = "",

    // Adım bilgileri
    val currentStep: RecipeStep? = null,
    val myProgress: StepProgress = StepProgress(),
    val partnerProgress: StepProgress = StepProgress(),

    // Connection durumu
    val isConnected: Boolean = true,
    val partnerConnectionStatus: PartnerConnectionStatus = PartnerConnectionStatus.UNKNOWN,

    // Dialog durumları
    val showCoopModeDialog: Boolean = false,
    val showWaitingForPartnerDialog: Boolean = false,
    val showCompletionDialog: Boolean = false,
    val isCreatorWaitingForPartner: Boolean = false
) {
    // Helper: Benim adımım tamamlandı mı?
    fun isMyStepCompleted(): Boolean = myProgress.isCompleted

    // Helper: Eşim adımını tamamladı mı?
    fun isPartnerStepCompleted(): Boolean = partnerProgress.isCompleted

    // Helper: İkimiz de tamamladık mı?
    fun areBothCompleted(): Boolean = isMyStepCompleted() && isPartnerStepCompleted()

    // Helper: Ben bekliyorum mu?
    fun amIWaiting(): Boolean = myProgress.isWaiting

    // Helper: Eşim bekliyor mu?
    fun isPartnerWaiting(): Boolean = partnerProgress.isWaiting

    // Helper: Bir sonraki adıma geçebilir miyiz?
    fun canProceedToNextStep(): Boolean {
        return session?.canProceedToNextStep() ?: false
    }

    // Helper: Session aktif mi?
    fun isSessionActive(): Boolean {
        return session?.isSessionActive() ?: false
    }

    // Helper: Kaç adım kaldı?
    fun getRemainingSteps(): Int {
        val current = session?.currentStep ?: 0
        val total = session?.totalSteps ?: 0
        return total - current
    }

    // Helper: Progress yüzdesi
    fun getProgressPercentage(): Int {
        return session?.getProgressPercentage() ?: 0
    }

    // Helper: Eş durumu mesajı
    fun getPartnerStatusMessage(): String {
        return when {
            !isConnected -> "Bağlantı bekleniyor..."
            partnerConnectionStatus == PartnerConnectionStatus.OFFLINE -> "Eşiniz çevrimdışı"
            partnerConnectionStatus == PartnerConnectionStatus.DISCONNECTED -> "Eşiniz bağlantı kesildi"
            isPartnerWaiting() -> "Eşiniz sizi bekliyor! ⏳"
            isPartnerStepCompleted() -> "Eşiniz hazır! ✅"
            else -> "Eşiniz yapıyor..."
        }
    }
}

enum class PartnerConnectionStatus {
    ONLINE,
    OFFLINE,
    DISCONNECTED,
    UNKNOWN
}

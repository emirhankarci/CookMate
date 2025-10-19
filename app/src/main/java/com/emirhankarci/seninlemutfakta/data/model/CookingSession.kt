package com.emirhankarci.seninlemutfakta.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName

data class CookingSession(
    val sessionId: String = "",
    val recipeId: String = "",
    val countryCode: String = "",
    val accountId: String = "",

    @get:PropertyName("isCoopMode")
    @set:PropertyName("isCoopMode")
    var isCoopMode: Boolean = false,

    val femaleUserId: String = "",
    val maleUserId: String = "",
    val currentStep: Int = 0,
    val totalSteps: Int = 0,
    val status: SessionStatus = SessionStatus.WAITING,
    val startedAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val femaleProgress: StepProgress = StepProgress(),
    val maleProgress: StepProgress = StepProgress(),

    // Firebase computed field - ignore during deserialization
    @get:PropertyName("sessionActive")
    @set:PropertyName("sessionActive")
    var sessionActive: Boolean = false
) {
    constructor() : this(
        "", "", "", "", false, "", "", 0, 0,
        SessionStatus.WAITING, System.currentTimeMillis(),
        System.currentTimeMillis(), StepProgress(), StepProgress(), false
    )

    // Helper functions
    fun getProgressByGender(gender: Gender): StepProgress {
        return when (gender) {
            Gender.FEMALE -> femaleProgress
            Gender.MALE -> maleProgress
        }
    }

    fun getPartnerProgress(gender: Gender): StepProgress {
        return when (gender) {
            Gender.FEMALE -> maleProgress
            Gender.MALE -> femaleProgress
        }
    }

    fun canProceedToNextStep(): Boolean {
        return if (isCoopMode) {
            // Coop mode: İkisi de tamamlamalı
            femaleProgress.isCompleted && maleProgress.isCompleted
        } else {
            // Solo mode: Sadece aktif kullanıcı tamamlamalı
            false  // Otomatik geçiş YOK, kullanıcı manuel "Sonraki Adım" basacak
        }
    }

    fun isSessionActive(): Boolean {
        return status == SessionStatus.IN_PROGRESS || status == SessionStatus.WAITING
    }

    fun isCompleted(): Boolean {
        return status == SessionStatus.COMPLETED
    }

    fun getProgressPercentage(): Int {
        return if (totalSteps > 0) {
            ((currentStep.toFloat() / totalSteps) * 100).toInt()
        } else 0
    }
}
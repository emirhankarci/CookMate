package com.emirhankarci.cookmate.data.model

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
    val maleProgress: StepProgress = StepProgress()
) {
    constructor() : this(
        "", "", "", "", false, "", "", 0, 0,
        SessionStatus.WAITING, System.currentTimeMillis(),
        System.currentTimeMillis(), StepProgress(), StepProgress()
    )

    // Helper functions
    @Exclude
    fun getProgressByGender(gender: Gender): StepProgress {
        return when (gender) {
            Gender.FEMALE -> femaleProgress
            Gender.MALE -> maleProgress
        }
    }

    @Exclude
    fun getPartnerProgress(gender: Gender): StepProgress {
        return when (gender) {
            Gender.FEMALE -> maleProgress
            Gender.MALE -> femaleProgress
        }
    }

    @Exclude
    fun canProceedToNextStep(): Boolean {
        return if (isCoopMode) {
            // Coop mode: İkisi de tamamlamalı
            femaleProgress.isCompleted && maleProgress.isCompleted
        } else {
            // Solo mode: Aktif kullanıcı tamamladı mı kontrol et
            femaleProgress.isCompleted || maleProgress.isCompleted
        }
    }

    @Exclude
    fun isSessionActive(): Boolean {
        return status == SessionStatus.IN_PROGRESS || status == SessionStatus.WAITING
    }

    @Exclude
    fun isCompleted(): Boolean {
        return status == SessionStatus.COMPLETED
    }

    @Exclude
    fun getProgressPercentage(): Int {
        return if (totalSteps > 0) {
            ((currentStep.toFloat() / totalSteps) * 100).toInt()
        } else 0
    }
}

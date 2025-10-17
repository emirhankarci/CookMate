package com.emirhankarci.seninlemutfakta.data.model

data class CookingSession(
    val sessionId: String = "",
    val recipeId: String = "",
    val countryCode: String = "",
    val accountId: String = "",
    val isCoopMode: Boolean = false,
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
            femaleProgress.isCompleted && maleProgress.isCompleted
        } else {
            true
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
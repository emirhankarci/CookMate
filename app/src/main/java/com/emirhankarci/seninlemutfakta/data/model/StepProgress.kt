package com.emirhankarci.seninlemutfakta.data.model

data class StepProgress(
    val currentStepIndex: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: Long = 0,
    val isWaiting: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis()
) {
    constructor() : this(0, false, 0, false, true, System.currentTimeMillis())

    // Helper functions
    fun isWaitingForPartner(): Boolean = isWaiting && isCompleted

    fun getTimeSinceCompletion(): Long {
        return if (isCompleted) System.currentTimeMillis() - completedAt else 0
    }
}
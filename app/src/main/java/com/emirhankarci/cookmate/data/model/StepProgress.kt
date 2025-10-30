package com.emirhankarci.cookmate.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName

data class StepProgress(
    val currentStepIndex: Int = 0,

    @get:PropertyName("isCompleted")
    @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,

    val completedAt: Long = 0,

    @get:PropertyName("isWaiting")
    @set:PropertyName("isWaiting")
    var isWaiting: Boolean = false,

    @get:PropertyName("isOnline")
    @set:PropertyName("isOnline")
    var isOnline: Boolean = true,

    val lastSeen: Long = System.currentTimeMillis()
) {
    constructor() : this(0, false, 0, false, true, System.currentTimeMillis())

    // Helper functions
    @Exclude
    fun isWaitingForPartner(): Boolean = isWaiting && isCompleted

    @Exclude
    fun getTimeSinceCompletion(): Long {
        return if (isCompleted) System.currentTimeMillis() - completedAt else 0
    }
}

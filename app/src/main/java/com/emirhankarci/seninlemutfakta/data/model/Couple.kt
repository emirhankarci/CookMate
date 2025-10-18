package com.emirhankarci.seninlemutfakta.data.model

data class Couple(
    val coupleId: String = "",
    val inviteCode: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val femaleUserId: String = "",
    val maleUserId: String = "",
    val isComplete: Boolean = false
) {
    constructor() : this("", "", "", System.currentTimeMillis(), "", "", false)

    // Helper functions
    fun isMember(userId: String): Boolean {
        return femaleUserId == userId || maleUserId == userId
    }

    fun getPartnerUserId(currentUserId: String): String? {
        return when (currentUserId) {
            femaleUserId -> maleUserId.takeIf { it.isNotEmpty() }
            maleUserId -> femaleUserId.takeIf { it.isNotEmpty() }
            else -> null
        }
    }

    fun getUserGender(userId: String): Gender? {
        return when (userId) {
            femaleUserId -> Gender.FEMALE
            maleUserId -> Gender.MALE
            else -> null
        }
    }

    fun needsPartner(): Boolean {
        return !isComplete && (femaleUserId.isEmpty() || maleUserId.isEmpty())
    }
}

package com.emirhankarci.seninlemutfakta.data.model

data class Couple(
    val coupleId: String = "",
    val coupleName: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val femaleProfile: UserProfile = UserProfile(),
    val maleProfile: UserProfile = UserProfile(),
    val femaleProfileLocked: Boolean = false,
    val maleProfileLocked: Boolean = false,
    val femaleProfileLockedBy: String = "", // Device/User ID that locked the profile
    val maleProfileLockedBy: String = ""
) {
    constructor() : this("", "", "", System.currentTimeMillis(), UserProfile(), UserProfile(), false, false, "", "")

    // Helper functions
    fun getProfile(gender: Gender): UserProfile {
        return when (gender) {
            Gender.FEMALE -> femaleProfile
            Gender.MALE -> maleProfile
        }
    }

    fun updateProfile(gender: Gender, updatedProfile: UserProfile): Couple {
        return when (gender) {
            Gender.FEMALE -> this.copy(femaleProfile = updatedProfile)
            Gender.MALE -> this.copy(maleProfile = updatedProfile)
        }
    }

    fun isProfileLocked(gender: Gender): Boolean {
        return when (gender) {
            Gender.FEMALE -> femaleProfileLocked
            Gender.MALE -> maleProfileLocked
        }
    }

    fun getProfileLockedBy(gender: Gender): String {
        return when (gender) {
            Gender.FEMALE -> femaleProfileLockedBy
            Gender.MALE -> maleProfileLockedBy
        }
    }
}

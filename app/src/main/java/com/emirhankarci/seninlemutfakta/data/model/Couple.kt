package com.emirhankarci.seninlemutfakta.data.model

data class Couple(
    val coupleId: String = "",
    val coupleName: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val femaleProfile: UserProfile = UserProfile(),
    val maleProfile: UserProfile = UserProfile()
) {
    constructor() : this("", "", "", System.currentTimeMillis(), UserProfile(), UserProfile())

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
}

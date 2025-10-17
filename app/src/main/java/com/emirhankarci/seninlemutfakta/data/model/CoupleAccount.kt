package com.emirhankarci.seninlemutfakta.data.model

data class CoupleAccount(
    val coupleId: String = "",
    val coupleName: String = "",
    val femaleProfile: UserProfile? = null,
    val maleProfile: UserProfile? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", null, null, System.currentTimeMillis())

    // Helper functions
    fun getProfileByGender(gender: Gender): UserProfile? {
        return when (gender) {
            Gender.FEMALE -> femaleProfile
            Gender.MALE -> maleProfile
        }
    }

    fun getPartnerProfile(gender: Gender): UserProfile? {
        return when (gender) {
            Gender.FEMALE -> maleProfile
            Gender.MALE -> femaleProfile
        }
    }
}
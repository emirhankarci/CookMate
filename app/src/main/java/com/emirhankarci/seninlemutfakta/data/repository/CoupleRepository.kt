package com.emirhankarci.seninlemutfakta.data.repository

import com.emirhankarci.seninlemutfakta.data.model.Couple
import com.emirhankarci.seninlemutfakta.data.model.Gender
import com.emirhankarci.seninlemutfakta.data.model.UserProfile
import com.emirhankarci.seninlemutfakta.data.remote.FirebaseDataSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoupleRepository @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) {

    // Yeni çift profili oluştur (Register sonrası)
    suspend fun createCouple(
        userId: String,
        email: String,
        coupleName: String
    ): Result<Couple> {
        return try {
            val coupleId = userId // Firebase Auth UID'yi couple ID olarak kullan

            // Her iki gender için boş profil oluştur
            val femaleProfile = UserProfile(
                userId = "${userId}_female",
                name = "",
                gender = Gender.FEMALE,
                avatarUrl = "",
                completedRecipes = emptyList(),
                badges = emptyList(),
                unlockedCountries = listOf("france", "italy"), // Başlangıç ülkeleri
                passportStamps = emptyList()
            )

            val maleProfile = UserProfile(
                userId = "${userId}_male",
                name = "",
                gender = Gender.MALE,
                avatarUrl = "",
                completedRecipes = emptyList(),
                badges = emptyList(),
                unlockedCountries = listOf("france", "italy"), // Başlangıç ülkeleri
                passportStamps = emptyList()
            )

            val couple = Couple(
                coupleId = coupleId,
                coupleName = coupleName,
                email = email,
                createdAt = System.currentTimeMillis(),
                femaleProfile = femaleProfile,
                maleProfile = maleProfile
            )

            firebaseDataSource.getCoupleRef(coupleId).setValue(couple).await()
            Result.success(couple)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kullanıcının çift bilgilerini getir
    suspend fun getUserCouple(userId: String): Result<Couple?> {
        return try {
            val snapshot = firebaseDataSource.getCoupleRef(userId).get().await()
            val couple = snapshot.getValue(Couple::class.java)
            Result.success(couple)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Profil güncelle
    suspend fun updateProfile(
        coupleId: String,
        gender: Gender,
        updatedProfile: UserProfile
    ): Result<Unit> {
        return try {
            val profilePath = when (gender) {
                Gender.FEMALE -> "femaleProfile"
                Gender.MALE -> "maleProfile"
            }

            firebaseDataSource.getCoupleRef(coupleId)
                .child(profilePath)
                .setValue(updatedProfile)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Profil bilgisi getir
    suspend fun getProfile(coupleId: String, gender: Gender): Result<UserProfile?> {
        return try {
            val snapshot = firebaseDataSource.getCoupleRef(coupleId).get().await()
            val couple = snapshot.getValue(Couple::class.java)

            val profile = when (gender) {
                Gender.FEMALE -> couple?.femaleProfile
                Gender.MALE -> couple?.maleProfile
            }

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

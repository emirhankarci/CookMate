package com.emirhankarci.seninlemutfakta.data.repository

import com.emirhankarci.seninlemutfakta.data.model.Couple
import com.emirhankarci.seninlemutfakta.data.model.Gender
import com.emirhankarci.seninlemutfakta.data.model.UserProfile
import com.emirhankarci.seninlemutfakta.data.remote.FirebaseDataSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    // Profil kilitle
    suspend fun lockProfile(
        coupleId: String,
        gender: Gender,
        userId: String
    ): Result<Unit> {
        return try {
            val coupleRef = firebaseDataSource.getCoupleRef(coupleId)

            val updates = when (gender) {
                Gender.FEMALE -> mapOf(
                    "femaleProfileLocked" to true,
                    "femaleProfileLockedBy" to userId
                )
                Gender.MALE -> mapOf(
                    "maleProfileLocked" to true,
                    "maleProfileLockedBy" to userId
                )
            }

            // Lock the profile
            coupleRef.updateChildren(updates).await()

            // Set up auto-unlock on disconnect
            val disconnectUpdates = when (gender) {
                Gender.FEMALE -> mapOf(
                    "femaleProfileLocked" to false,
                    "femaleProfileLockedBy" to ""
                )
                Gender.MALE -> mapOf(
                    "maleProfileLocked" to false,
                    "maleProfileLockedBy" to ""
                )
            }

            // This will automatically unlock the profile when connection is lost
            coupleRef.onDisconnect().updateChildren(disconnectUpdates).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Profil kilidini aç
    suspend fun unlockProfile(
        coupleId: String,
        gender: Gender
    ): Result<Unit> {
        return try {
            val coupleRef = firebaseDataSource.getCoupleRef(coupleId)

            val updates = when (gender) {
                Gender.FEMALE -> mapOf(
                    "femaleProfileLocked" to false,
                    "femaleProfileLockedBy" to ""
                )
                Gender.MALE -> mapOf(
                    "maleProfileLocked" to false,
                    "maleProfileLockedBy" to ""
                )
            }

            coupleRef.updateChildren(updates).await()

            // Cancel the onDisconnect trigger since we're manually unlocking
            coupleRef.onDisconnect().cancel().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Real-time couple data dinle
    fun observeCouple(coupleId: String): Flow<Couple?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val couple = snapshot.getValue(Couple::class.java)
                trySend(couple)
            }

            override fun onCancelled(error: DatabaseError) {
                // Permission hatası (logout sonrası) normal bir durum, crash etme
                // Sadece flow'u kapat
                if (error.code == DatabaseError.PERMISSION_DENIED) {
                    // Kullanıcı logout olmuş, sessizce kapat
                    close()
                } else {
                    // Diğer hatalar için exception fırlat
                    close(error.toException())
                }
            }
        }

        firebaseDataSource.getCoupleRef(coupleId).addValueEventListener(listener)

        awaitClose {
            firebaseDataSource.getCoupleRef(coupleId).removeEventListener(listener)
        }
    }

    // Tüm profil kilitlerini temizle (acil durum için)
    suspend fun unlockAllProfiles(coupleId: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "femaleProfileLocked" to false,
                "femaleProfileLockedBy" to "",
                "maleProfileLocked" to false,
                "maleProfileLockedBy" to ""
            )

            firebaseDataSource.getCoupleRef(coupleId)
                .updateChildren(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

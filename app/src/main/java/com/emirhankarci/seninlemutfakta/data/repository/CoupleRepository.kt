package com.emirhankarci.seninlemutfakta.data.repository

import com.emirhankarci.seninlemutfakta.data.model.Couple
import com.emirhankarci.seninlemutfakta.data.model.Gender
import com.emirhankarci.seninlemutfakta.data.remote.FirebaseDataSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class CoupleRepository @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) {

    // Yeni çift oluştur
    suspend fun createCouple(creatorUserId: String, creatorGender: Gender): Result<Couple> {
        return try {
            val coupleId = firebaseDataSource.generateCoupleId()
            val inviteCode = generateInviteCode()

            val couple = Couple(
                coupleId = coupleId,
                inviteCode = inviteCode,
                createdBy = creatorUserId,
                createdAt = System.currentTimeMillis(),
                femaleUserId = if (creatorGender == Gender.FEMALE) creatorUserId else "",
                maleUserId = if (creatorGender == Gender.MALE) creatorUserId else "",
                isComplete = false
            )

            firebaseDataSource.getCoupleRef(coupleId).setValue(couple).await()
            Result.success(couple)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Davet kodu ile çifte katıl
    suspend fun joinCoupleByInviteCode(inviteCode: String, userId: String, userGender: Gender): Result<Couple> {
        return try {
            println("🔍 REPO JOIN: Searching for inviteCode=$inviteCode")
            
            // Index sorunu için tüm couples'ı al ve client-side filtrele
            val snapshot = firebaseDataSource.getCouplesRef()
                .get()
                .await()

            println("🔍 REPO JOIN: Found ${snapshot.childrenCount} total couples, filtering by invite code")

            var foundCouple: Couple? = null
            snapshot.children.forEach { child ->
                val couple = child.getValue(Couple::class.java)
                println("🔍 REPO JOIN: Checking couple: inviteCode=${couple?.inviteCode}, needsPartner=${couple?.needsPartner()}")
                if (couple != null && couple.inviteCode == inviteCode && couple.needsPartner()) {
                    foundCouple = couple
                    println("✅ REPO JOIN: Found suitable couple: $couple")
                    return@forEach
                }
            }

            val couple = foundCouple ?: return Result.failure(Exception("Geçersiz davet kodu veya çift tamamlanmış"))

            // Kullanıcıyı çifte ekle
            val updatedCouple = when (userGender) {
                Gender.FEMALE -> {
                    if (couple.femaleUserId.isNotEmpty()) {
                        return Result.failure(Exception("Bu çiftte kadın kullanıcı zaten var"))
                    }
                    couple.copy(
                        femaleUserId = userId,
                        isComplete = couple.maleUserId.isNotEmpty()
                    )
                }
                Gender.MALE -> {
                    if (couple.maleUserId.isNotEmpty()) {
                        return Result.failure(Exception("Bu çiftte erkek kullanıcı zaten var"))
                    }
                    couple.copy(
                        maleUserId = userId,
                        isComplete = couple.femaleUserId.isNotEmpty()
                    )
                }
            }

            println("🔍 REPO JOIN: Updating couple to: $updatedCouple")
            firebaseDataSource.getCoupleRef(couple.coupleId).setValue(updatedCouple).await()
            println("✅ REPO JOIN: Successfully updated couple")
            Result.success(updatedCouple)
        } catch (e: Exception) {
            println("❌ REPO JOIN: Exception: ${e.message}")
            Result.failure(e)
        }
    }

    // Kullanıcının çiftini bul
    suspend fun getUserCouple(userId: String): Result<Couple?> {
        return try {
            val snapshot = firebaseDataSource.getCouplesRef().get().await()

            var userCouple: Couple? = null
            snapshot.children.forEach { child ->
                val couple = child.getValue(Couple::class.java)
                if (couple != null && couple.isMember(userId)) {
                    userCouple = couple
                    return@forEach
                }
            }

            Result.success(userCouple)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Çift ID'sine göre çifti getir
    suspend fun getCoupleById(coupleId: String): Result<Couple?> {
        return try {
            val snapshot = firebaseDataSource.getCoupleRef(coupleId).get().await()
            val couple = snapshot.getValue(Couple::class.java)
            Result.success(couple)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Davet kodu oluştur (6 haneli)
    private fun generateInviteCode(): String {
        return (100000..999999).random().toString()
    }
}

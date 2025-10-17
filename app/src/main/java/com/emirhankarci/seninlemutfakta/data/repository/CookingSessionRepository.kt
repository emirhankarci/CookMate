package com.emirhankarci.seninlemutfakta.data.repository

import com.emirhankarci.seninlemutfakta.data.model.*
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
class CookingSessionRepository @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) {

    // ==================== SESSION CREATION ====================

    suspend fun createSession(
        recipeId: String,
        countryCode: String,
        accountId: String,
        isCoopMode: Boolean,
        femaleUserId: String,
        maleUserId: String,
        totalSteps: Int
    ): Result<String> {
        return try {
            val sessionId = firebaseDataSource.generateSessionId()

            val session = CookingSession(
                sessionId = sessionId,
                recipeId = recipeId,
                countryCode = countryCode,
                accountId = accountId,
                isCoopMode = isCoopMode,
                femaleUserId = femaleUserId,
                maleUserId = maleUserId,
                currentStep = 0,
                totalSteps = totalSteps,
                status = SessionStatus.WAITING,
                startedAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis(),
                femaleProgress = StepProgress(),
                maleProgress = StepProgress()
            )

            firebaseDataSource.getCookingSessionRef(sessionId).setValue(session).await()
            Result.success(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== SESSION STATUS ====================

    suspend fun startSession(sessionId: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "status" to SessionStatus.IN_PROGRESS.name,
                "lastUpdated" to System.currentTimeMillis()
            )

            firebaseDataSource.getCookingSessionRef(sessionId)
                .updateChildren(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pauseSession(sessionId: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "status" to SessionStatus.PAUSED.name,
                "lastUpdated" to System.currentTimeMillis()
            )

            firebaseDataSource.getCookingSessionRef(sessionId)
                .updateChildren(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeSession(sessionId: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "status" to SessionStatus.COMPLETED.name,
                "lastUpdated" to System.currentTimeMillis()
            )

            firebaseDataSource.getCookingSessionRef(sessionId)
                .updateChildren(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== STEP PROGRESS -核心功能! ====================

    suspend fun completeStep(
        sessionId: String,
        gender: Gender,
        stepIndex: Int
    ): Result<Unit> {
        return try {
            val progressPath = if (gender == Gender.FEMALE) "femaleProgress" else "maleProgress"

            val updates = mapOf(
                "$progressPath/currentStepIndex" to stepIndex,
                "$progressPath/isCompleted" to true,
                "$progressPath/completedAt" to System.currentTimeMillis(),
                "$progressPath/isWaiting" to true,
                "lastUpdated" to System.currentTimeMillis()
            )

            firebaseDataSource.getCookingSessionRef(sessionId)
                .updateChildren(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun moveToNextStep(sessionId: String, nextStepIndex: Int): Result<Unit> {
        return try {
            val updates = mapOf(
                "currentStep" to nextStepIndex,
                "femaleProgress/isCompleted" to false,
                "femaleProgress/isWaiting" to false,
                "femaleProgress/completedAt" to 0,
                "maleProgress/isCompleted" to false,
                "maleProgress/isWaiting" to false,
                "maleProgress/completedAt" to 0,
                "lastUpdated" to System.currentTimeMillis()
            )

            firebaseDataSource.getCookingSessionRef(sessionId)
                .updateChildren(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== REAL-TIME OBSERVATION - 实时监听! ====================

    fun observeSession(sessionId: String): Flow<CookingSession?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val session = snapshot.getValue(CookingSession::class.java)
                trySend(session)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val ref = firebaseDataSource.getCookingSessionRef(sessionId)
        ref.addValueEventListener(listener)

        awaitClose { ref.removeEventListener(listener) }
    }

    // ==================== PARTNER STATUS ====================

    suspend fun updateOnlineStatus(
        sessionId: String,
        gender: Gender,
        isOnline: Boolean
    ): Result<Unit> {
        return try {
            val progressPath = if (gender == Gender.FEMALE) "femaleProgress" else "maleProgress"

            val updates = mapOf(
                "$progressPath/isOnline" to isOnline,
                "$progressPath/lastSeen" to System.currentTimeMillis(),
                "lastUpdated" to System.currentTimeMillis()
            )

            firebaseDataSource.getCookingSessionRef(sessionId)
                .updateChildren(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== SESSION QUERIES ====================

    suspend fun getActiveSessionForCouple(accountId: String): Result<CookingSession?> {
        return try {
            val snapshot = firebaseDataSource.getCookingSessionsRef()
                .orderByChild("accountId")
                .equalTo(accountId)
                .get()
                .await()

            val sessions = mutableListOf<CookingSession>()
            snapshot.children.forEach { child ->
                child.getValue(CookingSession::class.java)?.let {
                    if (it.isSessionActive()) {
                        sessions.add(it)
                    }
                }
            }

            Result.success(sessions.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWaitingSessionForUser(userId: String): Result<CookingSession?> {
        return try {
            // Tüm session'ları al ve client-side filtrele
            val snapshot = firebaseDataSource.getCookingSessionsRef()
                .get()
                .await()

            var waitingSession: CookingSession? = null

            snapshot.children.forEach { child ->
                val session = child.getValue(CookingSession::class.java)
                if (session != null &&
                    session.status == SessionStatus.WAITING &&
                    session.isCoopMode &&
                    (session.femaleUserId == userId || session.maleUserId == userId)) {
                    waitingSession = session
                    return@forEach
                }
            }

            Result.success(waitingSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Couple ID ile waiting session bulma (daha performanslı)
    suspend fun getWaitingSessionForCouple(accountId: String, currentRecipeId: String): Result<CookingSession?> {
        return try {
            val snapshot = firebaseDataSource.getCookingSessionsRef()
                .orderByChild("accountId")
                .equalTo(accountId)
                .get()
                .await()

            var waitingSession: CookingSession? = null

            snapshot.children.forEach { child ->
                val session = child.getValue(CookingSession::class.java)
                if (session != null &&
                    session.status == SessionStatus.WAITING &&
                    session.isCoopMode &&
                    session.recipeId == currentRecipeId) {
                    waitingSession = session
                    return@forEach
                }
            }

            Result.success(waitingSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Couple için herhangi bir waiting session bulma (tarif seçilmeden önce)
    suspend fun getAnyWaitingSessionForCouple(accountId: String): Result<CookingSession?> {
        return try {
            val snapshot = firebaseDataSource.getCookingSessionsRef()
                .orderByChild("accountId")
                .equalTo(accountId)
                .get()
                .await()

            var waitingSession: CookingSession? = null

            snapshot.children.forEach { child ->
                val session = child.getValue(CookingSession::class.java)
                if (session != null &&
                    session.status == SessionStatus.WAITING &&
                    session.isCoopMode) {
                    waitingSession = session
                    return@forEach
                }
            }

            Result.success(waitingSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== CONNECTION MONITORING ====================

    fun observeConnectionStatus(): Flow<Boolean> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.getValue(Boolean::class.java) ?: false
                trySend(isConnected)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val ref = firebaseDataSource.getConnectionRef()
        ref.addValueEventListener(listener)

        awaitClose { ref.removeEventListener(listener) }
    }

    // Partner'ın son görülme zamanını kontrol et
    fun isPartnerTimeout(lastSeen: Long, timeoutSeconds: Int = 30): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastSeen
        return timeDiff > (timeoutSeconds * 1000)
    }
}

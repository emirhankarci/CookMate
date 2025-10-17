package com.emirhankarci.seninlemutfakta.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDataSource @Inject constructor() {

    // Firebase instances
    // Europe-west1 bölgesine bağlan
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://seninle-mutfakta-default-rtdb.europe-west1.firebasedatabase.app"
    )
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Database references
    fun getCouplesRef(): DatabaseReference = database.getReference("couples")

    fun getCoupleRef(coupleId: String): DatabaseReference =
        getCouplesRef().child(coupleId)

    fun getCookingSessionsRef(): DatabaseReference =
        database.getReference("cookingSessions")

    fun getCookingSessionRef(sessionId: String): DatabaseReference =
        getCookingSessionsRef().child(sessionId)

    fun getRecipesRef(): DatabaseReference =
        database.getReference("recipes")

    fun getRecipesByCountryRef(countryCode: String): DatabaseReference =
        getRecipesRef().child(countryCode)

    fun getRecipeRef(countryCode: String, recipeId: String): DatabaseReference =
        getRecipesByCountryRef(countryCode).child(recipeId)

    fun getCountriesRef(): DatabaseReference =
        database.getReference("countries")

    fun getCountryRef(countryCode: String): DatabaseReference =
        getCountriesRef().child(countryCode)

    fun getUserPostsRef(): DatabaseReference =
        database.getReference("userPosts")

    fun getUserPostRef(postId: String): DatabaseReference =
        getUserPostsRef().child(postId)

    fun getBadgesRef(): DatabaseReference =
        database.getReference("badges")

    // Helper: Generate unique IDs
    fun generateCoupleId(): String = getCouplesRef().push().key ?: ""
    fun generateSessionId(): String = getCookingSessionsRef().push().key ?: ""
    fun generatePostId(): String = getUserPostsRef().push().key ?: ""

    // Helper: Get current user ID
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // ==================== CONNECTION MONITORING ====================

    fun getConnectionRef(): DatabaseReference {
        return database.getReference(".info/connected")
    }

    fun isConnected(): DatabaseReference {
        return database.getReference(".info/connected")
    }
}
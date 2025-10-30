package com.emirhankarci.cookmate.data.repository

import com.emirhankarci.cookmate.data.model.*
import com.emirhankarci.cookmate.data.remote.FirebaseDataSource
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
class FirebaseRepository @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) {

    // ==================== COUPLE OPERATIONS ====================

    suspend fun createCoupleAccount(
        coupleName: String,
        femaleProfile: UserProfile,
        maleProfile: UserProfile
    ): Result<String> {
        return try {
            val coupleId = firebaseDataSource.generateCoupleId()
            val coupleAccount = CoupleAccount(
                coupleId = coupleId,
                coupleName = coupleName,
                femaleProfile = femaleProfile,
                maleProfile = maleProfile,
                createdAt = System.currentTimeMillis()
            )

            firebaseDataSource.getCoupleRef(coupleId).setValue(coupleAccount).await()
            Result.success(coupleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCoupleAccount(coupleId: String): Result<CoupleAccount?> {
        return try {
            val snapshot = firebaseDataSource.getCoupleRef(coupleId).get().await()
            val couple = snapshot.getValue(CoupleAccount::class.java)
            Result.success(couple)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeCoupleAccount(coupleId: String): Flow<CoupleAccount?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val couple = snapshot.getValue(CoupleAccount::class.java)
                trySend(couple)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val ref = firebaseDataSource.getCoupleRef(coupleId)
        ref.addValueEventListener(listener)

        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun updateUserProfile(
        coupleId: String,
        gender: Gender,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            val path = if (gender == Gender.FEMALE) "femaleProfile" else "maleProfile"

            val updateMap = updates.mapKeys { "$path/${it.key}" }
            firebaseDataSource.getCoupleRef(coupleId).updateChildren(updateMap).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== COUNTRY OPERATIONS ====================

    suspend fun getAllCountries(): Result<List<Country>> {
        return try {
            val snapshot = firebaseDataSource.getCountriesRef().get().await()
            val countries = mutableListOf<Country>()

            snapshot.children.forEach { child ->
                child.getValue(Country::class.java)?.let { countries.add(it) }
            }

            Result.success(countries.sortedBy { it.order })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCountry(countryCode: String): Result<Country?> {
        return try {
            val snapshot = firebaseDataSource.getCountryRef(countryCode).get().await()
            val country = snapshot.getValue(Country::class.java)
            Result.success(country)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== RECIPE OPERATIONS ====================

    suspend fun getRecipesByCountry(countryCode: String): Result<List<Recipe>> {
        return try {
            val snapshot = firebaseDataSource.getRecipesByCountryRef(countryCode).get().await()
            val recipes = mutableListOf<Recipe>()

            snapshot.children.forEach { child ->
                child.getValue(Recipe::class.java)?.let { recipes.add(it) }
            }

            Result.success(recipes.sortedBy { it.order })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecipe(countryCode: String, recipeId: String): Result<Recipe?> {
        return try {
            val snapshot = firebaseDataSource.getRecipeRef(countryCode, recipeId).get().await()
            val recipe = snapshot.getValue(Recipe::class.java)
            Result.success(recipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== USER POST OPERATIONS ====================

    suspend fun createUserPost(post: UserPost): Result<String> {
        return try {
            val postId = firebaseDataSource.generatePostId()
            val postWithId = post.copy(postId = postId)

            firebaseDataSource.getUserPostRef(postId).setValue(postWithId).await()
            Result.success(postId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllPosts(): Result<List<UserPost>> {
        return try {
            val snapshot = firebaseDataSource.getUserPostsRef()
                .orderByChild("createdAt")
                .get()
                .await()

            val posts = mutableListOf<UserPost>()
            snapshot.children.forEach { child ->
                child.getValue(UserPost::class.java)?.let { posts.add(it) }
            }

            Result.success(posts.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

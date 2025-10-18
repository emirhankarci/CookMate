package com.emirhankarci.seninlemutfakta.di

import com.emirhankarci.seninlemutfakta.data.remote.FirebaseDataSource
import com.emirhankarci.seninlemutfakta.data.repository.AuthRepository
import com.emirhankarci.seninlemutfakta.data.repository.CookingSessionRepository
import com.emirhankarci.seninlemutfakta.data.repository.CoupleRepository
import com.emirhankarci.seninlemutfakta.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseDataSource(): FirebaseDataSource {
        return FirebaseDataSource()
    }

    @Provides
    @Singleton
    fun provideFirebaseRepository(
        firebaseDataSource: FirebaseDataSource
    ): FirebaseRepository {
        return FirebaseRepository(firebaseDataSource)
    }

    @Provides
    @Singleton
    fun provideCookingSessionRepository(
        firebaseDataSource: FirebaseDataSource
    ): CookingSessionRepository {
        return CookingSessionRepository(firebaseDataSource)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): AuthRepository {
        return AuthRepository(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideCoupleRepository(
        firebaseDataSource: FirebaseDataSource
    ): CoupleRepository {
        return CoupleRepository(firebaseDataSource)
    }
}
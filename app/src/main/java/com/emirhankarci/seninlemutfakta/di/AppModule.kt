package com.emirhankarci.seninlemutfakta.di

import com.emirhankarci.seninlemutfakta.data.remote.FirebaseDataSource
import com.emirhankarci.seninlemutfakta.data.repository.CookingSessionRepository
import com.emirhankarci.seninlemutfakta.data.repository.FirebaseRepository
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
}
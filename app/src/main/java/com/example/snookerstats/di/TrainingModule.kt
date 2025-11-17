package com.example.snookerstats.di

import com.example.snookerstats.data.repository.TrainingRepositoryImpl
import com.example.snookerstats.domain.repository.TrainingRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TrainingModule {

    @Provides
    @Singleton
    fun provideTrainingRepository(firestore: FirebaseFirestore): TrainingRepository {
        return TrainingRepositoryImpl(firestore)
    }
}

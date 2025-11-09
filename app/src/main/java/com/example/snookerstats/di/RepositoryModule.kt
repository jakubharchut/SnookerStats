package com.example.snookerstats.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    // Wszystkie powiązania zostały przeniesione do FirebaseModule
}

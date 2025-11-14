package com.example.snookerstats.di

import com.example.snookerstats.util.SnackbarManager
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
    fun provideSnackbarManager(): SnackbarManager {
        return SnackbarManager()
    }
}

package com.example.snookerstats.di

import android.content.Context
import androidx.room.Room
import com.example.snookerstats.data.local.SnookerStatsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SnookerStatsDatabase {
        return Room.databaseBuilder(
            context,
            SnookerStatsDatabase::class.java,
            "snooker_stats_db"
        ).build()
    }

    @Provides
    fun provideUserDao(database: SnookerStatsDatabase) = database.userDao()

    @Provides
    fun provideMatchDao(database: SnookerStatsDatabase) = database.matchDao()
}

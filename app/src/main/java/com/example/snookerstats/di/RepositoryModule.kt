package com.example.snookerstats.di

import com.example.snookerstats.data.repository.ChatRepositoryImpl
import com.example.snookerstats.data.repository.UserRepositoryImpl
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository
}

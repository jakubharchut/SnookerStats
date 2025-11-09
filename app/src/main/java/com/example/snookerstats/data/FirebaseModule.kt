package com.example.snookerstats.data

import com.example.snookerstats.data.local.preferences.EncryptedPrefsManager
import com.example.snookerstats.data.repository.AuthRepositoryImpl
import com.example.snookerstats.data.repository.ChatRepositoryImpl
import com.example.snookerstats.data.repository.CommunityRepositoryImpl
import com.example.snookerstats.data.repository.NotificationRepositoryImpl
import com.example.snookerstats.data.repository.ProfileRepositoryImpl
import com.example.snookerstats.data.repository.UserRepositoryImpl
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.domain.repository.CommunityRepository
import com.example.snookerstats.domain.repository.NotificationRepository
import com.example.snookerstats.domain.repository.ProfileRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        prefsManager: EncryptedPrefsManager
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth, firestore, prefsManager)

    @Provides
    @Singleton
    fun provideCommunityRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): CommunityRepository = CommunityRepositoryImpl(firestore, auth)

    @Provides
    @Singleton
    fun provideProfileRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): ProfileRepository = ProfileRepositoryImpl(firestore, auth)

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): NotificationRepository = NotificationRepositoryImpl(firestore, auth)

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository
    ): UserRepository = UserRepositoryImpl(firestore, authRepository)

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository,
        userRepository: UserRepository
    ): ChatRepository = ChatRepositoryImpl(firestore, authRepository, userRepository)
}

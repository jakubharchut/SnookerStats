package com.example.snookerstats.data

import com.example.snookerstats.data.local.preferences.EncryptedPrefsManager
import com.example.snookerstats.data.repository.*
import com.example.snookerstats.domain.repository.*
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
    ): IAuthRepository = AuthRepositoryImpl(firebaseAuth, firestore, prefsManager)

    @Provides
    @Singleton
    fun provideCommunityRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): CommunityRepository = CommunityRepositoryImpl(firestore, auth)
        
    @Provides
    @Singleton
    fun provideMatchRepository(
        firestore: FirebaseFirestore,
        authRepository: IAuthRepository
    ): MatchRepository = MatchRepositoryImpl(firestore, authRepository)

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
        authRepository: IAuthRepository
    ): UserRepository = UserRepositoryImpl(firestore, authRepository)

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore,
        authRepository: IAuthRepository
    ): ChatRepository = ChatRepositoryImpl(firestore, authRepository)
}

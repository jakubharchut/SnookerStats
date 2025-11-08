package com.example.snookerstats.data

import com.example.snookerstats.data.local.preferences.EncryptedPrefsManager
import com.example.snookerstats.data.repository.AuthRepositoryImpl
import com.example.snookerstats.data.repository.CommunityRepositoryImpl
import com.example.snookerstats.data.repository.ProfileRepositoryImpl
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.CommunityRepository
import com.example.snookerstats.domain.repository.ProfileRepository
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
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        prefsManager: EncryptedPrefsManager
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestore, prefsManager)
    }

    @Provides
    @Singleton
    fun provideCommunityRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth // Dodano FirebaseAuth
    ): CommunityRepository {
        return CommunityRepositoryImpl(firestore, auth) // Przekazano FirebaseAuth
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): ProfileRepository {
        return ProfileRepositoryImpl(firestore, auth)
    }
}

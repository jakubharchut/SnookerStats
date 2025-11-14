package com.example.snookerstats.data.repository

import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.IAuthRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: IAuthRepository
) : UserRepository {

    override suspend fun getUser(userId: String): Resource<User> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("Nie znaleziono użytkownika")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Wystąpił nieznany błąd")
        }
    }

    override fun getFriends(): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading)
        try {
            val currentUserId = authRepository.currentUser?.uid ?: run {
                emit(Resource.Error("Użytkownik niezalogowany"))
                return@flow
            }
            val userDoc = firestore.collection("users").document(currentUserId).get().await()
            val friendIds = userDoc.toObject(User::class.java)?.friends ?: emptyList()

            if (friendIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            val friends = coroutineScope {
                friendIds.map {
                    async { firestore.collection("users").document(it).get().await() }
                }.awaitAll()
            }.mapNotNull { it.toObject(User::class.java) }

            emit(Resource.Success(friends))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Błąd pobierania znajomych"))
        }
    }

    override suspend fun toggleFavorite(currentUserId: String, favoriteUserId: String) {
        val userDocRef = firestore.collection("users").document(currentUserId)
        val user = (getUser(currentUserId) as? Resource.Success)?.data ?: return

        if (user.favorites.contains(favoriteUserId)) {
            userDocRef.update("favorites", FieldValue.arrayRemove(favoriteUserId)).await()
        } else {
            userDocRef.update("favorites", FieldValue.arrayUnion(favoriteUserId)).await()
        }
    }

    override fun getCurrentUserId(): String? {
        return authRepository.currentUser?.uid
    }
}

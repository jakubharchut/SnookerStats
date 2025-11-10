package com.example.snookerstats.data.repository

import com.example.snookerstats.domain.model.Chat
import com.example.snookerstats.domain.model.Message
import com.example.snookerstats.domain.repository.IAuthRepository
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: IAuthRepository
) : ChatRepository {

    private val currentUserId: String
        get() = authRepository.currentUser?.uid!!

    override fun getChats(): Flow<Resource<List<Chat>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Błąd pobierania czatów"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val chats = snapshot.toObjects(Chat::class.java)
                    trySend(Resource.Success(chats))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getMessages(chatId: String): Flow<Resource<List<Message>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Błąd pobierania wiadomości"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    trySend(Resource.Success(messages))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(chatId: String, message: String): Resource<Unit> {
        return try {
            val chatRef = firestore.collection("chats").document(chatId)
            val chatDoc = chatRef.get().await()
            val participants = chatDoc.get("participants") as? List<String>
            val recipientId = participants?.firstOrNull { it != currentUserId }

            if (recipientId == null) {
                return Resource.Error("Nie można znaleźć odbiorcy wiadomości.")
            }

            val messageData = mapOf(
                "senderId" to currentUserId,
                "text" to message,
                "timestamp" to Timestamp.now()
            )

            firestore.runBatch { batch ->
                batch.set(chatRef.collection("messages").document(), messageData)
                batch.update(
                    chatRef, mapOf(
                        "lastMessage" to message,
                        "lastMessageTimestamp" to Timestamp.now(),
                        "unreadCounts.${recipientId}" to FieldValue.increment(1)
                    )
                )
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Błąd wysyłania wiadomości")
        }
    }

    override suspend fun createOrGetChat(otherUserId: String): Resource<String> {
        val participants = listOf(currentUserId, otherUserId).sorted()
        val chatId = participants.joinToString("_")

        return try {
            val chatDoc = firestore.collection("chats").document(chatId).get().await()
            if (!chatDoc.exists()) {
                val newChat = Chat(
                    id = chatId,
                    participants = participants,
                    unreadCounts = participants.associateWith { 0 }
                )
                firestore.collection("chats").document(chatId).set(newChat).await()
            }
            Resource.Success(chatId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Błąd tworzenia czatu")
        }
    }

    override suspend fun resetUnreadCount(chatId: String): Resource<Unit> {
        return try {
            firestore.collection("chats").document(chatId)
                .update("unreadCounts.${currentUserId}", 0)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Błąd resetowania licznika")
        }
    }

    override suspend fun deleteChat(chatId: String): Resource<Unit> {
        // Ta funkcja wymagałaby usunięcia subkolekcji, co jest bardziej złożone.
        // Na razie zostawiamy proste usunięcie dokumentu.
        return try {
            firestore.collection("chats").document(chatId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Błąd usuwania czatu")
        }
    }

    override suspend fun updateUserPresenceInChat(chatId: String, userId: String?): Resource<Unit> {
        return try {
            firestore.collection("chats").document(chatId)
                .update("userPresentInChat", userId)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Błąd aktualizacji obecności")
        }
    }
}

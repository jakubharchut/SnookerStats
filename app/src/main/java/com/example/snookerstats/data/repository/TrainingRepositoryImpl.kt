package com.example.snookerstats.data.repository

import com.example.snookerstats.domain.model.TrainingAttempt
import com.example.snookerstats.domain.repository.TrainingRepository
import com.example.snookerstats.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TrainingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TrainingRepository {

    override suspend fun saveTrainingAttempt(attempt: TrainingAttempt): Resource<Unit> {
        return try {
            firestore.collection("training_attempts").add(attempt).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override fun getTrainingAttempts(userId: String, trainingType: String): Flow<Resource<List<TrainingAttempt>>> = flow {
        emit(Resource.Loading)
        val snapshot = firestore.collection("training_attempts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("trainingType", trainingType)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()
        val attempts = snapshot.toObjects(TrainingAttempt::class.java)
        emit(Resource.Success(attempts))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Unknown error"))
    }
}

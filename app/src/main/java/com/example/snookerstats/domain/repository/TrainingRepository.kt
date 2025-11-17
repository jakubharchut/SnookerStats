package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.TrainingAttempt
import com.example.snookerstats.util.Resource
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    suspend fun saveTrainingAttempt(attempt: TrainingAttempt): Resource<Unit>
    fun getTrainingAttempts(userId: String, trainingType: String): Flow<Resource<List<TrainingAttempt>>>
}

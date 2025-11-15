package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.util.Resource
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    fun getAllMatches(userId: String): Flow<Resource<List<Match>>>
    fun cancelAllJobs()
}

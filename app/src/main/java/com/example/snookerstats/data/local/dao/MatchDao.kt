package com.example.snookerstats.data.local.dao

import androidx.room.*
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.MatchStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: Match)

    @Update
    suspend fun updateMatch(match: Match)

    @Delete
    suspend fun deleteMatch(match: Match)
    
    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatchById(matchId: String)

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: String): Match?

    @Query("SELECT * FROM matches ORDER BY date DESC")
    fun getAllMatches(): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE status = 'IN_PROGRESS' LIMIT 1")
    fun getOngoingMatch(): Flow<Match?>
}

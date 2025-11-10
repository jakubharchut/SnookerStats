package com.example.snookerstats.data.local.dao

import androidx.room.*
import com.example.snookerstats.domain.model.Match

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: Match)

    @Update
    suspend fun updateMatch(match: Match)

    @Delete
    suspend fun deleteMatch(match: Match)

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: String): Match?

    @Query("SELECT * FROM matches ORDER BY date DESC")
    suspend fun getAllMatches(): List<Match>
}

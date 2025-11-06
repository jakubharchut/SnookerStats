package com.example.snookerstats.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.snookerstats.domain.model.Match
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: Match)

    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun getMatch(matchId: String): Flow<Match?>

    @Query("SELECT * FROM matches")
    fun getAllMatches(): Flow<List<Match>>
}

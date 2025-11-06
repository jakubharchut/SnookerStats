package com.example.snookerstats.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shots")
data class Shot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val frameId: Long, // Klucz obcy do Frame
    val timestamp: Long,
    val ball: String,
    val points: Int,
    val isFoul: Boolean
)

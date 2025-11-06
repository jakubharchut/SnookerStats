package com.example.snookerstats.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.snookerstats.data.local.dao.MatchDao
import com.example.snookerstats.data.local.dao.UserDao
import com.example.snookerstats.domain.model.Frame
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.User

@Database(
    entities = [User::class, Match::class, Frame::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SnookerStatsDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun matchDao(): MatchDao
}

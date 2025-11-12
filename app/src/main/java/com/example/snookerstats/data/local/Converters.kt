package com.example.snookerstats.data.local

import androidx.room.TypeConverter
import com.example.snookerstats.domain.model.Frame
import com.example.snookerstats.domain.model.MatchStatus
import com.example.snookerstats.domain.model.MatchType // Poprawny import
import com.example.snookerstats.domain.model.Shot
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromShotList(shots: List<Shot>): String {
        return gson.toJson(shots)
    }

    @TypeConverter
    fun toShotList(shotsJson: String): List<Shot> {
        val type = object : TypeToken<List<Shot>>() {}.type
        return gson.fromJson(shotsJson, type)
    }

    @TypeConverter
    fun fromFrameList(frames: List<Frame>): String {
        return gson.toJson(frames)
    }

    @TypeConverter
    fun toFrameList(framesJson: String): List<Frame> {
        val type = object : TypeToken<List<Frame>>() {}.type
        return gson.fromJson(framesJson, type)
    }

    @TypeConverter
    fun fromBreaksMap(breaks: Map<String, List<Int>>): String {
        return gson.toJson(breaks)
    }

    @TypeConverter
    fun toBreaksMap(breaksJson: String): Map<String, List<Int>> {
        val type = object : TypeToken<Map<String, List<Int>>>() {}.type
        return gson.fromJson(breaksJson, type)
    }

    @TypeConverter
    fun fromMatchStatus(status: MatchStatus): String {
        return status.name
    }

    @TypeConverter
    fun toMatchStatus(statusString: String): MatchStatus {
        return MatchStatus.valueOf(statusString)
    }

    @TypeConverter
    fun fromMatchType(type: MatchType): String {
        return type.name
    }

    @TypeConverter
    fun toMatchType(typeString: String): MatchType {
        return MatchType.valueOf(typeString)
    }

    // Usunięto błędny konwerter dla MatchFormat z pakietu UI

    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp): Long {
        return timestamp.toDate().time
    }

    @TypeConverter
    fun toTimestamp(value: Long): Timestamp {
        return Timestamp(java.util.Date(value))
    }
}

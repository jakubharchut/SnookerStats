package com.example.snookerstats.data.local

import androidx.room.TypeConverter
import com.example.snookerstats.domain.model.Frame
import com.example.snookerstats.domain.model.MatchStatus
import com.example.snookerstats.domain.model.Shot
import com.example.snookerstats.ui.screens.MatchFormat
import com.example.snookerstats.ui.screens.MatchType
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // Konwerter dla List<String>
    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return gson.toJson(list)
    }

    // Konwerter dla List<Shot>
    @TypeConverter
    fun fromShotList(shots: List<Shot>): String {
        return gson.toJson(shots)
    }

    @TypeConverter
    fun toShotList(shotsJson: String): List<Shot> {
        val type = object : TypeToken<List<Shot>>() {}.type
        return gson.fromJson(shotsJson, type)
    }

    // Konwerter dla List<Frame>
    @TypeConverter
    fun fromFrameList(frames: List<Frame>): String {
        return gson.toJson(frames)
    }

    @TypeConverter
    fun toFrameList(framesJson: String): List<Frame> {
        val type = object : TypeToken<List<Frame>>() {}.type
        return gson.fromJson(framesJson, type)
    }

    // Konwerter dla Map<String, List<Int>> (dla 'breaks' w Frame)
    @TypeConverter
    fun fromBreaksMap(breaks: Map<String, List<Int>>): String {
        return gson.toJson(breaks)
    }

    @TypeConverter
    fun toBreaksMap(breaksJson: String): Map<String, List<Int>> {
        val type = object : TypeToken<Map<String, List<Int>>>() {}.type
        return gson.fromJson(breaksJson, type)
    }

    // Konwerter dla MatchStatus
    @TypeConverter
    fun fromMatchStatus(status: MatchStatus): String {
        return status.name
    }

    @TypeConverter
    fun toMatchStatus(statusString: String): MatchStatus {
        return MatchStatus.valueOf(statusString)
    }

    // Konwerter dla MatchType
    @TypeConverter
    fun fromMatchType(type: MatchType): String {
        return type.name
    }

    @TypeConverter
    fun toMatchType(typeString: String): MatchType {
        return MatchType.valueOf(typeString)
    }

    // Konwerter dla MatchFormat
    @TypeConverter
    fun fromMatchFormat(format: MatchFormat): String {
        return format.name
    }

    @TypeConverter
    fun toMatchFormat(formatString: String): MatchFormat {
        return MatchFormat.valueOf(formatString)
    }

    // Konwerter dla Timestamp
    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp): Long {
        return timestamp.toDate().time
    }

    @TypeConverter
    fun toTimestamp(value: Long): Timestamp {
        return Timestamp(java.util.Date(value))
    }
}

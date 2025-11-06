package com.example.snookerstats.data.local

import androidx.room.TypeConverter
import com.example.snookerstats.domain.model.Frame
import com.example.snookerstats.domain.model.Shot
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
}

package com.example.liontalk.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converter {
    // List<String> -> JsonString to RoomDB
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return Gson().toJson(value)
    }

    // RoomDB to JsonString -> List<String>
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
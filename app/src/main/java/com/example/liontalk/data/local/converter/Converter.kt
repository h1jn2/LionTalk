package com.example.liontalk.data.local.converter

import androidx.room.TypeConverter
import com.example.liontalk.model.ChatUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converter {
    private val gson = Gson()
    // List<String> -> JsonString to RoomDB
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value)
    }

    // RoomDB to JsonString -> List<String>
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromUser(value: ChatUser): String = gson.toJson(value)

    @TypeConverter
    fun toUser(value: String): ChatUser = gson.fromJson(value, ChatUser::class.java)

    @TypeConverter
    fun toUserList(value: String): List<ChatUser> {
        val listType = object : TypeToken<List<ChatUser>>() {

        }.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromUserList(value: List<ChatUser>): String {
        return gson.toJson(value)
    }
}
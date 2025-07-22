package com.example.liontalk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "chat_message")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    val roomId: Int,
    val sender: String,
    val content: String,
    val createdAt: Long
)

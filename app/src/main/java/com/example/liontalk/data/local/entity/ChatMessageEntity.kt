package com.example.liontalk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.liontalk.model.ChatUser

@Entity (tableName = "chat_message")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = false) val id : Int = 0,
    val roomId: Int,
    val sender: ChatUser,
    val content: String,
    val createdAt: Long
)

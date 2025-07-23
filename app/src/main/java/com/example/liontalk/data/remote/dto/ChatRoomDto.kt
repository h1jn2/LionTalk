package com.example.liontalk.data.remote.dto

import com.example.liontalk.model.ChatUser

data class ChatRoomDto(
    val id: Int,
    val title: String,
    val owner: ChatUser,
    val users: List<ChatUser>,
    val isLocked: Boolean,
    val createdAt: Long
)
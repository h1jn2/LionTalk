package com.example.liontalk.model

data class ChatRoom(
    val id: Int = 0,
    val title: String,
    val owner: ChatUser,
    val users: List<ChatUser> = emptyList(),
    val unReadCount: Int = 0,
    val lastReadMessageId: Int = 0,
    val isLocked: Boolean = false,
    val createdAt: Long
)


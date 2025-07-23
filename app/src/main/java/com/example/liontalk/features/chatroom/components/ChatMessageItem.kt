package com.example.liontalk.features.chatroom.components

import androidx.compose.runtime.Composable
import com.example.liontalk.data.local.entity.ChatMessageEntity
import com.example.liontalk.model.ChatMessage

@Composable
fun ChatMessageItem (message: ChatMessage, isMe: Boolean) {
    when {
        message.type == "system" -> SystemMessageItem(message.content)
        isMe -> MyMessageItem(message)
        else -> OtherMessageItem(message)
    }
}
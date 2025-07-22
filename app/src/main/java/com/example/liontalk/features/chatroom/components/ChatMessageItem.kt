package com.example.liontalk.features.chatroom.components

import androidx.compose.runtime.Composable
import com.example.liontalk.data.local.entity.ChatMessageEntity

@Composable
fun ChatMessageItem (message: ChatMessageEntity, isMe: Boolean) {
    when {
        isMe -> MyMessageItem(message)
        else -> OtherMessageItem(message)
    }
}
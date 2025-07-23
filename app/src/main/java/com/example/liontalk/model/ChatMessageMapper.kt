package com.example.liontalk.model

import com.example.liontalk.data.local.entity.ChatMessageEntity
import com.example.liontalk.data.remote.dto.ChatMessageDto

object ChatMessageMapper {
    fun ChatMessageDto.toEntity() = ChatMessageEntity(id, roomId, sender, content, createdAt)
    fun ChatMessageEntity.toDto() = ChatMessageDto(id, roomId, sender, content, createdAt)
    fun ChatMessageDto.toModel() = ChatMessage(id, roomId, sender, content, "text", createdAt)
    fun ChatMessageEntity.toModel() = ChatMessage(id, roomId, sender, content, "text", createdAt)
    fun ChatMessage.toEntity() = ChatMessageEntity(id, roomId, sender, content, createdAt)
    fun ChatMessage.toDto() = ChatMessageDto(id, roomId, sender, content, createdAt)
}
package com.example.liontalk.model

import com.example.liontalk.data.local.entity.ChatMessageEntity
import com.example.liontalk.data.remote.dto.ChatMessageDto

object ChatMessageMapper {
    fun ChatMessageDto.toEntity() = ChatMessageEntity(id, roomId, sender, content, createdAt)
    fun ChatMessageEntity.toDto() = ChatMessageDto(id, roomId, sender, content, createdAt)
}
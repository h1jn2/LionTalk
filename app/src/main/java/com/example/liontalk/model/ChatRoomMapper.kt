package com.example.liontalk.model

import com.example.liontalk.data.local.entity.ChatRoomEntity
import com.example.liontalk.data.remote.dto.ChatRoomDto

object ChatRoomMapper {
    fun ChatRoomDto.toEntity() = ChatRoomEntity(
        id, title, owner, users, 0, 0, isLocked, createdAt
    )

    fun ChatRoomDto.toModel() = ChatRoom(
        id, title, owner, users, 0, 0, isLocked, createdAt
    )

    fun ChatRoomEntity.toDto() = ChatRoomDto (
        id, title, owner, users, isLocked, createdAt
    )

    fun ChatRoomEntity.toModel() = ChatRoom (
        id, title, owner, users , unReadCount, lastReadMessageId, isLocked, createdAt
    )
}
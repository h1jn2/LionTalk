package com.example.liontalk.features.chatroomlist

import com.example.liontalk.data.local.entity.ChatRoomEntity

data class ChatRoomListState(
    val isLoading: Boolean = false,
    val chatRooms : List<ChatRoomEntity> = emptyList(),
    val error : String? = null
) {
}
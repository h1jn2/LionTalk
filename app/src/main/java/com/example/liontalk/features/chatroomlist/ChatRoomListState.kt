package com.example.liontalk.features.chatroomlist

import com.example.liontalk.data.local.entity.ChatRoomEntity
import com.example.liontalk.model.ChatRoom

enum class ChatRoomTab {
    JOINED, NOT_JOINED
}

data class ChatRoomListState(
    val isLoading: Boolean = false,
//    val chatRooms : List<ChatRoomEntity> = emptyList(),
    val chatRooms: List<ChatRoom> = emptyList(),
    val joinedRooms: List<ChatRoom> = emptyList(),
    val notJoinedRooms: List<ChatRoom> = emptyList(),
    val currentTab: ChatRoomTab = ChatRoomTab.JOINED,
    val error: String? = null
)
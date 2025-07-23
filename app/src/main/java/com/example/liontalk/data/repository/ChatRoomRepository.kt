package com.example.liontalk.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.liontalk.data.local.datasource.ChatRoomLocalDataSource
import com.example.liontalk.data.local.entity.ChatRoomEntity
import com.example.liontalk.data.remote.datasource.ChatRoomRemoteDataSource
import com.example.liontalk.data.remote.dto.ChatRoomDto
import com.example.liontalk.data.remote.dto.addUserIfNotExists
import com.example.liontalk.model.ChatRoom
import com.example.liontalk.model.ChatRoomMapper.toEntity
import com.example.liontalk.model.ChatRoomMapper.toModel
import com.example.liontalk.model.ChatUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRoomRepository(context: Context) {
    private val remote = ChatRoomRemoteDataSource()
    private val local = ChatRoomLocalDataSource(context)

    fun getChatRoomEntities(): LiveData<List<ChatRoomEntity>> {
        return local.getChatRooms()
    }

    fun getChatRoomsFlow(): Flow<List<ChatRoom>> {
        return local.getChatRoomsFlow().map { it.mapNotNull { entity -> entity.toModel() } }
    }

    suspend fun createChatRoom(chatRoom: ChatRoomDto) {
        val chatRoomDto = remote.createRoom(chatRoom)
        if (chatRoomDto != null) {
            local.insert(chatRoomDto.toEntity())
        }
    }

    suspend fun deleteChatRoomToRemote(roomId: Int) {
        remote.deleteRoom(roomId)
    }

    suspend fun syncFromServer() {
        try {
            Log.d("Sync", "서버에서 채팅방 목록 가져오는 중...")
            val remoteRooms = remote.fetchRooms()
            Log.d("Sync", "${remoteRooms.size} 개의 채팅방")
            val entities = remoteRooms.map { it.toEntity() }
            Log.d("Sync", "${entities.size} 개의 Entity 변환")

            local.clear()

            Log.d("Sync", "로컬 DB에 채팅방 데이터 저장 중...")
            local.insertAll(entities)
            Log.d("Sync", "로컬 DB 저장 완료")

            val dbCount = local.getCount()
            Log.d("Sync", "로컬 DB 저장 완료: $dbCount")

        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun enterRoom(user: ChatUser, roomId: Int): ChatRoom {
        val remoteRoom = remote.fetchRoom(roomId)
        val requestDto = remoteRoom.addUserIfNotExists(user)
        val updatedRoom = remote.updateRoom(requestDto)

        if (updatedRoom != null) {
            local.updateUsers(roomId, updatedRoom.users)
        }
        return updatedRoom?.toModel() ?: throw Exception("서버 입장 처리 실패")
    }

    suspend fun updateLastReadMessageId(roomId: Int, lastReadMessageId: Int) {
        local.updateLastReadMessageId(roomId, lastReadMessageId)
    }

    suspend fun updateUnReadCount(roomId: Int, unReadCount: Int) {
        local.updateUnReadCount(roomId, unReadCount)
    }

    suspend fun updateLockStatus(roomId: Int, isLocked: Boolean) {
        try {
            val remoteRoom = remote.fetchRoom(roomId)
            val updated = remoteRoom.copy(isLocked = isLocked)
            val result = remote.updateRoom(updated) ?: throw Exception("방 잠금($isLocked) 실패")

            result?.let {
                local.updateLockStatus(roomId, isLocked)
            }
        } catch (e: Exception) {
            Log.e("ROOM", "채팅방 상태 변경 중 오류 발생: ${e.message}")
        }
    }

    suspend fun getChatRoom(roomId: Int): ChatRoom {
        return local.getChatRoom(roomId).toModel()
    }
}

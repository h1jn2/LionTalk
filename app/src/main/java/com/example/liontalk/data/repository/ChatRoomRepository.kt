package com.example.liontalk.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.liontalk.data.local.datasource.ChatRoomLocalDataSource
import com.example.liontalk.data.local.entity.ChatRoomEntity
import com.example.liontalk.data.remote.datasource.ChatRoomRemoteDataSource
import com.example.liontalk.data.remote.dto.ChatRoomDto
import com.example.liontalk.model.ChatRoomMapper.toEntity

class ChatRoomRepository(context: Context) {
    private val remote = ChatRoomRemoteDataSource()
    private val local = ChatRoomLocalDataSource(context)

    fun getChatRoomEntities(): LiveData<List<ChatRoomEntity>> {
        return local.getChatRooms()
    }

    suspend fun createChatRoom(chatRoom: ChatRoomDto) {
        val chatRoomDto = remote.createRoom(chatRoom)
        if(chatRoomDto != null) {
            local.insert(chatRoomDto.toEntity())
        }
    }

    suspend fun deleteChatRoomToRemote(roomId: Int) {
        remote.deleteRoom(roomId)
    }

    suspend fun syncFromService() {
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
}

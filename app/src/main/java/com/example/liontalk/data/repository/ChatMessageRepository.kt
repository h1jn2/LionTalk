package com.example.liontalk.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.liontalk.data.local.datasource.ChatMessageLocalDataSource
import com.example.liontalk.data.local.entity.ChatMessageEntity
import com.example.liontalk.data.remote.datasource.ChatMessageRemoteDataSource
import com.example.liontalk.data.remote.dto.ChatMessageDto
import com.example.liontalk.model.ChatMessageMapper.toEntity

class ChatMessageRepository(context: Context) {
    private val remote = ChatMessageRemoteDataSource()
    private val local = ChatMessageLocalDataSource(context)

    suspend fun clearLocalDB() {
        local.clear()
    }

    fun getMessageForRoom(roomId: Int): LiveData<List<ChatMessageEntity>> {
        return local.getMessageForRoom(roomId)
    }

    suspend fun sendMessage(message: ChatMessageDto): ChatMessageDto? {
        try {
            val result = remote.sendMessage(message)
            result?.let {
                local.insert(it.toEntity())
                return it
            }
        } catch (e: Exception) {
            Log.e("ChatMessageRepository", "${e.message}")
        }
        return null
    }

    suspend fun receiveMessage(message: ChatMessageDto) {
        local.insert(message.toEntity())
    }
}
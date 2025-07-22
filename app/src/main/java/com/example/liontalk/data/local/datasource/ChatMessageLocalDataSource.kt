package com.example.liontalk.data.local.datasource

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Query
import com.example.liontalk.data.local.AppDatabase
import com.example.liontalk.data.local.entity.ChatMessageEntity

class ChatMessageLocalDataSource(context: Context) {
    private val dao = AppDatabase.create(context).chatMessageDao()

    suspend fun clear() {
        dao.clear()
    }

    suspend fun insert(message: ChatMessageEntity) {
        dao.insert(message)
    }

    fun getMessageForRoom(roomId: Int): LiveData<List<ChatMessageEntity>> {
        return dao.getMessageForRoom(roomId)
    }

}
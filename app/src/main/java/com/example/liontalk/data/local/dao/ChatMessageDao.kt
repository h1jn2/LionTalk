package com.example.liontalk.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.liontalk.data.local.entity.ChatMessageEntity

@Dao
interface ChatMessageDao {
    @Insert
    suspend fun insert(messageDao: ChatMessageEntity)

    @Query("SELECT * FROM chat_message WHERE roomId = :roomId ORDER BY id DESC")
    fun getMessageForRoom(roomId: Int): LiveData<List<ChatMessageEntity>>

}
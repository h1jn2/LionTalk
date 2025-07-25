package com.example.liontalk.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.liontalk.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(messageDao: ChatMessageEntity)

    @Query("SELECT * FROM chat_message WHERE roomId = :roomId ORDER BY id ASC")
    fun getMessageForRoom(roomId: Int): LiveData<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_message WHERE roomId = :roomId ORDER BY id ASC")
    fun getMessageForRoomFlow(roomId: Int): Flow<List<ChatMessageEntity>>

    @Query("DELETE FROM chat_message")
    suspend fun clear()

    @Query("SELECT * FROM chat_message WHERE roomId =:roomId")
    suspend fun getMessages(roomId: Int) : List<ChatMessageEntity>

    @Query("SELECT * FROM chat_message WHERE roomId = :roomId ORDER BY id DESC")
    suspend fun getLatestMessage(roomId: Int): ChatMessageEntity?
}
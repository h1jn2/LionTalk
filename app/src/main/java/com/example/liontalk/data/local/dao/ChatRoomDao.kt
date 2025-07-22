package com.example.liontalk.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.liontalk.data.local.entity.ChatRoomEntity

@Dao
interface ChatRoomDao {
    // 채팅방 생성 : ID 중복인 경우 대체
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chatroom: ChatRoomEntity)

    @Delete
    suspend fun delete(chatroom: ChatRoomEntity)

    // 전체 채팅방 목록 가져오기
    @Query("SELECT * FROM chat_room ORDER BY id DESC")
    fun getChatRooms() : LiveData<List<ChatRoomEntity>>

    // id 에 해당하는 채팅방 데이터 가져오기
    @Query("SELECT * FROM chat_room WHERE id=:id")
    fun getChatRoom(id : Int) : ChatRoomEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(chatRooms: List<ChatRoomEntity>)

    @Query("SELECT COUNT(*) FROM chat_room")
    fun getCount(): Int

    @Query("DELETE FROM chat_room")
    suspend fun clear()
}
package com.example.liontalk.features.chatroomlist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liontalk.data.local.AppDatabase
import com.example.liontalk.data.local.entity.ChatRoomEntity
import com.example.liontalk.data.repository.ChatRoomRepository
import com.example.liontalk.data.repository.UserPreferenceRepository
import com.example.liontalk.model.ChatRoomMapper.toDto
import com.example.liontalk.model.ChatUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomListViewModel(application: Application) : ViewModel() {
    private val _state = MutableLiveData(ChatRoomListState())
    val state: LiveData<ChatRoomListState> = _state

    private val chatRoomRepository = ChatRoomRepository(application.applicationContext)

    private val userPreferenceRepository = UserPreferenceRepository.getInstance()
    val me: ChatUser get() = userPreferenceRepository.requireMe()

    init {
        loadChatRooms()
    }

    private fun loadChatRooms() {
        viewModelScope.launch {
            _state.value = _state.value?.copy(isLoading = true)
            try {
                withContext(Dispatchers.IO) {
                    chatRoomRepository.syncFromService()
                }
                withContext(Dispatchers.Main) {
                    chatRoomRepository.getChatRoomEntities().observeForever { rooms ->
                        _state.postValue(
                            ChatRoomListState(
                                isLoading = false,
                                chatRooms = rooms
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value?.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun createChatRoom(title: String) {
        viewModelScope.launch {
            try {
                val room = ChatRoomEntity(
                    title = title,
                    owner = me,
                    users = emptyList(),
                    createdAt = System.currentTimeMillis()
                )
                chatRoomRepository.createChatRoom(room.toDto())
            } catch (e: Exception) {
                _state.value = _state.value?.copy(isLoading = false, error = e.message)
            }
        }
    }
}
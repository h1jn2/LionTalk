package com.example.liontalk.features.chatroomlist

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liontalk.data.local.AppDatabase
import com.example.liontalk.data.local.entity.ChatRoomEntity
import com.example.liontalk.data.remote.dto.ChatMessageDto
import com.example.liontalk.data.remote.mqtt.MqttClient
import com.example.liontalk.data.repository.ChatMessageRepository
import com.example.liontalk.data.repository.ChatRoomRepository
import com.example.liontalk.data.repository.UserPreferenceRepository
import com.example.liontalk.model.ChatRoomMapper.toDto
import com.example.liontalk.model.ChatUser
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomListViewModel(application: Application) : ViewModel() {
    //    private val _state = MutableLiveData(ChatRoomListState())
    private val _state = MutableStateFlow(ChatRoomListState())
    val state: StateFlow<ChatRoomListState> = _state.asStateFlow()

    private val chatRoomRepository = ChatRoomRepository(application.applicationContext)
    private val chatMessageRepository = ChatMessageRepository(application.applicationContext)
    private val userPreferenceRepository = UserPreferenceRepository.getInstance()
    val me: ChatUser get() = userPreferenceRepository.requireMe()

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                withContext(Dispatchers.IO) {
                    chatRoomRepository.syncFromServer()
                }
                withContext(Dispatchers.IO) {
                    subscribeToMqttTopics()
                }

                chatRoomRepository.getChatRoomsFlow().collect { rooms ->

                    val joined = rooms.filter {it.users.any {p-> p.name == me.name}}
                    val notJoined = rooms.filter { it.users.none {p-> p.name == me.name} }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        chatRooms = rooms,
                        joinedRooms = joined,
                        notJoinedRooms = notJoined
                    )
                }

            } catch (e : Exception ) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
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
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun switchTab(tab: ChatRoomTab) {
        _state.value = _state.value.copy(currentTab = tab)
    }

    // ---------MQTT---------
    private val topics = listOf("message")
    private fun subscribeToMqttTopics() {
        MqttClient.connect()
        MqttClient.setOnMessageReceived { topic, message -> {} }
        topics.forEach { MqttClient.subscribe("liontalk/rooms/+/$it") }
    }

    private fun handleReceivedMessage(topic: String, message: String) {
        when {
            topic.endsWith("/message") -> onReceivedMessage(message)
        }
    }

    private fun onReceivedMessage(message: String) {
        try {
            val dto = Gson().fromJson(message, ChatMessageDto::class.java)
            viewModelScope.launch {
                val room = chatRoomRepository.getChatRoom(dto.roomId)
                val unReadCount = chatMessageRepository.fetchUnReadCountFromServer(
                    roomId = dto.roomId,
                    room.lastReadMessageId
                )
                chatRoomRepository.updateUnReadCount(roomId = dto.roomId, unReadCount)
            }
        } catch (e: Exception) {

        }
    }
}
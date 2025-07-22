package com.example.liontalk.features.chatroom

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liontalk.data.local.AppDatabase
import com.example.liontalk.data.local.entity.ChatMessageEntity
import com.example.liontalk.data.remote.dto.ChatMessageDto
import com.example.liontalk.data.remote.dto.TypingMessageDto
import com.example.liontalk.data.remote.mqtt.MqttClient
import com.example.liontalk.data.repository.ChatMessageRepository
import com.example.liontalk.data.repository.UserPreferenceRepository
import com.example.liontalk.features.chatroom.components.ChatRoomEvent
import com.example.liontalk.model.ChatMessageMapper.toEntity
import com.example.liontalk.model.ChatUser
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomViewModel(application: Application, private val roomId: Int) : ViewModel() {
    private val chatMessageRepository = ChatMessageRepository(application.applicationContext)
    val messages : LiveData<List<ChatMessageEntity>> = chatMessageRepository.getMessageForRoom(roomId)

    private val userPreferenceRepository = UserPreferenceRepository.getInstance()

    val me : ChatUser get() = userPreferenceRepository.requireMe()

    private val _event = MutableSharedFlow<ChatRoomEvent>()
    val event = _event.asSharedFlow()
    private var typing = false
    private var typingStopJob: Job? = null

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                MqttClient.connect()
            }
            withContext(Dispatchers.IO) {
                subscribeToMqttTopics()
            }
        }
    }

    fun sendMessage (sender: String, content: String) {
        viewModelScope.launch (Dispatchers.IO){
            val dto = ChatMessageDto (
                roomId = roomId,
                sender = me,
                content = content,
                createdAt = System.currentTimeMillis()
            )
            val responseDto = chatMessageRepository.sendMessage(dto)

            if (responseDto != null) {
                val json = Gson().toJson(responseDto)
                MqttClient.publish("liontalk/rooms/$roomId/message", json)
            }
//            chatMessageDao.insert(messageEntity)
        }
    }

    private val topics = listOf("message", "typing")
    private fun subscribeToMqttTopics() {
        MqttClient.setOnMessageRecieved { topic, message ->
            handleIncomingMqttMessage(topic, message)
        }
        topics.forEach {
            MqttClient.subscribe("liontalk/rooms/$roomId/$it")
        }
    }

    private fun unSubscribeFromMqttTopics() {
        topics.forEach {
            MqttClient.unSubscribe("liontalk/rooms/$roomId/$it")
        }
    }

    private fun handleIncomingMqttMessage(topic: String, message: String) {
        when {
            topic.endsWith("/message") -> onReceivedMessage(message)
            topic.endsWith("/typing") -> onReceivedTyping(message)

        }
    }

    private fun onReceivedMessage(message: String) {
        val dto = Gson().fromJson(message, ChatMessageDto::class.java)

        viewModelScope.launch {
//            chatMessageDao.insert(dto.toEntity())
            chatMessageRepository.receiveMessage(dto)
        }
    }

    private fun onReceivedTyping(message: String) {
        val dto = Gson().fromJson(message, TypingMessageDto::class.java)
        if (dto.sender != me.name) {
            viewModelScope.launch {
                val event = if(dto.typing) ChatRoomEvent.TypingStarted(dto.sender)
                else ChatRoomEvent.TypingStopped

                _event.emit(event)
            }
        }
    }

    fun onTypingChanged(text: String) {
        if(text.isNotBlank() && !typing) {
            publishTypingStatus(true)
        }
        typingStopJob?.cancel()
        typingStopJob = viewModelScope.launch {
            delay(2000)
            typing = false
            publishTypingStatus(false)
        }
    }

    private fun publishTypingStatus(isTyping: Boolean) {
        val json = Gson().toJson(TypingMessageDto(sender = me.name, isTyping))
        MqttClient.publish("liontalk/rooms/$roomId/typing", json)
    }
}
package com.example.liontalk.features.chatroom

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liontalk.data.remote.dto.ChatMessageDto
import com.example.liontalk.data.remote.dto.PresenceMessageDto
import com.example.liontalk.data.remote.dto.TypingMessageDto
import com.example.liontalk.data.remote.mqtt.MqttClient
import com.example.liontalk.data.repository.ChatMessageRepository
import com.example.liontalk.data.repository.ChatRoomRepository
import com.example.liontalk.data.repository.UserPreferenceRepository
import com.example.liontalk.features.chatroom.components.ChatRoomEvent
import com.example.liontalk.model.ChatMessage
import com.example.liontalk.model.ChatUser
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomViewModel(application: Application, private val roomId: Int) : ViewModel() {
    private val chatMessageRepository = ChatMessageRepository(application.applicationContext)
    private val chatRoomRepository = ChatRoomRepository(application.applicationContext)

    private val _systemMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = combine(
        chatMessageRepository.getMessageForRoomFlow(roomId),
        _systemMessages
    ) { dbMessages, systemMessages ->
        (dbMessages + systemMessages).sortedBy { it.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    private val userPreferenceRepository = UserPreferenceRepository.getInstance()

    val me: ChatUser get() = userPreferenceRepository.requireMe()

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

            publishEnterStatus()
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dto = ChatMessageDto(
                roomId = roomId,
                sender = me,
                content = content,
                createdAt = System.currentTimeMillis()
            )
            val responseDto = chatMessageRepository.sendMessage(dto)

            if (responseDto != null) {
                val json = Gson().toJson(responseDto)
                MqttClient.publish("liontalk/rooms/$roomId/message", json)

                publishTypingStatus(false)

                _event.emit(ChatRoomEvent.ClearInput)
            }
//            chatMessageDao.insert(messageEntity)
        }
    }

    private val topics = listOf("message", "typing", "enter", "leave")
    private fun subscribeToMqttTopics() {
        MqttClient.setOnMessageReceived { topic, message ->
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
            topic.endsWith("/enter") -> onReceivedEnter(message)
            topic.endsWith("/leave") -> onReceivedLeave(message)
        }
    }

    private fun onReceivedEnter(message: String) {
        val dto = Gson().fromJson(message, PresenceMessageDto::class.java)
        if (dto.sender != me.name) {
            viewModelScope.launch {
                _event.emit(ChatRoomEvent.ChatRoomEnter(dto.sender))
                postSystemMessage("${dto.sender} 님이 입장하셨습니다.")
                _event.emit(ChatRoomEvent.ScrollToBottom)
            }
        }
    }

    private fun onReceivedLeave(message: String) {
        val dto = Gson().fromJson(message, PresenceMessageDto::class.java)
        if (dto.sender != me.name) {
            viewModelScope.launch {
                _event.emit(ChatRoomEvent.ChatRoomLeave(dto.sender))
                postSystemMessage("${dto.sender} 님이 퇴장하셨습니다.")
                _event.emit(ChatRoomEvent.ScrollToBottom)
            }
        }
    }

    private fun onReceivedMessage(message: String) {
        val dto = Gson().fromJson(message, ChatMessageDto::class.java)

        viewModelScope.launch {
            chatMessageRepository.receiveMessage(dto)
            _event.emit(ChatRoomEvent.ScrollToBottom)
            chatRoomRepository.updateLastReadMessageId(dto.roomId, dto.id)
        }
    }

    private fun onReceivedTyping(message: String) {
        val dto = Gson().fromJson(message, TypingMessageDto::class.java)
        if (dto.sender != me.name) {
            viewModelScope.launch {
                val event = if (dto.typing) ChatRoomEvent.TypingStarted(dto.sender)
                else ChatRoomEvent.TypingStopped

                _event.emit(event)
            }
        }
    }

    fun onTypingChanged(text: String) {
        if (text.isNotBlank() && !typing) {
            publishTypingStatus(true)
        }
        typingStopJob?.cancel()
        typingStopJob = viewModelScope.launch {
            delay(2000)
            typing = false
            publishTypingStatus(false)
        }
    }

    private fun postSystemMessage(content: String) {
        val systemMessage = ChatMessage(
            id = -1,
            roomId = roomId,
            sender = me,
            content = content,
            type = "system",
            createdAt = System.currentTimeMillis()
        )
        _systemMessages.value = _systemMessages.value + systemMessage
    }

    fun leaveRoom(onComplete: () -> Unit) {
        viewModelScope.launch {
            publishLeaveStatus()
            onComplete()
        }
    }

    fun stopTyping() {
        typing = false
        publishTypingStatus(false)
        typingStopJob?.cancel()
    }

    private fun publishTypingStatus(isTyping: Boolean) {
        val json = Gson().toJson(TypingMessageDto(sender = me.name, isTyping))
        MqttClient.publish("liontalk/rooms/$roomId/typing", json)
    }

    private fun publishEnterStatus() {
        val json = Gson().toJson(PresenceMessageDto(me.name))
        MqttClient.publish("liontalk/rooms/$roomId/enter", json)

        viewModelScope.launch {
            chatRoomRepository.enterRoom(me, roomId)
            val latestMessage = chatMessageRepository.getLatestMessage(roomId)
            latestMessage?.let {
                chatRoomRepository.updateLastReadMessageId(roomId, it.id)
            }
        }
    }

    private fun publishLeaveStatus() {
        val json = Gson().toJson(PresenceMessageDto(me.name))
        MqttClient.publish("liontalk/rooms/$roomId/leave", json)
    }
}
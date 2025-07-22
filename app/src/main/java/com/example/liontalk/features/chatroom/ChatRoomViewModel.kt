package com.example.liontalk.features.chatroom

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liontalk.data.local.AppDatabase
import com.example.liontalk.data.local.entity.ChatMessageEntity
import com.example.liontalk.data.remote.dto.ChatMessageDto
import com.example.liontalk.data.remote.mqtt.MqttClient
import com.example.liontalk.data.repository.ChatMessageRepository
import com.example.liontalk.model.ChatMessageMapper.toEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomViewModel(application: Application, private val roomId: Int) : ViewModel() {
//    private val chatMessageDao = AppDatabase.create(application).chatMessageDao()
//    val messages : LiveData<List<ChatMessageEntity>> = chatMessageDao.getMessageForRoom(roomId)
    private val chatMessageRepository = ChatMessageRepository(application)
    val messages: LiveData<List<ChatMessageEntity>> = chatMessageRepository.getMessageForRoom(roomId)

    init {
        viewModelScope.launch {
            chatMessageRepository.clearLocalDB()
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
                sender = sender,
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

    private val topics = listOf("message")
    private fun subscribeToMqttTopics() {
        MqttClient.connect()
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
        }
    }

    private fun onReceivedMessage(message: String) {
        val dto = Gson().fromJson(message, ChatMessageDto::class.java)

        viewModelScope.launch {
//            chatMessageDao.insert(dto.toEntity())
            chatMessageRepository.receiveMessage(dto)
        }
    }
}
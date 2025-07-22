package com.example.liontalk.features.chatroom

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liontalk.data.local.AppDatabase
import com.example.liontalk.data.local.entity.ChatMessageEntity
import com.example.liontalk.data.remote.mqtt.MqttClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatRoomViewModel(application: Application, private val roomId: Int) : ViewModel() {
    private val chatMessageDao = AppDatabase.create(application).chatMessageDao()
    val messages : LiveData<List<ChatMessageEntity>> = chatMessageDao.getMessageForRoom(roomId)

    fun sendMessage (sender: String, content: String) {
        viewModelScope.launch (Dispatchers.IO){
            val messageEntity = ChatMessageEntity (
                roomId = roomId,
                sender = sender,
                content = content,
                createdAt = System.currentTimeMillis()
            )
            chatMessageDao.insert(messageEntity)
        }
    }

    private val topics = listOf("message")
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
        }
    }

    private fun onReceivedMessage(message: String) {
        val dto = Gson
    }
}
package com.example.liontalk.data.remote.datasource

import com.example.liontalk.data.remote.dto.ChatMessageDto
import com.example.liontalk.data.remote.service.ChatMessageApiService
import com.example.liontalk.network.RetrofitProvider

class ChatMessageRemoteDataSource {
    private val api: ChatMessageApiService =
        RetrofitProvider.create(ChatMessageApiService::class.java)

    suspend fun sendMessage(message: ChatMessageDto): ChatMessageDto? {
        val response = api.sendMessage(message)
        if (!response.isSuccessful) {
            throw Exception("서버 메시지 전송 실패: [${response.code()}] ${response.message()}")
        }
        return response.body()
    }
}
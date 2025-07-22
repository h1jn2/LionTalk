package com.example.liontalk.data.remote.mqtt

import android.util.Log
import java.util.UUID
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import java.nio.charset.StandardCharsets

object MqttClient {
    private val mqttClient: Mqtt3BlockingClient = MqttClient.builder()
        .useMqttVersion3()
        .identifier("LionTalk ${UUID.randomUUID()}")
        .serverHost("broker.hivemq.com")
        .serverPort(1883)
        .buildBlocking()

    private var isConnected = false
    private val subscribedTopics = mutableSetOf<String>()
    private var messageCallback: ((topic: String, message: String) -> Unit)? = null

    fun connect(onConnected: (() -> Unit)? = null, onError: ((Throwable) -> Unit)? = null) {
        if (isConnected) {
            onConnected?.invoke()
            return
        }
        try {
            mqttClient.connect()
            isConnected = true
            Log.e("MQTT", "MQTT 연결 성공")
            onConnected?.invoke()
        } catch (e: Exception) {
            Log.e("MQTT", "MQTT 연결 실패: ${e.message}")
            onError?.invoke(e)
        }
    }

    fun setOnMessageRecieved(callback: (topic: String, message: String) -> Unit) {
        messageCallback = callback
    }

    fun subscribe(topic: String) {
        if (!isConnected) {
            Log.e("MQTT", "MQTT 연결 안 됨 - subscribe 실패")
        }
        if (subscribedTopics.contains(topic)) {
            Log.d("MQTT", "이미 구독중: $topic")
            return
        }
        mqttClient.toAsync().subscribeWith()
            .topicFilter(topic)
            .callback { publish ->
                val receivedTopic = publish.topic.toString()
                val payloadBuffer = publish.payload.orElse(null)

                val message = payloadBuffer?.let { buffer ->
                    val readOnlyBuffer = buffer.asReadOnlyBuffer()
                    val bytes = ByteArray(readOnlyBuffer.remaining())
                    readOnlyBuffer.get(bytes)
                    String(bytes, StandardCharsets.UTF_8)
                } ?: ""
                Log.d("MQTT", "수신: [$receivedTopic] $message")

                messageCallback?.invoke(receivedTopic, message)
            }
            .send()
        subscribedTopics.add(topic)
        Log.d("MQTT", "구독 시작: $topic")
    }

    fun unSubscribe(topic: String) {
        if (subscribedTopics.contains(topic)) {
            mqttClient.toAsync().unsubscribeWith()
                .topicFilter(topic)
                .send()

            subscribedTopics.remove(topic)
            Log.d("MQTT", "구독 해제: $topic")
        }
    }

    fun unSubscribeAll() {
        for (topic in subscribedTopics.toList()) {
            unSubscribe(topic)
        }
    }

    fun publish(topic: String, message: String) {
        if (!isConnected) {
            Log.e("MQTT", "MQTT 연결 안 됨 - publish 실패")
        }

        mqttClient.publishWith()
            .topic(topic)
            .payload(message.toByteArray(StandardCharsets.UTF_8))
            .send()

        Log.d("MQTT", "published: [$topic] $message")
    }

    fun disconnect() {
        if (!isConnected) {
            mqttClient.disconnect()
            isConnected = false
            subscribedTopics.clear()
            Log.d("MQTT", "MQTT 연결 해제")
        }
    }
}
package com.drivertest.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeepSeekRequest(
    @SerializedName("model")
    val model: String = "deepseek-chat",

    @SerializedName("messages")
    val messages: List<ChatMessage>,

    @SerializedName("temperature")
    val temperature: Double = 0.3,

    @SerializedName("max_tokens")
    val maxTokens: Int = 2048
)

data class ChatMessage(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String
)

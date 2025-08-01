package com.example.botactivity

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isFinal: Boolean = false // Added to handle streaming text
)
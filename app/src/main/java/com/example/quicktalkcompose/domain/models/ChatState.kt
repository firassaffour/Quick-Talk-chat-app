package com.example.quicktalkcompose.domain.models

data class ChatState(
    val currentIsMuted : Boolean = false,
    val receiverIsTyping : Boolean = false,
    val receiverIsMuted : Boolean = false,
    val receiverIsInChat : Boolean = false,
)
package com.example.quicktalkcompose.domain.models

sealed class MessageEvent {
    data class Added(val message: Message): MessageEvent()
    data class Changed(val message: Message): MessageEvent()
    data class Deleted(val message: Message): MessageEvent()
}
package com.example.quicktalkcompose.domain.models

data class User(
    val userId : String = "",
    val name : String = "",
    val phoneNumber : String = "",
    val profileImage : String = "",
    val description : String = "",
    val lastMessage : String = "",
    val lastMessageTime :Long = 0,
    val isMuted : Boolean = false,
    val isOnline : Boolean = false,
    var unreadCount : Int = 0
)

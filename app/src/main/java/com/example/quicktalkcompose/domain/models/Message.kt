package com.example.quicktalkcompose.domain.models

data class Message(
    var messageId : String = "",
    var messageText : String = "",
    var messageTime : Long = 0,
    var messageDate : Long = 0,
    val senderId : String = "",
    val repliedTo : Message? = null,
    val image : String = "",
    var reactionCount : Int = 0,
    var isEdited : Boolean = false,
    var status : String = "",
    var storyReply : Boolean = false,
    var storyText : String = "",
    var storyImage : String = ""
)

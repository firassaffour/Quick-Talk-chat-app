package com.example.quicktalkcompose.domain.models

data class StoryItem(
    val storyId: String = "",
    val imageUrl : String = "",
    val time : String = "",
    var isSeen : Boolean = false,
    val text : String = ""
)

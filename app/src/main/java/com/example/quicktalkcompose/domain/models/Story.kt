package com.example.quicktalkcompose.domain.models

data class Story(
    val userId: String = "",
    val userProfileImage: String = "",
    val userName: String = "",
    val stories: MutableList<StoryItem> = mutableListOf(),
    var isSeen: Boolean = false,
)

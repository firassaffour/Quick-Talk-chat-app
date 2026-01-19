package com.example.quicktalkcompose.home_screen

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quicktalkcompose.domain.models.ChatState
import com.example.quicktalkcompose.domain.models.Message
import com.example.quicktalkcompose.domain.models.Story
import com.example.quicktalkcompose.domain.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel (application: Application, private val homeRepository: HomeRepository) : AndroidViewModel(application) {

    private val _usersList = MutableStateFlow<List<User>>(emptyList())
    val usersList = _usersList.asStateFlow()

    private val _storiesList = MutableStateFlow<List<Story>>(emptyList())
    val storiesList = _storiesList.asStateFlow()

    private val _myStory = MutableStateFlow<Story>(Story())
    val myStory = _myStory.asStateFlow()

    private val _shouldAnimate = MutableStateFlow<Boolean>(false)
    val shouldAnimate = _shouldAnimate.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText : StateFlow<String> = _messageText

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery : StateFlow<String> = _searchQuery

    val filteredUserList = combine(_allUsers, _searchQuery){ users, query ->
        if (query.isBlank()) users
        else users.filter { it.phoneNumber.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatState = MutableStateFlow(ChatState())
    var selectedStoryImageUri by mutableStateOf<Uri?>(null)


    fun getCurrentInfo(): User {
        return homeRepository.getCurrentInfo()
    }

    fun getUsersChat(){
        viewModelScope.launch {
            homeRepository.getUsersChat().collect{ list ->
                _usersList.value = list
            }
        }
    }

    fun uploadStory(imageUri: Uri, text: String = "") {
        viewModelScope.launch {
                val context = getApplication<Application>().applicationContext
                homeRepository.uploadStory(context, imageUri, text)
        }
    }

    fun setShouldAnimate(state : Boolean){
        _shouldAnimate.value = state
    }

    fun getUserStories(userId: String){
        viewModelScope.launch {
            homeRepository.getUserStories(userId).collect { story ->
                _myStory.value = story
            }
        }
    }

    fun getAllUsersStories(){
        viewModelScope.launch {
            homeRepository.getAllUsersStories().collect { stories ->
                _storiesList.value = stories
            }
        }
    }

    fun setStoriesSeen(userId: String, storyId : String) {
        homeRepository.setStoriesSeen(userId, storyId)
    }

    fun updateMessageText(text : String){
        _messageText.value = text
    }

    fun updateSearchQuery(query : String){
        _searchQuery.value = query
    }

    fun findContacts(){
        homeRepository.findContacts { contactList ->
            _allUsers.value = contactList
        }
    }

    fun formatTime(timeStamp : Long?): String{
        return homeRepository.formatTime(timeStamp)
    }

    fun formatDate(timeStamp : Long?): String{
        return homeRepository.formatDate(timeStamp)
    }

    fun observeChatState(currentUid: String, receiverUid: String){
        viewModelScope.launch {
            homeRepository.observeChatState(currentUid, receiverUid)
                .collect{ state ->
                    chatState.value = state
                }
        }
    }

    fun sendMessage(receiverUid: String, messageText : String, repliedTo : Message? = null, imageUri: Uri? = null, status : String = "Sent", storyReply : Boolean = false, storyText : String = "", storyImage: String = "", onFinish: () -> Unit){
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            homeRepository.sendMessage(context, receiverUid, messageText, repliedTo, imageUri, status, storyReply, storyText, storyImage)
            onFinish()
        }
    }

    fun initOneSignal(context: Context){
        homeRepository.initOneSignal(context)
    }

    fun sendMessageNotification(receiverUid: String, message : String) {
        val context = getApplication<Application>().applicationContext
        homeRepository.sendMessageNotification(context, receiverUid, message)
    }
}
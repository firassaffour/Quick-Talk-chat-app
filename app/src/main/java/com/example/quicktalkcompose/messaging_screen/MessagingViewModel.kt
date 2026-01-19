package com.example.quicktalkcompose.messaging_screen

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quicktalkcompose.domain.models.ChatState
import com.example.quicktalkcompose.domain.models.Message
import com.example.quicktalkcompose.domain.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagingViewModel (application: Application, private val messagingRepository: MessagingRepository) : AndroidViewModel(application) {

    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    val chatState = MutableStateFlow(ChatState())

    private val _messageText = MutableStateFlow("")
    val messageText : StateFlow<String> = _messageText

    fun updateMessageText(text : String){
        _messageText.value = text
    }

    fun getCurrentInfo(): User {
        return messagingRepository.getCurrentInfo()
    }

    fun sendMessage(receiverUid: String, messageText : String, repliedTo : Message? = null, imageUri: Uri? = null, status : String = "Sent", storyReply : Boolean = false, onFinish: () -> Unit){
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            messagingRepository.sendMessage(context, receiverUid, messageText, repliedTo, imageUri, status, storyReply)
            onFinish()
        }
    }

    fun formatTime(timeStamp : Long?): String{
        return messagingRepository.formatTime(timeStamp)
    }

    fun formatDate(timeStamp : Long?): String{
        return messagingRepository.formatDate(timeStamp)
    }

    fun listenForMessages(receiverUid: String){
        viewModelScope.launch{
            messagingRepository.listenForMessages(receiverUid).collect{list ->
                _chatMessages.value = list
            }
        }
    }


    fun sendMessageNotification(receiverUid: String, message : String) {
        val context = getApplication<Application>().applicationContext
        messagingRepository.sendMessageNotification(context, receiverUid, message)
    }

    fun editMessage(receiverUid: String, messageText : String, messageId : String){
        messagingRepository.editMessage(receiverUid, messageText, messageId)
    }

    fun deleteMessage(currentUid: String, receiverUid: String, messageId: String){
        messagingRepository.deleteMessage(currentUid, receiverUid, messageId)
    }

    fun generateChatId(user1 : String, user2: String) : String{
        return messagingRepository.generateChatId(user1, user2)
    }

    fun setCurrentIsInChat(currentUid: String, receiverUid: String, isDisposed: Boolean){
        messagingRepository.setCurrentIsInChat(currentUid, receiverUid, isDisposed)
    }

    fun setCurrentIsTyping(currentUid: String, receiverUid: String){
        messagingRepository.setCurrentIsTyping(_messageText.value, currentUid, receiverUid)
    }

    fun observeChatState(currentUid: String, receiverUid: String){
        viewModelScope.launch {
            messagingRepository.observeChatState(currentUid, receiverUid)
                .collect{ state ->
                    chatState.value = state
                }
        }
    }

    fun setMessagesStatusToRead(currentUid: String, receiverUid: String){
        messagingRepository.setMessagesStatusToRead(currentUid, receiverUid)
    }

    fun setReactionToMessage(currentUid: String, receiverUid: String, messageId: String, messageText: String) {
        val context = getApplication<Application>().applicationContext
        messagingRepository.setReactionToMessage(context,currentUid, receiverUid, messageId, messageText)
    }

    fun muteTheUser(currentUid: String, receiverUid: String, isMuted : Boolean){
        messagingRepository.muteTheUser(currentUid, receiverUid, isMuted)
    }
}
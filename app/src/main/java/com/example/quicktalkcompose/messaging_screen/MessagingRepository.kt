package com.example.quicktalkcompose.messaging_screen

import android.content.Context
import android.net.Uri
import com.example.quicktalkcompose.domain.models.Message
import com.example.quicktalkcompose.domain.models.MessageEvent
import com.example.quicktalkcompose.domain.models.User
import com.example.quicktalkcompose.remote_data_Source.CloudinaryDataSource
import com.example.quicktalkcompose.remote_data_Source.FirebaseDataSource
import com.example.quicktalkcompose.remote_data_Source.OneSignalDataSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MessagingRepository(private val firebaseDataSource: FirebaseDataSource, private val cloudinaryDataSource: CloudinaryDataSource, private val oneSignalDataSource: OneSignalDataSource) {

    fun getCurrentInfo(): User {
        return firebaseDataSource.getCurrentInfo()
    }

    suspend fun sendMessage(context: Context, receiverUid: String, messageText : String, repliedTo : Message? = null, imageUri: Uri? = null, status : String = "Sent", storyReply : Boolean = false){
        val imageUrl = cloudinaryDataSource.uploadMessageImageToCloudinary(context, imageUri)
        firebaseDataSource.sendMessage(receiverUid, messageText, repliedTo, imageUrl, status, storyReply)
    }

    fun formatTime(timeStamp : Long?): String{
        return firebaseDataSource.formatTime(timeStamp)
    }

    fun formatDate(timeStamp : Long?): String{
        return firebaseDataSource.formatDate(timeStamp)
    }

    fun listenForMessages(receiverUid: String): Flow<List<Message>>{
        val currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        val messages = mutableListOf<Message>()
        return firebaseDataSource.listenForMessages(currentUid, receiverUid).map { event ->
            when(event){
                is MessageEvent.Added -> {messages.add(event.message)}
                is MessageEvent.Changed -> {
                    val index = messages.indexOfFirst { it.messageId == event.message.messageId }
                    if (index != -1) messages[index] = event.message}
                is MessageEvent.Deleted -> {messages.removeAll { it.messageId == event.message.messageId }}
            }
            messages.toList()
        }
    }

    fun editMessage(receiverUid: String, messageText : String, messageId : String){
        firebaseDataSource.editMessage(receiverUid, messageText, messageId)
    }

    fun deleteMessage(currentUid: String, receiverUid: String, messageId: String){
        firebaseDataSource.deleteMessage(currentUid, receiverUid, messageId)
    }

    fun sendMessageNotification(context: Context, receiverUid: String, message : String) {
        val user = firebaseDataSource.getCurrentInfo()
        oneSignalDataSource.sendMessageNotification(context, receiverUid, user.name, user.profileImage, message)
    }

    fun generateChatId(user1 : String, user2: String) : String{
        return firebaseDataSource.generateChatId(user1, user2)
    }

    fun setCurrentIsInChat(currentUid: String, receiverUid: String, isDisposed: Boolean){
        firebaseDataSource.setCurrentIsInChat(currentUid, receiverUid, isDisposed)
    }

    fun setCurrentIsTyping(messageText: String, currentUid: String, receiverUid: String){
        firebaseDataSource.setCurrentIsTyping(messageText, currentUid, receiverUid)
    }

    fun observeChatState(currentUid: String, receiverUid: String) = firebaseDataSource.observeChatState(currentUid, receiverUid)

    fun setMessagesStatusToRead(currentUid: String, receiverUid: String){
        firebaseDataSource.setMessagesStatusToRead(currentUid, receiverUid)
    }

    fun setReactionToMessage(context: Context, currentUid: String, receiverUid: String, messageId: String, messageText: String) {
        val user = firebaseDataSource.getCurrentInfo()
        firebaseDataSource.setReactionToMessage(currentUid, receiverUid, messageId){
            oneSignalDataSource.sendMessageNotification(context, receiverUid, user.name, user.profileImage, "reacted ❤ to $messageText")
        }
    }

    fun muteTheUser(currentUid: String, receiverUid: String, isMuted : Boolean){
        firebaseDataSource.muteTheUser(currentUid, receiverUid, isMuted)
    }
}
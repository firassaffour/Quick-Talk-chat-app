package com.example.quicktalkcompose.home_screen

import android.content.Context
import android.net.Uri
import com.example.quicktalkcompose.domain.models.Message
import com.example.quicktalkcompose.domain.models.Story
import com.example.quicktalkcompose.domain.models.User
import com.example.quicktalkcompose.remote_data_Source.CloudinaryDataSource
import com.example.quicktalkcompose.remote_data_Source.FirebaseDataSource
import com.example.quicktalkcompose.remote_data_Source.OneSignalDataSource
import kotlinx.coroutines.flow.Flow

class HomeRepository(private val firebaseDataSource: FirebaseDataSource, private val cloudinaryDataSource: CloudinaryDataSource, private val oneSignalDataSource: OneSignalDataSource) {

    fun getCurrentInfo(): User {
        return firebaseDataSource.getCurrentInfo()
    }

    fun getUsersChat(): Flow<List<User>> {
        return firebaseDataSource.getUsersChat()
    }

    suspend fun uploadStory(context: Context, imageUri: Uri, text: String = ""){
        val imageUrl = cloudinaryDataSource.uploadImageStoryToCloudinary(context, imageUri)
        firebaseDataSource.saveStoryImageToFirebase(imageUrl!!, text)
    }

    fun getUserStories(userId: String) : Flow<Story>{
        return firebaseDataSource.getUserStories(userId)
    }

    fun getAllUsersStories(): Flow<List<Story>>{
        return firebaseDataSource.getAllUsersStories()
    }

    fun setStoriesSeen(userId: String, storyId : String) {
        firebaseDataSource.setStoriesSeen(userId, storyId)
    }

    fun findContacts(onResult : (List<User>) -> Unit){
        firebaseDataSource.findContacts { contactsList ->
            onResult(contactsList)
        }
    }

    fun formatTime(timeStamp : Long?): String{
        return firebaseDataSource.formatTime(timeStamp)
    }

    fun formatDate(timeStamp : Long?): String{
        return firebaseDataSource.formatDate(timeStamp)
    }

    fun observeChatState(currentUid: String, receiverUid: String) = firebaseDataSource.observeChatState(currentUid, receiverUid)

    suspend fun sendMessage(context: Context, receiverUid: String, messageText : String, repliedTo : Message? = null, imageUri: Uri? = null, status : String = "Sent", storyReply : Boolean = false, storyText: String = "", storyImage: String = ""){
        val imageUrl = cloudinaryDataSource.uploadMessageImageToCloudinary(context, imageUri)
        firebaseDataSource.sendMessage(receiverUid, messageText, repliedTo, imageUrl, status, storyReply, storyText, storyImage)
    }

    fun initOneSignal(context: Context){
        oneSignalDataSource.initOneSignal(context)
    }

    fun sendMessageNotification(context: Context, receiverUid: String, message : String) {
        val user = firebaseDataSource.getCurrentInfo()
        oneSignalDataSource.sendMessageNotification(context, receiverUid, user.name, user.profileImage, message)
    }
}
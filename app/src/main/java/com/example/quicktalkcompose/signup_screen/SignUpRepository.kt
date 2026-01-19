package com.example.quicktalkcompose.signup_screen

import android.content.Context
import android.net.Uri
import com.example.quicktalkcompose.remote_data_Source.CloudinaryDataSource
import com.example.quicktalkcompose.remote_data_Source.FirebaseDataSource

class SignUpRepository(private val firebaseDataSource: FirebaseDataSource, private val cloudinaryDataSource: CloudinaryDataSource) {

    fun signup(uid: String, number : String, onResult : (Boolean) -> Unit){
        firebaseDataSource.signup(uid, number, onResult)
    }

    suspend fun saveProfileImageUrlToFirebase(context : Context, imageUri: Uri) {
        val imageUrl = cloudinaryDataSource.uploadProfileImageToCloudinary(context, imageUri)
        firebaseDataSource.saveProfileImageUrlToFirebase(imageUrl!!)
    }
}
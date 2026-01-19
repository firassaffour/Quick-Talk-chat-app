package com.example.quicktalkcompose.remote_data_Source

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

class CloudinaryDataSource {

    data class CloudinaryResponse(val secure_url : String)

    interface CloudinaryApi {
        @Multipart
        @POST("v1_1/{cloudName}/image/upload")
        suspend fun uploadImage(
            @Path("cloudName") cloudName: String,
            @Part file: MultipartBody.Part,
            @Query("upload_preset") uploadPreset: String
        ): Response<CloudinaryResponse>
    }


   suspend fun uploadProfileImageToCloudinary(context : Context, imageUri: Uri): String? {
            return try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val requestBody = inputStream?.readBytes()?.toRequestBody("image/*".toMediaTypeOrNull())

                val body = MultipartBody.Part.createFormData(
                    "file", "image.jpg", requestBody!!
                )

                val cloudName = "dkiblm397"
                val uploadPreset = "text_upload"

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.cloudinary.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(CloudinaryApi::class.java)

                val response = api.uploadImage(
                    file = body,
                    uploadPreset = uploadPreset,
                    cloudName = cloudName
                )
                    response.body()?.secure_url
            }
            catch (e: Exception){
                Log.e("TAG", "saveProfileImageUrlToFirebase: ${e.message}")
            null
            }
    }

    suspend fun uploadImageStoryToCloudinary(context : Context, imageUri: Uri): String? {
            return try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val requestBody = inputStream?.readBytes()?.toRequestBody("image/*".toMediaTypeOrNull())

                val body = MultipartBody.Part.createFormData(
                    "file", "image.jpg", requestBody!!
                )

                val cloudName = "dkiblm397"
                val uploadPreset = "text_upload"

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.cloudinary.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(CloudinaryApi::class.java)

                val response = api.uploadImage(
                    file = body,
                    uploadPreset = uploadPreset,
                    cloudName = cloudName
                )
                    response.body()?.secure_url

            }
            catch (e: Exception){
                Log.e("TAG", "saveProfileImageUrlToFirebase: ${e.message}")
            null
            }
    }

    suspend fun uploadMessageImageToCloudinary(context: Context, imageUri: Uri?): String? {
           return try {
                val inputStream = context.contentResolver.openInputStream(imageUri!!)
                val requestBody = inputStream?.readBytes()?.toRequestBody("image/*".toMediaTypeOrNull())

                val body = MultipartBody.Part.createFormData(
                    "file", "image.jpg", requestBody!!
                )

                val cloudName = "dkiblm397"
                val uploadPreset = "text_upload"

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.cloudinary.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(CloudinaryApi::class.java)

                val response = api.uploadImage(
                    file = body,
                    uploadPreset = uploadPreset,
                    cloudName = cloudName
                )
                    response.body()?.secure_url
            }
            catch (e: Exception){
                Log.e("TAG", "saveProfileImageUrlToFirebase: ${e.message}")
                null
            }
    }
}
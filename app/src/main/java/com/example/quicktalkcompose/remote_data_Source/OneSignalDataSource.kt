package com.example.quicktalkcompose.remote_data_Source

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.onesignal.OneSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class OneSignalDataSource {

    fun initOneSignal(context: Context){
        OneSignal.initWithContext(context, "de2c96ba-ae19-4127-bcfe-a65459fae9cd")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }
    }

    fun sendMessageNotification(context: Context, receiverUid: String, senderName: String?, senderImage : String, message : String) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val jsonBody = JSONObject().apply {
            put("app_id", "de2c96ba-ae19-4127-bcfe-a65459fae9cd")
            put("include_external_user_ids", JSONArray().put(receiverUid))
            put("headings", JSONObject().put("en", senderName))
            put("contents", JSONObject().put("en", message))
            put("large_icon", senderImage)
            put("data", JSONObject().apply {
                put("openChatWith", currentUid)
            })
        }

        val request = object : JsonObjectRequest(
            Method.POST,
            "https://onesignal.com/api/v1/notifications",
            jsonBody,
            { response -> Log.d("OneSignal", "Notification sent: $response") },
            { error -> Log.e("OneSignal", "Notification error: $error") }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Basic os_v2_app_3ywjnovodfaspph6uzkft6xjzuoay6mhlznuihndfvnmnsnkzwqphvfhk4bo2oxs63rwl4efb2ubncdxlslnosf6haupybytgcpzxjy",
                    "Content-Type" to "application/json"
                )
            }
            // os_v2_app_3ywjnovodfaspph6uzkft6xjzuoay6mhlznuihndfvnmnsnkzwqphvfhk4bo2oxs63rwl4efb2ubncdxlslnosf6haupybytgcpzxjy
        }

        Volley.newRequestQueue(context).add(request)
    }
}
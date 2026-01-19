package com.example.quicktalkcompose.signup_screen

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.onesignal.OneSignal
import kotlinx.coroutines.launch

class SignUpViewModel (application: Application, private val signUpRepository: SignUpRepository) : AndroidViewModel(application) {
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState : LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus(){
        if (auth.currentUser == null) _authState.value = AuthState.Unauthenticated
        else _authState.value = AuthState.Authenticated
    }

    fun signup(number : String, context: Context, firstSign : () -> Unit, alreadyExist : () -> Unit){
        OneSignal.initWithContext(context, "de2c96ba-ae19-4127-bcfe-a65459fae9cd")
        if (number.isEmpty()){
            _authState.value = AuthState.Error("email or password can't be empty")
            return
        }

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null){
            _authState.value = AuthState.Error("user is null")
            return
        }

        else{
            val uid = user.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()){
                    alreadyExist()
                    _authState.value = AuthState.Authenticated
                    OneSignal.login(uid)
                    OneSignal.User.pushSubscription.optIn()
                }
                else{
                    signUpRepository.signup(uid, number){ success ->
                        if (success){
                            firstSign()
                            _authState.value = AuthState.Authenticated
                            OneSignal.login(uid)
                            OneSignal.User.pushSubscription.optIn()
                        }
                    }
                }
            }
        }

    }

    fun signOut(){
        auth.signOut()
        OneSignal.logout()
        _authState.value = AuthState.Unauthenticated
    }

    fun saveProfileImageUrlToFirebase(imageUri: Uri) {
        viewModelScope.launch {
                val context = getApplication<Application>().applicationContext
                signUpRepository.saveProfileImageUrlToFirebase(context, imageUri)
        }
    }
}

sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message : String) : AuthState()
}

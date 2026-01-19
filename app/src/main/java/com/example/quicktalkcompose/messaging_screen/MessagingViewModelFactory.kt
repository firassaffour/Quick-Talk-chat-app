package com.example.quicktalkcompose.messaging_screen

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MessagingViewModelFactory(
    private val repository: MessagingRepository,
    private val application: Application
) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessagingViewModel(application, repository) as T
    }
}
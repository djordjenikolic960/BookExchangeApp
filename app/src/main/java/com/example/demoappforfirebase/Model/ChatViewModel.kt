package com.example.demoappforfirebase.Model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    var messages = MutableLiveData<ArrayList<Message>>()
    var hasNewMessages = MutableLiveData<Boolean>()

    init {
        hasNewMessages.value = false
        messages.value = arrayListOf()
    }
}
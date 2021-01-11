package com.example.demoappforfirebase.Model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    var messages = MutableLiveData<ArrayList<Message>>()
    var hasNewMessages = MutableLiveData<Boolean>()
    var user: User? = null
    var otherUser: User? = null

    init {
        hasNewMessages.value = false
        messages.value = arrayListOf()
    }
}
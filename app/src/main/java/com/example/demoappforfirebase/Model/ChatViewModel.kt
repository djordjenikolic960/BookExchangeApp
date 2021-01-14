package com.example.demoappforfirebase.Model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.firebase.database.DataSnapshot

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    var messages = MutableLiveData<ArrayList<Message>>()
    var hasNewMessages = MutableLiveData<Boolean>()
    var otherUser: User? = null
    var idsOfUserThatOwnerChatsWith = arrayListOf<String>()
    var preferencesHelper = PreferencesHelper(application)

    init {
        hasNewMessages.value = false
        messages.value = arrayListOf()
    }

    fun clearAll() {
        otherUser = null
        messages.value = arrayListOf()
    }

    fun updateIdsOfUsersThatOwnerChatsWith(dataSnapshot: DataSnapshot) {
        for (postSnapshot in dataSnapshot.children) {
            if (postSnapshot.key == "Chats") {
                for (snapShot in postSnapshot.children) {
                    if (preferencesHelper.getUserId().let { snapShot.key?.contains(it) }!!) {
                        var chatId = snapShot.key.toString()
                        chatId = chatId.replace(preferencesHelper.getUserId(), "")
                        idsOfUserThatOwnerChatsWith.add(chatId)
                    }
                }
            }
        }
    }
}
package com.example.demoappforfirebase.Model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class UserViewModel(application: Application) : AndroidViewModel(application) {
    val imageUrl = MutableLiveData<String>()
    var isMyProfile = false

    init {
        imageUrl.value = ""
    }
}
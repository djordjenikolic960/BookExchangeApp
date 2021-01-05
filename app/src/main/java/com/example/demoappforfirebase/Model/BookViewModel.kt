package com.example.demoappforfirebase.Model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class BookViewModel(application: Application) : AndroidViewModel(application) {
    var imageUrl:String? = null
}
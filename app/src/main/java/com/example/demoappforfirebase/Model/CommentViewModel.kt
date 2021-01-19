package com.example.demoappforfirebase.Model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class CommentViewModel(application: Application) : AndroidViewModel(application) {
    var shouldHideCommentLayout = MutableLiveData(true)
}
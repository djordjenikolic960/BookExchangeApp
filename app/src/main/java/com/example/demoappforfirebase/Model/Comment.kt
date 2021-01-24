package com.example.demoappforfirebase.Model

data class Comment(var userID: String, var comment: String, var timeStamp: Long) {
    constructor() : this("", "", 0L)
}
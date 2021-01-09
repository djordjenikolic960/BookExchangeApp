package com.example.demoappforfirebase.Model

data class Message(val id: String, var author: String, var message: String, var timestamp: Long, var isRead: Boolean) {
    constructor() : this("","", "", 0L, false)
}
package com.example.demoappforfirebase.Model

import java.util.*

data class Message(var author: String, var message: String, var timestamp: Long) {
    constructor() : this("", "", 0L)
}
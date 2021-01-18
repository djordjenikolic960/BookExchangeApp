package com.example.demoappforfirebase.Model

import java.util.*
import kotlin.collections.ArrayList

data class Book(
    var bookId: String,
    var ownerId: String,
    var title: String,
    var author: String,
    var image: String,
    var description: String,
    var categories: ArrayList<Int>,
    var timeStamp: Long,
    var comments: ArrayList<Comment>,
    var usersThatLiked: ArrayList<String>
) {
    constructor() : this("", "", "", "", "", "", ArrayList(), 0L, ArrayList(), ArrayList())
}

class Comment(var userID: String, var comment: String, var timeStamp: Long) {
    constructor() : this("", "", 0L)
}
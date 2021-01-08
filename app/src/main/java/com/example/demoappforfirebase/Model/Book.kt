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
    var timeStamp: Long
) {
    constructor() : this("", "", "", "", "", "", ArrayList(), Date().time)
}

enum class Categories{
    ACTION_AND_ADVENTURE,
    CLASSICS,
    MYSTERY,
    FANTASY,
    HORROR,
    ROMANCE,
    SCIENCE_FICTION,
    THRILLER,
    HISTORY,
    POETRY
}
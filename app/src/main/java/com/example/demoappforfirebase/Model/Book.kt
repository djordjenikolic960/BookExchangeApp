package com.example.demoappforfirebase.Model

data class Book(var bookId: String, var ownerId: String, var title: String, var author: String, var image: String) {
    constructor() : this("","", "", "", "")
}
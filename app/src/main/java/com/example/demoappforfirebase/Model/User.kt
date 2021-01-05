package com.example.demoappforfirebase.Model

data class User(
    var id: String,
    var name: String,
    var surname: String,
    var email: String,
    var password: String,
    var picture: String
) {
    constructor() : this("", "", "", "", "", "")
}
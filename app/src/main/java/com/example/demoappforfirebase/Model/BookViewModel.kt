package com.example.demoappforfirebase.Model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class BookViewModel(application: Application) : AndroidViewModel(application) {
    var imageUrl: String? = null
    var categoriesSelected = ArrayList<Int>()
    var oldBooks = ArrayList<Book>()
    var currentBooks = MutableLiveData<ArrayList<Book>>()

    init {
        currentBooks.value = arrayListOf()
    }

    fun getBooksByCategory(category: Int) {
        currentBooks.value = oldBooks.filter { it.categories.contains(category) } as ArrayList<Book>
    }

    fun updateBooksByAuthorAndTitle(query: String) {
        currentBooks.value = oldBooks.filter { it.author.contains(query) || it.title.contains(query) } as ArrayList<Book>
    }

    fun addBook(book: Book) {
        if (!oldBooks.contains(book)) {
            oldBooks.add(book)
        }
    }
}
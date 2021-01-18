package com.example.demoappforfirebase.Model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.demoappforfirebase.Utils.PreferencesHelper

class BookViewModel(application: Application) : AndroidViewModel(application) {
    var imageUrl: String? = null
    var categoriesSelected = ArrayList<Int>()
    var oldBooks = ArrayList<Book>()
    var currentBooks = MutableLiveData<ArrayList<Book>>()
    var book = MutableLiveData<Book>()
    var sortType = MutableLiveData<Int>()
    var currentCategories = MutableLiveData<ArrayList<Int>>()
    private val preferencesHelper = PreferencesHelper(application)
    var bookComments: ArrayList<Comment>? = null

    init {
        sortType.value = preferencesHelper.getSortType()
        currentBooks.value = arrayListOf()
        currentCategories.value = arrayListOf()
    }

    fun getBooksByCategory(category: Int): ArrayList<Book> {
        return oldBooks.filter {
            it.categories.contains(category)
        } as ArrayList<Book>
    }

    fun updateBooksByAuthorAndTitle(query: String) {
        currentBooks.value = oldBooks.filter { it.author.contains(query) || it.title.contains(query) } as ArrayList<Book>
    }

    fun addBook(book: Book) {
        val keys = arrayListOf<String>()
        oldBooks.forEach { keys.add(it.bookId) }
        if (!keys.contains(book.bookId)) {
            oldBooks.add(book)
        }
    }

    fun setNewSortType(newSortType: Int) {
        if (preferencesHelper.getSortType() != newSortType) {
            preferencesHelper.setSortType(newSortType)
            sortType.value = newSortType
        }
    }
}
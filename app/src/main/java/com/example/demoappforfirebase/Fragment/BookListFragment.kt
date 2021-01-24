package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoappforfirebase.Adapter.BooksAdapter
import com.example.demoappforfirebase.MainActivity
import com.example.demoappforfirebase.Model.Book
import com.example.demoappforfirebase.Model.BookViewModel
import com.example.demoappforfirebase.Model.UserViewModel
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_book_list.*

class BookListFragment : BaseFragment() {
    private lateinit var database: DatabaseReference
    private lateinit var bookRecycler: RecyclerView
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var bookVM: BookViewModel
    private lateinit var userVM: UserViewModel
    private var booksAdapter: BooksAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onBackPressed() {
        activity?.finish()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        createHelpers()
        bookRecycler = booksRecycler
        bookRecycler.layoutManager = LinearLayoutManager(requireContext())
        if (bookVM.currentBooks.value!!.isNotEmpty()) {
            if (booksAdapter == null) {
                booksAdapter = BooksAdapter(getSortedBooks(bookVM.currentBooks.value!!, bookVM.sortType.value!!))
                bookRecycler.adapter = booksAdapter
            } else {
                booksAdapter!!.updateDataSet(bookVM.currentBooks.value!!)
            }
        }
        bookVM.currentBooks.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                if (booksAdapter == null) {
                    booksAdapter = BooksAdapter(getSortedBooks(bookVM.currentBooks.value!!, bookVM.sortType.value!!))
                    bookRecycler.adapter = booksAdapter
                } else {
                    booksAdapter!!.updateDataSet(it)
                }
            }
        })

        bookVM.sortType.observe(viewLifecycleOwner, {
            bookVM.currentBooks.value = getSortedBooks(bookVM.oldBooks, it)
        })

        bookVM.currentCategories.observe(viewLifecycleOwner, {
            val allBooks = arrayListOf<Book>()
            for (index in it) {
                allBooks.addAll(bookVM.getBooksByCategory(index))
            }
            bookVM.currentBooks.value = allBooks
        })
    }

    private fun createHelpers() {
        fragmentHelper = FragmentHelper(requireActivity())
        bookVM = ViewModelProvider(requireActivity()).get(BookViewModel::class.java)
        userVM = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        database = FirebaseDatabase.getInstance().reference
    }

    private fun getSortedBooks(books: ArrayList<Book>, sortingType: Int): ArrayList<Book> {
        books.sortWith { book1, book2 ->
            when (sortingType) {
                MainActivity.SortType.NEWER_FIRST.ordinal -> book2.timeStamp.compareTo(book1.timeStamp)
                MainActivity.SortType.OLDER_FIRST.ordinal -> book1.timeStamp.compareTo(book2.timeStamp)
                MainActivity.SortType.A_TO_Z.ordinal -> book1.title.compareTo(book2.title)
                MainActivity.SortType.Z_TO_A.ordinal -> book2.title.compareTo(book1.title)
                MainActivity.SortType.MOST_LIKED.ordinal -> book2.usersThatLiked.size.compareTo(book1.usersThatLiked.size)
                else -> book2.comments.size.compareTo(book1.comments.size)
            }
        }
        return books
    }
}
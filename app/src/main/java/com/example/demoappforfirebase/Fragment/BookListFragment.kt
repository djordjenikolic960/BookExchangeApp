package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoappforfirebase.Adapter.BooksAdapter
import com.example.demoappforfirebase.MainActivity
import com.example.demoappforfirebase.Model.Book
import com.example.demoappforfirebase.Model.BookViewModel
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.AnalyticsUtil
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_book_list.*

class BookListFragment : BaseFragment() {
    private lateinit var database: DatabaseReference
    private lateinit var bookRecycler: RecyclerView
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var bookVM: BookViewModel
    private var booksAdapter: BooksAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onBackPressed() {
        activity?.finish()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHelpers()
        bookRecycler = booksRecycler
        bookRecycler.layoutManager = GridLayoutManager(requireContext(), 2)

        val databaseListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (postSnapshot in dataSnapshot.children) {
                        if (postSnapshot.key == "Books") {
                            for (snapShot in postSnapshot.children) {
                                val book: Book = snapShot.getValue(Book::class.java)!!
                                bookVM.addBook(book)
                            }
                        }
                    }
                    bookVM.currentBooks.value = bookVM.oldBooks
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                AnalyticsUtil.logError(requireContext(), databaseError.toString())
            }
        }

        bookVM.currentBooks.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                if (booksAdapter == null) {
                    booksAdapter = BooksAdapter(bookVM.currentBooks.value!!)
                    bookRecycler.adapter = booksAdapter
                } else {
                    booksAdapter!!.updateDataSet(it)
                }
            } else {
                Toast.makeText(requireContext(), "Still no books", Toast.LENGTH_SHORT).show()
            }
        })

        bookVM.sortType.observe(viewLifecycleOwner, {
            val books = bookVM.oldBooks
            books.sortWith { book1, book2 ->
                when (it) {
                    MainActivity.SortType.NEWER_FIRST.ordinal -> book2.timeStamp.compareTo(book1.timeStamp)
                    MainActivity.SortType.OLDER_FIRST.ordinal -> book1.timeStamp.compareTo(book2.timeStamp)
                    MainActivity.SortType.A_TO_Z.ordinal -> book1.title.compareTo(book2.title)
                    else -> book2.title.compareTo(book1.title)
                }
            }
            bookVM.currentBooks.value = books
        })

        bookVM.currentCategories.observe(viewLifecycleOwner, {
            val allBooks = arrayListOf<Book>()
            for (index in it) {
                allBooks.addAll(bookVM.getBooksByCategory(index))
            }
            bookVM.currentBooks.value = allBooks
        })

        database.addValueEventListener(databaseListener)
    }

    private fun setHelpers() {
        fragmentHelper = FragmentHelper(requireActivity())
        bookVM = ViewModelProvider(requireActivity()).get(BookViewModel::class.java)
        database = FirebaseDatabase.getInstance().reference
    }
}
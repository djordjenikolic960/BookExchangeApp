package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoappforfirebase.Adapter.BooksAdapter
import com.example.demoappforfirebase.Model.Book
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_book_list.*

class BookListFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var bookRecycler: RecyclerView
    private lateinit var fragmentHelper: FragmentHelper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fragmentHelper = FragmentHelper(requireActivity())
        database = FirebaseDatabase.getInstance().reference
        bookRecycler = booksRecycler
        bookRecycler.layoutManager = GridLayoutManager(requireContext(), 2)

        val databaseListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val books = arrayListOf<Book>()
                    for (postSnapshot in dataSnapshot.children) {
                        if (postSnapshot.key == "Books") {
                            for (snapShot in postSnapshot.children) {
                                val book: Book = snapShot.getValue(Book::class.java)!!
                                books.add(book)
                            }
                        }
                        val adapter = BooksAdapter(books)
                        bookRecycler.adapter = adapter
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
            }
        }

        database.addValueEventListener(databaseListener)
    }
}
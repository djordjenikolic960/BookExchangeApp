package com.example.demoappforfirebase.Fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.demoappforfirebase.Model.Book
import com.example.demoappforfirebase.Model.BookViewModel
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.AnalyticsUtil
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_book_more_details.*
import kotlinx.android.synthetic.main.fragment_book_more_details.bookAuthor
import kotlinx.android.synthetic.main.fragment_book_more_details.bookImage
import kotlinx.android.synthetic.main.fragment_book_more_details.bookName
import kotlinx.android.synthetic.main.fragment_book_more_details.bookDescription
import java.io.IOException

class BookMoreDetailsFragment : BaseFragment() {
    private lateinit var database: DatabaseReference
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var bookVM: BookViewModel
    private var bookId: String = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = layoutInflater.inflate(R.layout.fragment_book_more_details, container, false)
        val args = arguments
        if (args != null) {
            bookId = args.getString("bookId", "")
        }
        return rootView
    }

    override fun onBackPressed() {
        fragmentHelper.replaceFragment(BookListFragment::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHelpers()
        val bookQuery: Query =
            database.child("Books").child(bookId)
        bookQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val book = dataSnapshot.getValue(Book::class.java)!!
                try {
                    val image = decodeFromFirebaseBase64(book.image)
                    bookImage.setImageBitmap(image)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                bookName.text = book.title
                bookAuthor.text = book.author
                bookDescription.text = book.description
                bookVM.book.value = book
            }

            override fun onCancelled(databaseError: DatabaseError) {
                AnalyticsUtil.logError(requireContext(), databaseError.toString())
            }
        })

        bookVM.book.observe(viewLifecycleOwner, { book ->
            if (book != null) {
                moreDetailsProgressBar.visibility = View.GONE
                bookMoreDetailsParent.visibility = View.VISIBLE
                if (bookVM.book.value?.ownerId != preferencesHelper.getUserId()) {
                    contactUserButtons.visibility = View.VISIBLE
                    btnSeeUserProfile.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putString("userId", book.ownerId)
                        fragmentHelper.replaceFragment(UserProfileFragment::class.java, bundle)
                    }
                    btnContactUser.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putString("chatId", book.ownerId)
                        bundle.putString("bookId", book.bookId)
                        bundle.putString("openedFromFragment", BookMoreDetailsFragment::class.simpleName)
                        fragmentHelper.replaceFragment(ChatFragment::class.java, bundle)
                    }
                }
            }
        })
    }

    private fun setHelpers() {
        fragmentHelper = FragmentHelper(requireActivity())
        preferencesHelper = PreferencesHelper(requireContext())
        database = FirebaseDatabase.getInstance().reference
        bookVM = ViewModelProvider(requireActivity()).get(BookViewModel::class.java)
    }

    @Throws(IOException::class)
    fun decodeFromFirebaseBase64(image: String?): Bitmap? {
        val decodedByteArray = Base64.decode(image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
    }
}
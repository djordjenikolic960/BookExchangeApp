package com.example.demoappforfirebase.Fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.demoappforfirebase.Adapter.CommentsAdapter
import com.example.demoappforfirebase.Model.*
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.AnalyticsUtil
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.fragment_book_more_details.*
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
        bookVM.book.value = null
        fragmentHelper.replaceFragment(BookListFragment::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHelpers()
        val bookQuery: Query = database.child("Books").child(bookId)

        bookQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val book = dataSnapshot.getValue(Book::class.java)!!
                try {
                    val image = decodeFromFirebaseBase64(book.image)
                    bookImage.setImageBitmap(image)
                    Blurry.with(context).from(image).into(bookBackground)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                bookTitle.text = book.title
                bookAuthor.text = book.author
                bookDescription.text = book.description
                bookVM.bookComments = book.comments ?: arrayListOf()
                bookVM.book.value = book
            }

            override fun onCancelled(databaseError: DatabaseError) {
                AnalyticsUtil.logError(requireContext(), databaseError.toString())
            }
        })

        val commentsQuery = database.child("Books").child(bookId).child("comments")
        commentsQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (bookVM.bookComments != null) {
                    val usersWhoCommented = ArrayList<User>()
                    val database = FirebaseDatabase.getInstance().reference
                    database.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (postSnapshot in snapshot.children) {
                                    if (postSnapshot.key == "Users") {
                                        for (snapShot in postSnapshot.children) {
                                            val user: User = snapShot.getValue(User::class.java)!!
                                            if(!usersWhoCommented.contains(user)){
                                                usersWhoCommented.add(user)
                                            }
                                        }
                                    }
                                }
                                bookComments.adapter = CommentsAdapter(bookVM.bookComments, usersWhoCommented)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            AnalyticsUtil.logError(requireContext(), error.toString())
                        }
                    })
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}

        })

        bookVM.book.observe(viewLifecycleOwner, { book ->
            if (book != null) {
                moreDetailsProgressBar.visibility = View.GONE
                tabLayout.selectTab(tabLayout.getTabAt(0))
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
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                //todo proveriti koji je tab i uraditi nesto u vezi toga
                bookDescription.text = bookVM.book.value?.description
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                bookDescription.text = bookVM.book.value?.description
            }
        })
    }

    private fun addToDatabase() {//TODO add comment to databse
        //Omoguciti unos komentara
        bookVM.book.value!!.comments.add(Comment(preferencesHelper.getUserId(), "ja sdbnsakjfasfuo polju skace", System.currentTimeMillis()))
        database.child("Books").child(bookVM.book.value!!.bookId).child("comments").setValue(bookVM.book.value)
    }

    private fun createHelpers() {
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
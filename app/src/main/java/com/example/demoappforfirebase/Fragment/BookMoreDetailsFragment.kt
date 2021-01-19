package com.example.demoappforfirebase.Fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.ascendik.diary.util.ImageUtil
import com.example.demoappforfirebase.Adapter.CommentsAdapter
import com.example.demoappforfirebase.Model.*
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.*
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.fragment_book_more_details.*
import kotlinx.android.synthetic.main.view_contact_book_owner.*
import java.io.IOException

class BookMoreDetailsFragment : BaseFragment() {
    private lateinit var database: DatabaseReference
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var commentLayout: LinearLayout
    private lateinit var commentMessage: EditText
    private lateinit var commentSendButton: ImageView
    private lateinit var bookVM: BookViewModel
    private lateinit var userVM: UserViewModel
    private lateinit var commentVM: CommentViewModel
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
        commentVM.shouldHideCommentLayout.value = true
        StyleUtil.hideSoftKeyboard(commentMessage)
        fragmentHelper.replaceFragment(BookListFragment::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        createHelpers()
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
                    val adapter = CommentsAdapter(bookVM.bookComments!!, userVM.allUsers)
                    bookComments.adapter = adapter
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
                BitmapUtil.updateHeartImageView(actionMore, book.usersThatLiked.contains(preferencesHelper.getUserId()))
                actionMore.setOnClickListener {
                    if (!book.usersThatLiked.contains(preferencesHelper.getUserId())) {
                        book.usersThatLiked.add(preferencesHelper.getUserId())
                    } else {
                        book.usersThatLiked.remove(preferencesHelper.getUserId())
                    }
                    BitmapUtil.updateHeartImageView(actionMore, book.usersThatLiked.contains(preferencesHelper.getUserId()))
                    database.child("Books").child(book.bookId).child("usersThatLiked").setValue(book.usersThatLiked)
                }
                tabLayout.selectTab(tabLayout.getTabAt(0))
                if (bookVM.book.value?.ownerId != preferencesHelper.getUserId()) {
                    contactBookOwnerLayout.visibility = View.VISIBLE
                    val userQuery = bookVM.book.value?.ownerId?.let { database.child("Users").child(it) }
                    userQuery?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue(User::class.java)
                            userName.text = user?.name
                            if (user?.picture == "") {
                                userNameFirstLetter.text = user.name.first().toString()
                                userImage.setBackgroundDrawable(
                                    StyleUtil.getRoundedShapeDrawable(
                                        StyleUtil.getAttributeColor(requireContext(), android.R.attr.textColorPrimary),
                                        200f
                                    )
                                )
                            } else {
                                try {
                                    val image = ImageUtil.decodeFromFirebaseBase64(user?.picture)
                                    userImage.setImageBitmap(image)
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            AnalyticsUtil.logError(requireContext(), error.toString())
                        }

                    })
                    contactBookOwnerBtn.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putString("chatId", book.ownerId)
                        bundle.putString("bookId", book.bookId)
                        bundle.putString("openedFromFragment", BookMoreDetailsFragment::class.simpleName)
                        fragmentHelper.replaceFragment(ChatFragment::class.java, bundle)
                    }
                } else {
                    contactBookOwnerLayout.visibility = View.GONE
                }
            }
        })
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateTab(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                updateTab(tab)
            }
        })

        commentVM.shouldHideCommentLayout.observe(viewLifecycleOwner, {
            commentLayout.isVisible = !it
        })

        commentMessage.setOnFocusChangeListener { _, hasFocus ->
            StyleUtil.stylizeStatusBar(requireActivity(), !hasFocus)
        }

        commentSendButton.setOnClickListener {
            addCommentToDatabase(commentMessage.editableText.toString())
            commentMessage.editableText.clear()
        }
    }

    private fun updateTab(tab: TabLayout.Tab?) {
        if (tab == tabLayout.getTabAt(0)) {
            commentLayout.visibility = View.GONE
            StyleUtil.hideSoftKeyboard(commentMessage)
            bookDescription.isVisible = true
            bookDescription.text = bookVM.book.value?.description
            bookComments.isVisible = false
            commentVM.shouldHideCommentLayout.value = true
            contactBookOwnerLayout.isVisible = true
        } else {
            bookDescription.isVisible = false
            commentLayout.isVisible = true
            bookComments.isVisible = true
            commentVM.shouldHideCommentLayout.value = false
            contactBookOwnerLayout.isVisible = false
        }
    }

    private fun addCommentToDatabase(comment: String) {
        bookVM.book.value!!.comments.add(Comment(preferencesHelper.getUserId(), comment, System.currentTimeMillis()))
        database.child("Books").child(bookVM.book.value!!.bookId).child("comments").setValue(bookVM.book.value?.comments)
    }

    private fun createHelpers() {
        fragmentHelper = FragmentHelper(requireActivity())
        preferencesHelper = PreferencesHelper(requireContext())
        database = FirebaseDatabase.getInstance().reference
        bookVM = ViewModelProvider(requireActivity()).get(BookViewModel::class.java)
        userVM = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        commentVM = ViewModelProvider(requireActivity()).get(CommentViewModel::class.java)
        commentLayout = activity?.findViewById(R.id.commentLayout)!!
        commentMessage = activity?.findViewById(R.id.commentMessage)!!
        commentSendButton = activity?.findViewById(R.id.btnSendComment)!!

    }

    @Throws(IOException::class)
    fun decodeFromFirebaseBase64(image: String?): Bitmap? {
        val decodedByteArray = Base64.decode(image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
    }
}
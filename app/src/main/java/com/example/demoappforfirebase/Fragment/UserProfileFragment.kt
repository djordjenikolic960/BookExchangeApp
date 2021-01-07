package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.ascendik.diary.util.ImageUtil
import com.example.demoappforfirebase.Adapter.BooksAdapter
import com.example.demoappforfirebase.MainActivity
import com.example.demoappforfirebase.Model.*
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.AnalyticsUtil
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_user_profile.*
import java.lang.StringBuilder

class UserProfileFragment : BaseFragment() {
    private lateinit var database: DatabaseReference
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var userViewModel: UserViewModel
    private lateinit var user: User

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onBackPressed() {
        FragmentHelper(requireActivity()).replaceFragment(BookListFragment::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHelpers()
        val userQuery = database.child("Users").child(preferencesHelper.getUserId())
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)!!
                userViewModel.imageUrl.value = user.picture
                if (user.picture.isNotEmpty()) {
                    profileImage.setImageBitmap(ImageUtil.decodeFromFirebaseBase64(user.picture))
                } else {
                    profileImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_profile, requireContext().theme))
                }
                val userName = StringBuilder().append(user.name).append(" ").append(user.surname).toString()
                profileName.text = userName
            }

            override fun onCancelled(error: DatabaseError) {
                AnalyticsUtil.logError(requireContext(), error.toString())
            }
        })

        val booksQuery = database.child("Books").orderByChild("ownerId").equalTo(preferencesHelper.getUserId())
        booksQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val books = arrayListOf<Book>()
                    for (postSnapshot in snapshot.children) {
                        val book: Book = postSnapshot.getValue(Book::class.java)!!
                        books.add(book)
                    }
                    userBooksCount.text = books.size.toString()
                    val adapter = BooksAdapter(books)
                    userBooksRecycler.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                AnalyticsUtil.logError(requireContext(), error.toString())
            }
        })

        val userConnections = database.child("Chats")
        userConnections.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val allChatsForUser = (snapshot.value as HashMap<String, String>).keys.filter { it.contains(preferencesHelper.getUserId()) }
                    userConnectionsCount.text = allChatsForUser.size.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                AnalyticsUtil.logError(requireContext(), error.toString())
            }
        })

        profileImage.setOnClickListener {
            ImageUtil.onLaunchCamera(requireActivity() as MainActivity)
        }

        userViewModel.imageUrl.observe(viewLifecycleOwner,
            {
                if (it.isNotEmpty() && it != user.picture) {
                    database.child("Users").child(preferencesHelper.getUserId()).child("picture").setValue(it)
                    profileImage.setImageBitmap(ImageUtil.decodeFromFirebaseBase64(it))
                }
            })
    }

    private fun setHelpers() {
        database = FirebaseDatabase.getInstance().reference
        preferencesHelper = PreferencesHelper(requireContext())
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
    }
}
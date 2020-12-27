package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.demoappforfirebase.Model.BookViewModel
import com.example.demoappforfirebase.Model.Message
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_chats.*

class ChatsFragment: Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var fragmentHelper: FragmentHelper
    private var chatId = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fragmentHelper = FragmentHelper(requireActivity())
        database = FirebaseDatabase.getInstance().reference
        val databaseListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (postSnapshot in dataSnapshot.children) {
                        if (postSnapshot.key == "Chats") {
                            for (snapShot in postSnapshot.children) {
                                if (Firebase.auth.uid?.let { snapShot.key?.contains(it) }!!) {
                                    chatId = snapShot.key.toString()
                                    //chat.text = chatId
                                }
                            }
                        }

                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
            }
        }
        database.addValueEventListener(databaseListener)
        chat.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("chatId", chatId)
            fragmentHelper.replaceFragment(ChatFragment::class.java, bundle)
        }
    }
}
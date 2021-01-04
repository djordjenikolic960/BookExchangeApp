package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.demoappforfirebase.Adapter.ChatsAdapter
import com.example.demoappforfirebase.Model.User
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_chat_list.*

class ChatListFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var fragmentHelper: FragmentHelper
    private var chatId = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fragmentHelper = FragmentHelper(requireActivity())
        database = FirebaseDatabase.getInstance().reference
        val databaseListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val chattingUsersId = arrayListOf<String>()
                    for (postSnapshot in dataSnapshot.children) {
                        if (postSnapshot.key == "Chats") {
                            for (snapShot in postSnapshot.children) {
                                if (Firebase.auth.uid?.let { snapShot.key?.contains(it) }!!) {
                                    chatId = snapShot.key.toString()
                                    chatId = chatId.replace(Firebase.auth.uid!!, "")
                                    chattingUsersId.add(chatId)
                                }
                            }
                        }
                    }
                    if (chattingUsersId.isNotEmpty()) {
                        val databaseListener = object : ValueEventListener {

                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    val users = arrayListOf<User>()
                                    var numberOfUsers = 0
                                    for (postSnapshot in dataSnapshot.children) {
                                        if (postSnapshot.key == "Users") {
                                            for (snapShot in postSnapshot.children) {
                                                if (numberOfUsers < chattingUsersId.size && snapShot.key == chattingUsersId[numberOfUsers]) {
                                                    val user: User = snapShot.getValue(User::class.java)!!
                                                    users.add(user)
                                                    numberOfUsers++
                                                }
                                            }
                                        }
                                        val adapter = ChatsAdapter(users)
                                        if (fragmentHelper.isFragmentVisible(ChatListFragment::class.java)) {
                                            userRecycler.adapter = adapter
                                        }
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
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
            }
        }
        database.addValueEventListener(databaseListener)
    }
}
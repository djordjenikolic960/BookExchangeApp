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
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_chat_list.*

class ChatListFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var preferencesHelper: PreferencesHelper
    private var chatId = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fragmentHelper = FragmentHelper(requireActivity())
        database = FirebaseDatabase.getInstance().reference
        preferencesHelper = PreferencesHelper(requireContext())
        val databaseListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val chattingUsersId = arrayListOf<String>()
                    for (postSnapshot in dataSnapshot.children) {
                        if (postSnapshot.key == "Chats") {
                            for (snapShot in postSnapshot.children) {
                                if (preferencesHelper.getUserId().let { snapShot.key?.contains(it) }!!) {
                                    chatId = snapShot.key.toString()
                                    chatId = chatId.replace(preferencesHelper.getUserId(), "")
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
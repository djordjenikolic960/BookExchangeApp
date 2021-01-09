package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.demoappforfirebase.Adapter.ChatsAdapter
import com.example.demoappforfirebase.Model.Message
import com.example.demoappforfirebase.Model.User
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.AnalyticsUtil
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_chat_list.*
import java.lang.StringBuilder

class ChatListFragment : BaseFragment() {
    private lateinit var database: DatabaseReference
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var preferencesHelper: PreferencesHelper
    private var chatId = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onBackPressed() {
        fragmentHelper.replaceFragment(BookListFragment::class.java)
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
                                    val lastMessages = arrayListOf<Message>()
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
                                        val adapter = ChatsAdapter(users, lastMessages)
                                        if (fragmentHelper.isFragmentVisible(ChatListFragment::class.java)) {
                                            userRecycler.adapter = adapter
                                        }
                                    }
                                    if (users.isNotEmpty()) {
                                        for (user in users) {
                                            var id = ""
                                            val stringBuilder = StringBuilder()
                                            id = if (preferencesHelper.getUserId() > user.id) {
                                                stringBuilder.append(preferencesHelper.getUserId()).append(user.id).toString()
                                            } else {
                                                stringBuilder.append(user.id).append(preferencesHelper.getUserId()).toString()
                                            }
                                            val messages = arrayListOf<Message>()
                                            val chatQuery: Query =
                                                database.child("Chats").child(id)
                                            chatQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    for (child in snapshot.children) {
                                                        val msg = child.getValue(Message::class.java)
                                                        messages.add(msg!!)
                                                    }
                                                    lastMessages.add(messages.last())
                                                    val adapter = ChatsAdapter(users, lastMessages)
                                                    if (fragmentHelper.isFragmentVisible(ChatListFragment::class.java)) {
                                                        userRecycler.adapter = adapter
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    TODO("Not yet implemented")
                                                }
                                            })
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                AnalyticsUtil.logError(requireContext(), databaseError.toString())
                            }
                        }

                        database.addValueEventListener(databaseListener)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                AnalyticsUtil.logError(requireContext(), databaseError.toString())
            }
        }
        database.addValueEventListener(databaseListener)
    }
}
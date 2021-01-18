package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.demoappforfirebase.Adapter.ChatsAdapter
import com.example.demoappforfirebase.Model.ChatViewModel
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
    private lateinit var chatVM: ChatViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onBackPressed() {
        fragmentHelper.replaceFragment(BookListFragment::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        createHelpers()
        val databaseListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    chatVM.idsOfUserThatOwnerChatsWith.clear()
                    chatVM.updateIdsOfUsersThatOwnerChatsWith(dataSnapshot)
                    if (chatVM.idsOfUserThatOwnerChatsWith.isNotEmpty()) {
                        val databaseListener = object : ValueEventListener {

                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    val users = arrayListOf<User>()
                                    val lastMessages = arrayListOf<Message>()
                                    var numberOfUsers = 0
                                    for (postSnapshot in dataSnapshot.children) {
                                        if (postSnapshot.key == "Users") {
                                            for (snapShot in postSnapshot.children) {
                                                if (numberOfUsers < chatVM.idsOfUserThatOwnerChatsWith.size && snapShot.key == chatVM.idsOfUserThatOwnerChatsWith[numberOfUsers]) {
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
                                            val id = chatVM.recreateChatIdWithUser(user.id)
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
                                                    AnalyticsUtil.logError(requireContext(), error.toString())
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

    private fun createHelpers() {
        chatVM = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
        fragmentHelper = FragmentHelper(requireActivity())
        database = FirebaseDatabase.getInstance().reference
        preferencesHelper = PreferencesHelper(requireContext())
    }
}
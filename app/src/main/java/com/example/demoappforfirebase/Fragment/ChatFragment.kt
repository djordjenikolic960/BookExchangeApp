package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.ascendik.diary.util.ImageUtil
import com.example.demoappforfirebase.Model.ChatViewModel
import com.example.demoappforfirebase.Model.Message
import com.example.demoappforfirebase.Model.User
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.AnalyticsUtil
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.example.demoappforfirebase.Utils.StyleUtil
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.view_chat_message.view.*
import kotlinx.android.synthetic.main.view_chat_message.view.userNameFirstLetter
import java.lang.StringBuilder

class ChatFragment : BaseFragment() {
    private lateinit var database: DatabaseReference
    private lateinit var chatVM: ChatViewModel
    private var otherUserId: String = ""
    private var id = ""
    private var bookIdIfFromBookMoreDetails = ""
    private var openedFragmentFrom = ""
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var fragmentHelper: FragmentHelper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = layoutInflater.inflate(R.layout.fragment_chat, container, false)
        val args = arguments
        if (args != null) {
            otherUserId = args.getString("chatId", "")
            openedFragmentFrom = args.getString("openedFromFragment", "")
            bookIdIfFromBookMoreDetails = args.getString("bookId", "")
        }
        return rootView
    }

    override fun onBackPressed() {
        when (openedFragmentFrom) {
            ChatListFragment::class.simpleName -> fragmentHelper.replaceFragment(ChatListFragment::class.java)
            BookMoreDetailsFragment::class.simpleName -> {
                val bundle = Bundle()
                bundle.putString("bookId", bookIdIfFromBookMoreDetails)
                fragmentHelper.replaceFragment(BookMoreDetailsFragment::class.java, bundle)
            }
            else -> {
                val bundle = Bundle()
                bundle.putString("userId", otherUserId)
                fragmentHelper.replaceFragment(UserProfileFragment::class.java, bundle)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        chatVM = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
        preferencesHelper = PreferencesHelper(requireContext())
        fragmentHelper = FragmentHelper(requireActivity())
        database = FirebaseDatabase.getInstance().reference
        val otherUserQuery = database.child("Users").child(otherUserId)
        otherUserQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatVM.otherUser = dataSnapshot.getValue(User::class.java)!!
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        val userQuery = database.child("Users").child(preferencesHelper.getUserId())
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatVM.user = dataSnapshot.getValue(User::class.java)!!
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        chatVM.messages.observe(viewLifecycleOwner, {
            chat.removeAllViews()
            for (message in it) {
                updateMessageStatus(message)
                chat.addView(getMessageView(message))
            }
        })

        val databaseListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val messages = arrayListOf<Message>()
                    for (postSnapshot in dataSnapshot.children) {
                        if (postSnapshot.key == "Chats") {
                            for (snapShot in postSnapshot.children) {
                                val stringBuilder = StringBuilder()
                                id = if (preferencesHelper.getUserId() > otherUserId) {
                                    stringBuilder.append(preferencesHelper.getUserId()).append(otherUserId).toString()
                                } else {
                                    stringBuilder.append(otherUserId).append(preferencesHelper.getUserId()).toString()
                                }
                                if (snapShot.key == id) {
                                    for (message in snapShot.children) {
                                        val msg: Message = message.getValue(Message::class.java)!!
                                        messages.add(msg)
                                    }
                                }
                                chatVM.messages.value = messages
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                AnalyticsUtil.logError(requireContext(), databaseError.toString())
            }
        }
        database.addValueEventListener(databaseListener)
        btnSend.setOnClickListener {
            val stringBuilder = StringBuilder()
            id = if (preferencesHelper.getUserId() > otherUserId) {
                stringBuilder.append(preferencesHelper.getUserId()).append(otherUserId).toString()
            } else {
                stringBuilder.append(otherUserId).append(preferencesHelper.getUserId()).toString()
            }
            val generatedId: String = database.push().key!!
            database.child("Chats").child(id).child(generatedId)
                .setValue(Message(generatedId, preferencesHelper.getUserId(), message.editableText.toString(), System.currentTimeMillis(), false))
            message.editableText.clear()
        }
        message.setOnFocusChangeListener { _, hasFocus ->
            StyleUtil.stylizeStatusBar(requireActivity(), !hasFocus)
        }
    }

    private fun updateMessageStatus(message: Message) {
        val stringBuilder = StringBuilder()
        id = if (preferencesHelper.getUserId() > otherUserId) {
            stringBuilder.append(preferencesHelper.getUserId()).append(otherUserId).toString()
        } else {
            stringBuilder.append(otherUserId).append(preferencesHelper.getUserId()).toString()
        }
        message.isRead = true
        if (message.author != preferencesHelper.getUserId()) {
            database.child("Chats").child(id).child(message.id).setValue(message)
        }
    }

    private fun getMessageView(message: Message): View {
        val inflater = LayoutInflater.from(requireContext())
        val nullParent: ViewGroup? = null
        val view: View = inflater.inflate(R.layout.view_chat_message, nullParent)
        if (message.author == preferencesHelper.getUserId()) {
            if (chatVM.user?.picture == "") {
                view.userNameFirstLetter.text = chatVM.user?.name?.first()?.toUpperCase().toString()
            } else {
                val image = ImageUtil.decodeFromFirebaseBase64(chatVM.user?.picture)
                view.userImage.setImageBitmap(image)
            }
            view.messageCard.setCardBackgroundColor(StyleUtil.getAttributeColor(requireContext(), R.attr.my_message_color))
            view.messageLayout.gravity = Gravity.END
            val imageLayout = view.imageLayout
            view.messageLayout.removeView(view.imageLayout)
            view.messageLayout.addView(imageLayout)
        } else {
            if (chatVM.otherUser?.picture == "") {
                view.userNameFirstLetter.text = chatVM.otherUser?.name?.first()?.toUpperCase().toString()
            } else {
                val image = ImageUtil.decodeFromFirebaseBase64(chatVM.otherUser?.picture)
                view.userImage.setImageBitmap(image)
            }
            view.messageCard.setCardBackgroundColor(StyleUtil.getAttributeColor(requireContext(), R.attr.other_message_color))
            view.messageLayout.gravity = Gravity.START
        }
        view.message.text = message.message
        return view
    }
}
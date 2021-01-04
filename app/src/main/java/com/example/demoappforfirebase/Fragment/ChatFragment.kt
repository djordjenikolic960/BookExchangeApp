package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.demoappforfirebase.Model.BookViewModel
import com.example.demoappforfirebase.Model.ChatViewModel
import com.example.demoappforfirebase.Model.Message
import com.example.demoappforfirebase.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_chat.*
import java.lang.StringBuilder

class ChatFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var chatVM: ChatViewModel
    private var chatId: String = ""
    private var id = ""
    private lateinit var bookVM: BookViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = layoutInflater.inflate(R.layout.fragment_chat, container, false)
        val args = arguments
        if (args != null) {
            chatId = args.getString("chatId", "")
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        chatVM = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
        chatVM.messages.observe(viewLifecycleOwner, {
            chat.removeAllViews()
            for (message in it) {
                val cardView = CardView(requireContext())
                val cardParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                val textView = TextView(requireContext())
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                if (message.author == Firebase.auth.uid) {
                    params.gravity = Gravity.END
                } else {
                    params.gravity = Gravity.START
                }
                textView.textSize = 25f
                textView.layoutParams = params
                textView.text = message.message
                chat.addView(textView)
            }
        })
        database = FirebaseDatabase.getInstance().reference
        val databaseListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val messages = arrayListOf<Message>()
                    for (postSnapshot in dataSnapshot.children) {
                        if (postSnapshot.key == "Chats") {
                            for (snapShot in postSnapshot.children) {
                                val stringBuilder = StringBuilder()
                                id = if (Firebase.auth.uid.toString() > chatId) {
                                    stringBuilder.append(Firebase.auth.uid).append(chatId).toString()
                                } else {
                                    stringBuilder.append(chatId).append(Firebase.auth.uid).toString()
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
                // Failed to read value
            }
        }
        database.addValueEventListener(databaseListener)
        btnSend.setOnClickListener {
            val generatedId: String = database.push().key!!
            database.child("Chats").child(id).child(generatedId)
                .setValue(Message(Firebase.auth.uid!!, message.editableText.toString(), System.currentTimeMillis()))
            message.editableText.clear()
        }
    }
}
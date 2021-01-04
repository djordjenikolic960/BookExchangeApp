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
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.example.demoappforfirebase.Utils.StyleUtil
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_chat.*
import java.lang.StringBuilder

class ChatFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var chatVM: ChatViewModel
    private var chatId: String = ""
    private var id = ""
    private lateinit var bookVM: BookViewModel
    private lateinit var preferencesHelper: PreferencesHelper
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
        preferencesHelper = PreferencesHelper(requireContext())
        chatVM.messages.observe(viewLifecycleOwner, {
            chat.removeAllViews()
            for (message in it) {
                val cardView = CardView(requireContext())
                val cardParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                val textView = TextView(requireContext())
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                if (message.author ==  preferencesHelper.getUserId()) {
                    cardParams.gravity = Gravity.END
                    cardView.setCardBackgroundColor(resources.getColor(R.color.peachOrangeDark))

                } else {
                    cardView.setCardBackgroundColor(resources.getColor(R.color.white))
                    cardParams.gravity = Gravity.START
                }
                cardParams.setMargins(4, 4, 4, 4)
                cardView.layoutParams = cardParams
                cardView.radius = 50f
                textView.textSize = 18f
                textView.setTextColor(resources.getColor(R.color.white))
                params.setMargins(
                    resources.getDimension(R.dimen.message_horizontal_margin).toInt(),
                    resources.getDimension(R.dimen.message_vertical_margin).toInt(),
                    resources.getDimension(R.dimen.message_horizontal_margin).toInt(),
                    resources.getDimension(R.dimen.message_vertical_margin).toInt()
                )
                textView.layoutParams = params
                textView.text = message.message
                cardView.addView(textView)
                chat.addView(cardView)
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
                                id = if (preferencesHelper.getUserId() > chatId) {
                                    stringBuilder.append(preferencesHelper.getUserId()).append(chatId).toString()
                                } else {
                                    stringBuilder.append(chatId).append(preferencesHelper.getUserId()).toString()
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
                .setValue(Message(preferencesHelper.getUserId(), message.editableText.toString(), System.currentTimeMillis()))
            message.editableText.clear()
        }
        message.setOnFocusChangeListener { _, hasFocus ->
            StyleUtil.stylizeStatusBar(requireActivity(), !hasFocus)
        }
    }
}
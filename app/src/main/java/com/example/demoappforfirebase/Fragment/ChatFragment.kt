package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.example.demoappforfirebase.Model.ChatViewModel
import com.example.demoappforfirebase.Model.Message
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.AnalyticsUtil
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.example.demoappforfirebase.Utils.StyleUtil
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_chat.*
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
            openedFragmentFrom = args.getString("openedFromFragment","")
            bookIdIfFromBookMoreDetails = args.getString("bookId","")
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
        chatVM.messages.observe(viewLifecycleOwner, {
            chat.removeAllViews()
            for (message in it) {
                val cardView = CardView(requireContext())
                val cardParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                val textView = TextView(requireContext())
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                if (message.author == preferencesHelper.getUserId()) {
                    cardParams.gravity = Gravity.END
                    cardView.setCardBackgroundColor(StyleUtil.getAttributeColor(requireContext(), R.attr.my_message_color))
                } else {
                    cardView.setCardBackgroundColor(StyleUtil.getAttributeColor(requireContext(), R.attr.other_message_color))
                    cardParams.gravity = Gravity.START
                }
                cardParams.setMargins(4, 4, 4, 4)
                cardView.layoutParams = cardParams
                cardView.radius = 50f
                textView.textSize = 18f
                textView.setTextColor(ResourcesCompat.getColor(resources,R.color.white, requireContext().theme))
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
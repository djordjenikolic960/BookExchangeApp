package com.example.demoappforfirebase.Adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.ascendik.diary.util.ImageUtil
import com.example.demoappforfirebase.Fragment.ChatFragment
import com.example.demoappforfirebase.Model.Message
import com.example.demoappforfirebase.Fragment.ChatListFragment
import com.example.demoappforfirebase.Model.User
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.example.demoappforfirebase.Utils.StyleUtil
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatsAdapter(private val usersDataSet: ArrayList<User>, private val messagesDataSet: ArrayList<Message>) :
    RecyclerView.Adapter<ChatsAdapter.UserHolder>() {
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var database: FirebaseDatabase

    class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: CircleImageView = itemView.findViewById(R.id.userImage)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val lastMessage: TextView = itemView.findViewById(R.id.lastMsg)
        val userNameFirstLetter: TextView = itemView.findViewById(R.id.userNameFirstLetter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_chat_card, parent, false)
        fragmentHelper = FragmentHelper(parent.context as FragmentActivity)
        preferencesHelper = PreferencesHelper(parent.context)
        database = FirebaseDatabase.getInstance()
        return UserHolder(view)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        if (usersDataSet.isNotEmpty()) {
            val current = usersDataSet[position]
            holder.userName.text = StringBuilder().append(current.name).append(" ").append(current.surname)
            if (current.picture == "") {
                holder.userNameFirstLetter.text = current.name.first().toString()
                holder.userImage.setBackgroundDrawable(
                    StyleUtil.getRoundedShapeDrawable(
                        StyleUtil.getAttributeColor(holder.userImage.context, android.R.attr.textColorPrimary),
                        200f
                    )
                )
            } else {
                try {
                    val image = ImageUtil.decodeFromFirebaseBase64(current.picture)
                    holder.userImage.setImageBitmap(image)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            holder.itemView.setOnClickListener {
                showChatFragment(current)
            }

        }
        if (messagesDataSet.isNotEmpty()) {
            holder.lastMessage.text = StringBuilder().append(messagesDataSet[position].message).append(" ")
                .append(SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date(messagesDataSet[position].timestamp)))
        }
    }

    override fun getItemCount(): Int {
        return usersDataSet.size
    }

    private fun showChatFragment(user: User) {
        val bundle = Bundle()
        updateMessagesStatus(user)
        bundle.putString("chatId", user.id)
        bundle.putString("openedFromFragment", ChatListFragment::class.simpleName)
        fragmentHelper.replaceFragment(ChatFragment::class.java, bundle)
    }

    private fun updateMessagesStatus(user: User) {
        val id: String
        val stringBuilder = StringBuilder()
        id = if (preferencesHelper.getUserId() > user.id) {
            stringBuilder.append(preferencesHelper.getUserId()).append(user.id).toString()
        } else {
            stringBuilder.append(user.id).append(preferencesHelper.getUserId()).toString()
        }
        val chatQuery: Query =
            database.reference.child("Chats").child(id)
        chatQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val msg = child.getValue(Message::class.java)
                    msg!!.isRead = true
                    database.reference.child("Chats").child(id).child(msg.id).setValue(msg)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}
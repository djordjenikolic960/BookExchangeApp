package com.example.demoappforfirebase.Adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.demoappforfirebase.Fragment.ChatFragment
import com.example.demoappforfirebase.Model.User
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import java.lang.StringBuilder

class ChatsAdapter (private val dataSet: ArrayList<User>) :
    RecyclerView.Adapter<ChatsAdapter.UserHolder>() {
    private lateinit var fragmentHelper: FragmentHelper

    class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //todo ubaciti sliku korisnika pa ako je ima onda je prikazati ako ne neka ostane ovako drawable
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userName: TextView = itemView.findViewById(R.id.userName)
        //todo ovo mozda ne mora, takodje ubaciti pre koliko je bila poslednja poruka
        val lastMessage: TextView = itemView.findViewById(R.id.lastMsg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_chat_card, parent, false)
        fragmentHelper = FragmentHelper(parent.context as FragmentActivity)
        return UserHolder(view)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val current = dataSet[position]
        holder.userName.text = StringBuilder().append(current.name).append(" ").append(current.surname)
        holder.itemView.setOnClickListener {
            showChatFragment(current)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    private fun showChatFragment(user: User) {
        val bundle = Bundle()
        bundle.putString("chatId", user.id)
        fragmentHelper.replaceFragment(ChatFragment::class.java, bundle)
    }
}
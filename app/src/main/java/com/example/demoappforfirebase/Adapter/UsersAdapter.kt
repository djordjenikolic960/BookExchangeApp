package com.example.demoappforfirebase.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.firebase.database.*

class UsersAdapter(private val dataSet: ArrayList<String>) :
    RecyclerView.Adapter<UsersAdapter.UserHolder>() {
    class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userId: TextView = itemView.findViewById(R.id.userId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_user_card, parent, false)
        return UserHolder(view)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val current = dataSet[position]
        holder.userId.text = current
        holder.userId.setOnClickListener {
            val database =  FirebaseDatabase.getInstance().reference
            val userQuery: Query =
               database.child("simpleChat").child("users")
            userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val senderId = PreferencesHelper(holder.itemView.context).getUserId()
                        database.child("messages").child(
                            if (current > senderId) {
                                current + senderId
                            } else {
                                senderId + current
                            }
                        ).child("message").setValue("kliknuo sam na sebe")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}
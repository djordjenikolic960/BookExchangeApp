package com.example.demoappforfirebase.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demoappforfirebase.Model.Comment
import com.example.demoappforfirebase.Model.User
import com.example.demoappforfirebase.R
import java.lang.StringBuilder

class CommentsAdapter(
    var commentsDataSet: ArrayList<Comment>,
    var usersDataSet: ArrayList<User>
) : RecyclerView.Adapter<CommentsAdapter.CommentsHolder>() {
    inner class CommentsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentUserImage: ImageView = itemView.findViewById(R.id.commentUserImage)
        val commentUserName: TextView = itemView.findViewById(R.id.commentUserName)
        val commentText: TextView = itemView.findViewById(R.id.commentText)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_comment_card, parent, false)
        return CommentsHolder(view)
    }

    override fun onBindViewHolder(holder: CommentsHolder, position: Int) {
        val current = commentsDataSet[position]
        //TODO set user image
        val user = usersDataSet.find { it.id == current.userID }
        holder.commentUserName.text = StringBuilder().append(user?.name).append(" ").append(user?.surname).toString()
        holder.commentText.text = current.comment
    }

    override fun getItemCount(): Int {
        return commentsDataSet.size
    }
}
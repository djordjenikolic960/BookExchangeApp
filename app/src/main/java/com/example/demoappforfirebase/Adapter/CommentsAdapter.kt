package com.example.demoappforfirebase.Adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.ascendik.diary.util.ImageUtil
import com.example.demoappforfirebase.Fragment.UserProfileFragment
import com.example.demoappforfirebase.Model.Comment
import com.example.demoappforfirebase.Model.CommentViewModel
import com.example.demoappforfirebase.Model.User
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.StyleUtil
import java.io.IOException
import java.lang.StringBuilder

class CommentsAdapter(
    var commentsDataSet: ArrayList<Comment>,
    var usersDataSet: ArrayList<User>
) : RecyclerView.Adapter<CommentsAdapter.CommentsHolder>() {
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var commentVM: CommentViewModel

    inner class CommentsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.commentUserImage)
        val userName: TextView = itemView.findViewById(R.id.commentUserName)
        val userNameFirstLetter: TextView = itemView.findViewById(R.id.commentUserNameFirstLetter)
        val userComment: TextView = itemView.findViewById(R.id.comment)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_comment_card, parent, false)
        fragmentHelper = FragmentHelper(parent.context as FragmentActivity)
        commentVM = ViewModelProvider(parent.context as FragmentActivity).get(CommentViewModel::class.java)
        return CommentsHolder(view)
    }

    override fun onBindViewHolder(holder: CommentsHolder, position: Int) {
        val current = commentsDataSet[position]
        val user = usersDataSet.find { it.id == current.userID }
        holder.userName.text = StringBuilder().append(user?.name).append(" ").append(user?.surname).toString()
        holder.userComment.text = current.comment
        if (user?.picture!!.isEmpty()) {
            holder.userNameFirstLetter.text = user.name.first().toString()
            holder.userImage.setBackgroundDrawable(
                StyleUtil.getRoundedShapeDrawable(
                    StyleUtil.getAttributeColor(holder.userImage.context, android.R.attr.textColorPrimary),
                    200f
                )
            )
        } else {
            try {
                val image = ImageUtil.decodeFromFirebaseBase64(user.picture)
                holder.userImage.setImageBitmap(image)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        holder.itemView.setOnClickListener {
            showUserProfileFragment(user)
        }
    }

    override fun getItemCount(): Int {
        return commentsDataSet.size
    }

    private fun showUserProfileFragment(user: User) {
        val bundle = Bundle()
        bundle.putString("userId", user.id)
        commentVM.shouldHideCommentLayout.value = true
        fragmentHelper.replaceFragment(UserProfileFragment::class.java, bundle)
    }
}
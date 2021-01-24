package com.example.demoappforfirebase.Adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.ascendik.diary.util.ImageUtil
import com.example.demoappforfirebase.Fragment.BookMoreDetailsFragment
import com.example.demoappforfirebase.Model.Book
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.BitmapUtil
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import kotlin.collections.ArrayList


class BooksAdapter(private var dataSet: ArrayList<Book>) :
    RecyclerView.Adapter<BooksAdapter.BookHolder>() {
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var preferencesHelper: PreferencesHelper
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    class BookHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val text: TextView = itemView.findViewById(R.id.author)
        val image: ImageView = itemView.findViewById(R.id.image)
        val description: TextView = itemView.findViewById(R.id.shortDescription)
        val like: ImageView = itemView.findViewById(R.id.likeBook)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_book_card, parent, false)
        fragmentHelper = FragmentHelper(parent.context as FragmentActivity)
        preferencesHelper = PreferencesHelper(parent.context)
        return BookHolder(view)
    }

    override fun onBindViewHolder(holder: BookHolder, position: Int) {
        val current = dataSet[position]
        holder.title.text = current.title
        holder.text.text = current.author
        holder.description.text = current.description
        holder.likeCount.text = current.usersThatLiked.size.toString()
        BitmapUtil.updateHeartImageView(holder.like, current.usersThatLiked.contains(preferencesHelper.getUserId()))
        try {
            val image = ImageUtil.decodeFromFirebaseBase64(current.image)
            holder.image.setImageBitmap(image)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        holder.like.setOnClickListener {
            if (!current.usersThatLiked.contains(preferencesHelper.getUserId())) {
                current.usersThatLiked.add(preferencesHelper.getUserId())
            } else {
                current.usersThatLiked.remove(preferencesHelper.getUserId())
            }
            BitmapUtil.updateHeartImageView(holder.like, current.usersThatLiked.contains(preferencesHelper.getUserId()))
            database.child("Books").child(current.bookId).child("usersThatLiked").setValue(current.usersThatLiked)
        }
        holder.itemView.setOnClickListener {
            showBookDetailsFragment(current)
        }
    }

    fun updateDataSet(newDataSet: ArrayList<Book>) {
        dataSet = newDataSet
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    private fun showBookDetailsFragment(book: Book) {
        val bundle = Bundle()
        bundle.putString("bookId", book.bookId)
        fragmentHelper.replaceFragment(BookMoreDetailsFragment::class.java, bundle)
    }
}
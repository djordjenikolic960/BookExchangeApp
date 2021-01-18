package com.example.demoappforfirebase.Adapter

import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.ascendik.diary.util.ImageUtil
import com.example.demoappforfirebase.Fragment.BookMoreDetailsFragment
import com.example.demoappforfirebase.Model.Book
import com.example.demoappforfirebase.R
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
        val booksDivider: View = itemView.findViewById(R.id.booksDivider)
        val likeBook: ImageView = itemView.findViewById(R.id.likeBook)
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
        if (holder.adapterPosition == itemCount - 1) {
            holder.booksDivider.visibility = View.GONE
        }
        if (current.usersThatLiked.contains(preferencesHelper.getUserId())) {
            likeBook(holder)
        }
        try {
            val image = ImageUtil.decodeFromFirebaseBase64(current.image)
            holder.image.setImageBitmap(image)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        holder.likeBook.setOnClickListener {
            if (!current.usersThatLiked.contains(preferencesHelper.getUserId())) {
                likeBook(holder)
                current.usersThatLiked.add(PreferencesHelper(holder.itemView.context).getUserId())
            } else {
                unlikeBook(holder)
                current.usersThatLiked.remove(PreferencesHelper(holder.itemView.context).getUserId())
            }
            database.child("Books").child(current.bookId).child("usersThatLiked").setValue(current.usersThatLiked)
        }
        holder.itemView.setOnClickListener {
            showBookDetailsFragment(current)
        }
    }

    private fun unlikeBook(holder: BookHolder) {
        holder.likeBook.setImageDrawable(AppCompatResources.getDrawable(holder.itemView.context, R.drawable.ic_heart))
    }

    private fun likeBook(holder: BookHolder) {
        val unwrappedDrawable = AppCompatResources.getDrawable(holder.itemView.context, R.drawable.ic_heart_full)
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!).mutate()
        DrawableCompat.setTint(wrappedDrawable, Color.RED)
        holder.likeBook.setImageDrawable(wrappedDrawable)
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
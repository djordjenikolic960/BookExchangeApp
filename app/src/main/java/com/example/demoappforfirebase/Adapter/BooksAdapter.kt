package com.example.demoappforfirebase.Adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
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
import com.example.demoappforfirebase.Utils.FragmentHelper
import java.io.IOException
import kotlin.collections.ArrayList


class BooksAdapter(private val dataSet: ArrayList<Book>) :
    RecyclerView.Adapter<BooksAdapter.BookHolder>() {
    private lateinit var fragmentHelper: FragmentHelper
    class BookHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val text: TextView = itemView.findViewById(R.id.author)
        val image: ImageView = itemView.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_book_card, parent, false)
        fragmentHelper = FragmentHelper(parent.context as FragmentActivity)
        return BookHolder(view)
    }

    override fun onBindViewHolder(holder: BookHolder, position: Int) {
       val current = dataSet[position]
        holder.title.text = current.title
        holder.text.text = current.author
        try {
            val image = ImageUtil.decodeFromFirebaseBase64(current.image)
            holder.image.setImageBitmap(image)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        holder.itemView.setOnClickListener {
            showNoteFragment(current)
        }

    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    private fun showNoteFragment(book: Book) {
        val bundle = Bundle()
        bundle.putString("bookId", book.bookId)
        fragmentHelper.replaceFragment(BookMoreDetailsFragment::class.java, bundle)
    }
}
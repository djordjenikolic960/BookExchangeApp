package com.example.demoappforfirebase.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.demoappforfirebase.Model.BookViewModel
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.StyleUtil

class CategoriesAdapter :
    RecyclerView.Adapter<CategoriesAdapter.CategoriesHolder>() {
    private lateinit var fragmentHelper: FragmentHelper
    var checkedCategories = arrayListOf<Int>()
    private lateinit var bookVM: BookViewModel

    class CategoriesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoriesText: TextView = itemView.findViewById(R.id.categoriesText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_popup_categorie, parent, false)
        fragmentHelper = FragmentHelper(parent.context as FragmentActivity)
        bookVM = ViewModelProvider(parent.context as FragmentActivity).get(BookViewModel::class.java)
        return CategoriesHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {
        holder.categoriesText.text = holder.itemView.resources.getStringArray(R.array.categories)[holder.adapterPosition]
        updateViewHolder(checkedCategories.contains(holder.adapterPosition), holder)
        holder.itemView.setOnClickListener {
            if (checkedCategories.contains(holder.adapterPosition)) {
                checkedCategories.remove(holder.adapterPosition)
            } else {
                checkedCategories.add(holder.adapterPosition)
            }
            updateViewHolder(checkedCategories.contains(holder.adapterPosition), holder)
            bookVM.currentCategories.value = checkedCategories
        }
    }

    override fun getItemCount(): Int {
        return 10
    }

    private fun updateViewHolder(update: Boolean, holder: CategoriesHolder) {
        holder.categoriesText.setBackgroundDrawable(StyleUtil.getDrawableForCategories(holder.adapterPosition, holder.itemView.context, update))
        if (update) {
            holder.categoriesText.setTextColor(StyleUtil.getAttributeColor(holder.categoriesText.context, R.attr.colorSurface))
        } else {
            holder.categoriesText.setTextColor(StyleUtil.getAttributeColor(holder.categoriesText.context, android.R.attr.textColorPrimary))
        }
    }

}
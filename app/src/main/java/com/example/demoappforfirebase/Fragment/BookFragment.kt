package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.ascendik.diary.util.ImageUtil
import com.example.demoappforfirebase.MainActivity
import com.example.demoappforfirebase.Model.Book
import com.example.demoappforfirebase.Model.BookViewModel
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.example.demoappforfirebase.Utils.StyleUtil
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_book.*
import java.util.*

class BookFragment : BaseFragment() {
    private lateinit var database: DatabaseReference
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var bookVM: BookViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_book, container, false)
    }

    override fun onBackPressed() {
        fragmentHelper.replaceFragment(BookListFragment::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fragmentHelper = FragmentHelper(requireActivity())
        preferencesHelper = PreferencesHelper(requireContext())
        bookVM = ViewModelProvider(requireActivity()).get(BookViewModel::class.java)
        database = FirebaseDatabase.getInstance().reference
        for (index in 0 until categoryTags.childCount) {
            val child = categoryTags.getChildAt(index)
            (child as TextView).text = requireContext().resources.getStringArray(R.array.categories)[index]
            if (bookVM.categoriesSelected.contains(index)) {
                child.setBackgroundDrawable(StyleUtil.getDrawableForCategories(index, requireContext(), true))
            } else {
                child.setBackgroundDrawable(StyleUtil.getDrawableForCategories(index, requireContext(), false))
            }
            child.setOnClickListener {
                if (bookVM.categoriesSelected.contains(index)) {
                    bookVM.categoriesSelected.remove(index)
                    it.setBackgroundDrawable(StyleUtil.getDrawableForCategories(index, requireContext(), false))
                    child.setTextColor(StyleUtil.getAttributeColor(requireContext(), android.R.attr.textColorPrimary))
                } else {
                    bookVM.categoriesSelected.add(index)
                    it.setBackgroundDrawable(StyleUtil.getDrawableForCategories(index, requireContext(), true))
                    child.setTextColor(StyleUtil.getAttributeColor(requireContext(), R.attr.colorSurface))
                }
            }
        }
        bookImage.setOnClickListener {
            ImageUtil.onLaunchCamera(requireActivity() as MainActivity)
        }
        btnWrite.setBackgroundDrawable(StyleUtil.getRoundedShapeDrawable(StyleUtil.getAttributeColor(requireContext(), R.attr.colorControlActivated), 20f))
        btnWrite.setOnClickListener {
            val generatedId: String = database.push().key!!
            database.child("Books").child(generatedId)
                .setValue(
                    Book(
                        generatedId,
                        preferencesHelper.getUserId(),
                        bookName.editableText.toString(),
                        bookAuthor.editableText.toString(),
                        bookVM.imageUrl ?: "",
                        bookDescription.editableText.toString(),
                        bookVM.categoriesSelected,
                        Date().time
                    )
                )
            preferencesHelper.setIndex(preferencesHelper.getIndex() + 1)
            bookVM.categoriesSelected = arrayListOf()
            fragmentHelper.replaceFragment(BookListFragment::class.java)
            bookVM.imageUrl = ""
        }
    }
}
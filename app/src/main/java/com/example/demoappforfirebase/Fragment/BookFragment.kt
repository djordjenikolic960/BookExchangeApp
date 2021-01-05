package com.example.demoappforfirebase.Fragment

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ascendik.diary.util.ImageUtil
import com.ascendik.diary.util.ImageUtil.REQUEST_GALLERY_PHOTO
import com.example.demoappforfirebase.MainActivity
import com.example.demoappforfirebase.Model.Book
import com.example.demoappforfirebase.Model.BookViewModel
import com.example.demoappforfirebase.Model.Categories
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_book.*

class BookFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var bookVM: BookViewModel
    private lateinit var packageManager: PackageManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_book, container, false)
        packageManager = (requireActivity() as MainActivity).packageManager
        return view

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fragmentHelper = FragmentHelper(requireActivity())
        preferencesHelper = PreferencesHelper(requireContext())
        bookVM = ViewModelProvider(requireActivity()).get(BookViewModel::class.java)
        database = FirebaseDatabase.getInstance().reference
        bookImage.setOnClickListener {
            ImageUtil.onLaunchCamera(requireActivity() as MainActivity)
        }
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
                        arrayListOf(Categories.CLASSICS.ordinal, Categories.FANTASY.ordinal)
                    )
                )
            preferencesHelper.setIndex(preferencesHelper.getIndex() + 1)
            fragmentHelper.replaceFragment(BookListFragment::class.java)
            bookVM.imageUrl = ""
        }
    }
}
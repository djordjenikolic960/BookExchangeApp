package com.example.demoappforfirebase.Fragment

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ascendik.diary.util.ImageUtil
import com.example.demoappforfirebase.MainActivity
import com.example.demoappforfirebase.Model.Book
import com.example.demoappforfirebase.Model.BookViewModel
import com.example.demoappforfirebase.Model.Categories
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_book.*

class BookFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var bookVM: BookViewModel
    private lateinit var packageManager :PackageManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =layoutInflater.inflate(R.layout.fragment_book, container, false)
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
            onLaunchCamera()
        }
        btnWrite.setOnClickListener {
            val generatedId: String = database.push().key!!
            database.child("Books").child(generatedId)
                .setValue(
                    Book(
                        generatedId,
                        Firebase.auth.uid.toString(),
                        bookName.editableText.toString(),
                        bookAuthor.editableText.toString(),
                        bookVM.imageUrl.value ?: "",
                        bookDescription.editableText.toString(),
                        arrayListOf(Categories.CLASSICS.ordinal, Categories.FANTASY.ordinal)
                    )
                )
            preferencesHelper.setIndex(preferencesHelper.getIndex() + 1)
            fragmentHelper.replaceFragment(BookListFragment::class.java)
            bookVM.imageUrl.value = ""
        }
        bookVM.imageUrl.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                val bmOptions = BitmapFactory.Options()
                val bitmap = BitmapFactory.decodeFile(ImageUtil.pathForImage, bmOptions)
                bookImage.setImageBitmap(bitmap)
            }
        })
    }

    private fun onLaunchCamera() {
        ImageUtil.dispatchTakePictureIntent(requireActivity() as MainActivity)
    }
}
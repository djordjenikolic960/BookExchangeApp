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
            onLaunchCamera()
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
                        bookVM.imageUrl.value ?: "",
                        bookDescription.editableText.toString(),
                        arrayListOf(Categories.CLASSICS.ordinal, Categories.FANTASY.ordinal)
                    )
                )
            preferencesHelper.setIndex(preferencesHelper.getIndex() + 1)
            fragmentHelper.replaceFragment(BookListFragment::class.java)
            bookVM.imageUrl.value = ""
        }
    }

    private fun onLaunchCamera() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        val  builder =  AlertDialog.Builder(context)
        builder.setTitle("Choose your profile picture")

        builder.setItems(options) { dialog, which ->
            when {
                options[which] == "Take Photo" -> {
                    ImageUtil.dispatchTakePictureIntent(requireActivity() as MainActivity)
                }
                options[which] == "Choose from Gallery" -> {
                    val getIntent = Intent(Intent.ACTION_GET_CONTENT)
                    getIntent.type = "image/*"
                    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    pickIntent.type = "image/*"
                    val chooserIntent = Intent.createChooser(getIntent, "Select Image")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
                    startActivityForResult(chooserIntent, REQUEST_GALLERY_PHOTO)
                }
                options[which] == "Cancel" -> {
                    dialog!!.dismiss()
                }
            }
        };
        builder.show();
    }
}
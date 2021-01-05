package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ascendik.diary.util.ImageUtil
import com.example.demoappforfirebase.MainActivity
import com.example.demoappforfirebase.Model.User
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_user_profile.*
import java.lang.StringBuilder

class UserProfileFragment : BaseFragment() {
    private lateinit var database: DatabaseReference
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var user:User

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onBackPressed() {
        FragmentHelper(requireActivity()).replaceFragment(BookListFragment::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHelpers()
        val userQuery = database.child("Users").child(preferencesHelper.getUserId())
        userQuery.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)!!


                if(user.picture.isNotEmpty()){
                    profileImage.setImageBitmap(ImageUtil.decodeFromFirebaseBase64(user.picture))
                }
                val userName = StringBuilder().append(user.name).append(" ").append(user.surname).toString()
                profileName.text = userName
            }

            override fun onCancelled(error: DatabaseError) { }

        })

        profileImage.setOnClickListener {
            ImageUtil.onLaunchCamera(requireActivity() as MainActivity)
        }
    }

    private fun setHelpers() {
        database =  FirebaseDatabase.getInstance().reference
        preferencesHelper = PreferencesHelper(requireContext())
    }
}
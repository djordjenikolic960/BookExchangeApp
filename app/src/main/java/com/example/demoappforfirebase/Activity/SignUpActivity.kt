package com.example.demoappforfirebase.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.demoappforfirebase.Fragment.SignInFragment
import com.example.demoappforfirebase.Fragment.SignUpFragment
import com.example.demoappforfirebase.Fragment.WelcomeFragment
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var fragmentHelper: FragmentHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_content_sign)
        auth = Firebase.auth
        fragmentHelper = FragmentHelper(this)
        fragmentHelper.replaceFragment(WelcomeFragment::class.java)
    }

    override fun onBackPressed() {
        when {
            fragmentHelper.isFragmentVisible(SignInFragment::class.java) -> fragmentHelper.replaceFragment(WelcomeFragment::class.java)
            fragmentHelper.isFragmentVisible(SignUpFragment::class.java) -> fragmentHelper.replaceFragment(WelcomeFragment::class.java)
            else -> super.onBackPressed()
        }
    }
}
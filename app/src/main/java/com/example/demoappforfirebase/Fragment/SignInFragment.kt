package com.example.demoappforfirebase.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.demoappforfirebase.MainActivity
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_sign_in.*

class SignInFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        auth = Firebase.auth

        signInLayout.setOnClickListener {
            when {
                email.text.isNullOrBlank() -> {
                 Toast.makeText(requireContext(), "You must enter your email", Toast.LENGTH_SHORT).show()
                }
                password.text.isNullOrBlank() -> {
                    Toast.makeText(requireContext(), "You must enter your password", Toast.LENGTH_SHORT).show()
                }
                password.text.length < 8 -> {
                    Toast.makeText(requireContext(), "You password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    auth.signInWithEmailAndPassword(
                        email.text.toString(),
                        password.text.toString()
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            PreferencesHelper(requireContext()).setUserId(auth.currentUser!!.uid)
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            activity?.finish()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "You password is wrong", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
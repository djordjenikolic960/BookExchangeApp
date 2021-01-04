package com.example.demoappforfirebase.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.demoappforfirebase.MainActivity
import com.example.demoappforfirebase.Model.User
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_sign_up.*

class SignUpFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        auth = Firebase.auth
        signUpLayout.setOnClickListener {
            when {
                firstName.text.isNullOrEmpty() -> {
                    Toast.makeText(requireContext(), "You must enter your first name", Toast.LENGTH_SHORT).show()
                }
                lastName.text.isNullOrEmpty() -> {
                    Toast.makeText(requireContext(), "You must enter your last name", Toast.LENGTH_SHORT).show()
                }
                email.text.isNullOrEmpty() -> {
                    Toast.makeText(requireContext(), "You must enter your email", Toast.LENGTH_SHORT).show()
                }
                password.text.isNullOrEmpty() -> {
                    Toast.makeText(requireContext(), "You must enter your password", Toast.LENGTH_SHORT).show()
                }
                password.text.length < 8 -> {
                    Toast.makeText(requireContext(), "You password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    auth.createUserWithEmailAndPassword(
                        email.text.toString(),
                        password.text.toString()
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val user: FirebaseUser = auth.currentUser!!
                            addUserInDatabase(user)
                            PreferencesHelper(requireContext()).setUserId(auth.currentUser!!.uid)
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            activity?.finish()
                        }
                    }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    private fun addUserInDatabase(user: FirebaseUser) {
        FirebaseDatabase.getInstance().reference.child("Users")
            .child(user.uid).setValue(
                User(
                    user.uid,
                    firstName.text.toString(),
                    lastName.text.toString(),
                    email.text.toString(),
                    password.text.toString()
                )
            )
    }
}
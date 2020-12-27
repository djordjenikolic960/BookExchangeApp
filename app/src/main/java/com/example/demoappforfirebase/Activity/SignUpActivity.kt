package com.example.demoappforfirebase.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.demoappforfirebase.MainActivity
import com.example.demoappforfirebase.Model.User
import com.example.demoappforfirebase.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase


class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        auth = Firebase.auth

        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            auth.createUserWithEmailAndPassword(
                findViewById<EditText>(R.id.email).text.toString(),
                findViewById<EditText>(R.id.password).text.toString()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    val user: FirebaseUser = auth.currentUser!!
                    addUserInDatabase(user)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
            findViewById<Button>(R.id.btnSignIn).setOnClickListener {
                auth.signInWithEmailAndPassword(
                    findViewById<EditText>(R.id.email).text.toString(),
                    findViewById<EditText>(R.id.password).text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserInDatabase(user: FirebaseUser) {
        FirebaseDatabase.getInstance().reference.child("Users")
            .child(user.uid).setValue(
                User(
                    user.uid,
                    findViewById<EditText>(R.id.name).text.toString(),
                    findViewById<EditText>(R.id.surname).text.toString(),
                    findViewById<EditText>(R.id.email).text.toString(),
                    findViewById<EditText>(R.id.password).text.toString()
                )
            )
    }
}
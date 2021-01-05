package com.example.demoappforfirebase.Fragment

import android.content.Context
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    val FIREBASE_ANALYTICS = "database_error"
    private var backPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            backPressedCallback
        )
    }

    open fun onBackPressed() {}
}
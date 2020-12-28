package com.example.demoappforfirebase.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.demoappforfirebase.R
import com.example.demoappforfirebase.Utils.FragmentHelper
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment : Fragment() {
    private lateinit var fragmentHelper: FragmentHelper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fragmentHelper = FragmentHelper(requireActivity())
        signIn.setOnClickListener {
            fragmentHelper.replaceFragment(SignInFragment::class.java)
        }

        signUp.setOnClickListener {
            fragmentHelper.replaceFragment(SignUpFragment::class.java)
        }
    }
}
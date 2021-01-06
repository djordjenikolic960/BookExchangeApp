package com.example.demoappforfirebase.Utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.demoappforfirebase.Fragment.BookListFragment
import com.example.demoappforfirebase.R
import java.lang.ref.WeakReference

class FragmentHelper(activity: FragmentActivity) {
    private var activity = WeakReference(activity)

    fun initFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            replaceFragment(BookListFragment::class.java)
        }
    }

    fun replaceFragment(fragmentClass: Class<out Fragment>) {
        replaceFragment(fragmentClass, null)
    }

    fun replaceFragment(fragmentClass: Class<out Fragment>, bundle: Bundle?) {
        val fragmentTransaction = activity.get()!!.supportFragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(
            android.R.anim.slide_in_left, android.R.anim.slide_out_right,
            android.R.anim.slide_in_left, android.R.anim.slide_out_right
        )
        try {
            val fragment = fragmentClass.newInstance()
            bundle.let { fragment.arguments = bundle }
            fragmentTransaction.replace(R.id.fragment_container, fragment, fragmentClass.name)
                .commitNow()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        activity.get()!!.invalidateOptionsMenu()
    }

    fun isFragmentVisible(fragmentClass: Class<out Fragment>): Boolean {
        return activity.get()!!.supportFragmentManager.findFragmentByTag(fragmentClass.name) != null
                && (activity.get()!!.supportFragmentManager.findFragmentByTag(fragmentClass.name) as Fragment).isVisible
    }

    fun getVisibleFragment(): Fragment? {
        for (fragment in activity.get()!!.supportFragmentManager.fragments) {
            if (fragment != null && fragment.isVisible) {
                return fragment
            }
        }
        return null
    }
}
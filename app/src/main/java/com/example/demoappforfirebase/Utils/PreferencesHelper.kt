package com.example.demoappforfirebase.Utils

import android.content.Context
import android.preference.PreferenceManager

class PreferencesHelper(context: Context) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun setIndex(index: Int) {
        preferences.edit().putInt("index", index).apply()
    }

    fun getIndex(): Int {
        return preferences.getInt("index", 0)
    }
}
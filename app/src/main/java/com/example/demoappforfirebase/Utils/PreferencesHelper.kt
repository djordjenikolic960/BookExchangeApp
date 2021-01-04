package com.example.demoappforfirebase.Utils

import android.content.Context
import android.preference.PreferenceManager

class PreferencesHelper(context: Context) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    /// User Sessions
    fun setUserId(uid :String){
        preferences.edit().putString("activeUserId", uid).apply()
    }

    fun getUserId(): String {
        return preferences.getString("activeUserId", "").toString()
    }

    /// User Sessions


    fun setIndex(index: Int) {
        preferences.edit().putInt("index", index).apply()
    }

    fun getIndex(): Int {
        return preferences.getInt("index", 0)
    }
}
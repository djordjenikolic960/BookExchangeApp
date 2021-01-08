package com.example.demoappforfirebase.Utils

import android.content.Context
import android.preference.PreferenceManager
import com.example.demoappforfirebase.MainActivity

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

    ///////////////////////
    // SORT \\
    //////////////////////
    fun setSortType(sortType: Int) {
        preferences.edit().putInt("sort_type", sortType).apply()
    }

    fun getSortType(): Int {
        return preferences.getInt("sort_type", MainActivity.SortType.NEWER_FIRST.ordinal)
    }
}
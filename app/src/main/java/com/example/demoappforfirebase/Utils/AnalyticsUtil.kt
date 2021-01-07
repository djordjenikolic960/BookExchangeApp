package com.example.demoappforfirebase.Utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsUtil {
    val ERROR_EVENT = "database_error"
    val ERROR_VALUE = "error"

    @JvmStatic
    fun logError(context: Context, error: String) {
        val params = Bundle()
        params.putString(ERROR_VALUE, error)
        FirebaseAnalytics.getInstance(context).logEvent(ERROR_EVENT, params)
    }
}
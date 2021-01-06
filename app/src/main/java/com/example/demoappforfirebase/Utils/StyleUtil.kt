package com.example.demoappforfirebase.Utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.example.demoappforfirebase.R
import kotlinx.android.synthetic.main.view_content_main.*

object StyleUtil {
    fun stylize(activity: Activity) {
        stylizeNavigationBar(activity)
        stylizeStatusBarElements(activity)
    }

    private fun setScrollColor(context: Context, id: Int) {
        if (id != 0) {
            val androidGlow = ContextCompat.getDrawable(context, id)
            androidGlow!!.setColorFilter(
                getAttributeColor(context, R.attr.colorPrimary),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun stylizeNavigationBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.window.decorView.systemUiVisibility = (activity.window.decorView.systemUiVisibility
                        or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
            }
        }
    }

    private fun stylizeStatusBarElements(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility = 0
        }
    }

    fun stylizeStatusBar(activity: Activity, transparent: Boolean) {
        if (Build.VERSION.SDK_INT >= 21) {
            if (transparent) {
                val layoutParams: CoordinatorLayout.LayoutParams = activity.appBar.layoutParams as CoordinatorLayout.LayoutParams
                layoutParams.topMargin = 0
                activity.window.statusBarColor = Color.TRANSPARENT
                activity.window.decorView.systemUiVisibility =
                    (activity.window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            } else {
                val layoutParams: CoordinatorLayout.LayoutParams = activity.appBar.layoutParams as CoordinatorLayout.LayoutParams
                layoutParams.topMargin = -getStatusBarHeight(activity.resources)
                activity.window.statusBarColor = getAttributeColor(activity, R.attr.colorPrimary)
                activity.window.decorView.systemUiVisibility =
                    (activity.window.decorView.systemUiVisibility and (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN).inv())
            }
        }
    }

    fun getStatusBarHeight(resources: Resources): Int {
        var statusBarHeight = 0
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

    @JvmStatic
    fun getAttributeColor(context: Context, @AttrRes resId: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(resId, typedValue, true)
        return typedValue.data
    }

    @JvmStatic
    fun getAttributeFloat(context: Context, @AttrRes resId: Int): Float {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(resId, typedValue, true)
        return typedValue.float
    }

    fun getDrawableForCategories(position: Int, context: Context, shouldFill: Boolean): Drawable {
        val optionsColor = context.resources.obtainTypedArray(R.array.categories_colors)
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = 200f
        if (shouldFill) {
            drawable.setColor(context.resources.getColor(optionsColor.getResourceId(position, -1)))
        } else {
            drawable.setStroke(3, context.resources.getColor(optionsColor.getResourceId(position, -1)))
            drawable.setColor(getAttributeColor(context, android.R.attr.itemBackground))
        }
        val rippleStateList = ColorStateList.valueOf(context.resources.getColor(optionsColor.getResourceId(position, -1)))
        optionsColor.recycle()
        return RippleDrawable(rippleStateList, drawable, drawable)
    }

    fun getRoundedShapeDrawable(color: Int, radius: Float): Drawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = radius
        drawable.setColor(color)
        return drawable
    }
}

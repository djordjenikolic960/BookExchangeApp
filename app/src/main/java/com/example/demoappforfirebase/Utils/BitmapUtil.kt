package com.example.demoappforfirebase.Utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import com.example.demoappforfirebase.R

object BitmapUtil {
    fun getMessageIndicator(mContext: Context, showIndicator: Boolean): Drawable {
        val inflater = LayoutInflater.from(mContext)
        val nullParent: ViewGroup? = null
        val view: View = inflater.inflate(R.layout.view_new_message_indicator, nullParent)
        view.findViewById<View>(R.id.counterValuePanel).isVisible = showIndicator
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val background = view.background

        background?.draw(canvas)
        view.draw(canvas)

        return BitmapDrawable(mContext.resources, bitmap)
    }

    fun updateHeartImageView(imageView: ImageView, update: Boolean) {
        if (update) {
            val unwrappedDrawable = AppCompatResources.getDrawable(imageView.context, R.drawable.ic_heart_full)
            val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!).mutate()
            DrawableCompat.setTint(wrappedDrawable, Color.RED)
            imageView.setImageDrawable(wrappedDrawable)
        } else {
            imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.context, R.drawable.ic_heart))
        }
    }
}
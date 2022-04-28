package com.zqy.hci.theme

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

class DrawableBuilder private constructor(theme: UITheme, drawableResId: Int) {
    private val mDrawableResourceId: Int
    private val mTheme: UITheme
    private var mDrawable: Drawable? = null

    fun buildDrawable(): Drawable? {
        if (mDrawable != null) return mDrawable
        val packageContext: Context = mTheme.context
        mDrawable = ContextCompat.getDrawable(packageContext, mDrawableResourceId)
        return mDrawable
    }

    companion object {
        fun build(theme: UITheme, a: TypedArray, index: Int): DrawableBuilder {
            val resId = a.getResourceId(index, 0)
            require(resId != 0) { "No resource ID was found at index $index" }
            return DrawableBuilder(theme, resId)
        }
    }

    init {
        mTheme = theme
        mDrawableResourceId = drawableResId
    }
}

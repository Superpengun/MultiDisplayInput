package com.zqy.hci.utils

import android.content.Context
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import java.lang.IllegalArgumentException

object LayoutUtils {

    fun updateLayoutHeightOf(window: Window?, layoutHeight: Int) {
        val params = window?.attributes
        if (params != null && params.height != layoutHeight) {
            params.height = layoutHeight
            window.attributes = params
        }
    }

    fun updateLayoutHeightOf(view: View, layoutHeight: Int) {
        val params = view.layoutParams
        if (params != null && params.height != layoutHeight) {
            params.height = layoutHeight
            view.layoutParams = params
        }
    }

    fun updateLayoutGravityOf(view: View, layoutGravity: Int) {
        val lp = view.layoutParams
        if (lp is LinearLayout.LayoutParams) {
            val params = lp
            if (params.gravity != layoutGravity) {
                params.gravity = layoutGravity
                view.layoutParams = params
            }
        } else if (lp is FrameLayout.LayoutParams) {
            val params = lp
            if (params.gravity != layoutGravity) {
                params.gravity = layoutGravity
                view.layoutParams = params
            }
        } else {
            throw IllegalArgumentException(
                "Layout parameter doesn't have gravity: " + lp.javaClass.name
            )
        }
    }


    fun dip2px(context: Context, dpValue:Float): Float{
        val scale = context.resources.displayMetrics.density
        return dpValue * scale + 0.5f
    }

    fun px2dip(context: Context, pxValue:Float):Float{
        val scale = context.resources.displayMetrics.density
        return pxValue / scale + 0.5f
    }


    fun getScreenWidth(context: Context):Int{
        val vm = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return vm.defaultDisplay.width
    }
}
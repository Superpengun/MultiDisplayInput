package com.zqy.hci.keyboard

import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.TypedValue
import com.orhanobut.logger.Logger
import java.lang.NumberFormatException
import java.util.*


object KeyboardSupport {
    private const val TAG = "ASKKbdSupport"
    private fun parseCSV(value: String): IntArray {
        var count = 0
        var lastIndex = 0
        if (value.length > 0) {
            count++
            while (value.indexOf(",", lastIndex + 1).also { lastIndex = it } > 0) {
                count++
            }
        }
        val values = IntArray(count)
        count = 0
        val st = StringTokenizer(value, ",")
        while (st.hasMoreTokens()) {
            val nextToken = st.nextToken()
            try {
                // Issue 395
                // default behavior
                if (nextToken.length != 1) {
                    values[count++] = nextToken.toInt()
                } else {
                    // length == 1, assume a char!
                    values[count++] = nextToken[0].code
                }
            } catch (nfe: NumberFormatException) {
                Logger.e(TAG, "Error parsing keycodes $value")
            }
        }
        return values
    }

    fun updateDrawableBounds(icon: Drawable?) {
        if (icon == null) return
        icon.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
    }

    private val codesValue = TypedValue()
    fun getKeyCodesFromTypedArray(typedArray: TypedArray, index: Int): IntArray {
        typedArray.getValue(index, codesValue)
        return if (codesValue.type == TypedValue.TYPE_INT_DEC
            || codesValue.type == TypedValue.TYPE_INT_HEX
        ) {
            intArrayOf(codesValue.data)
        } else if (codesValue.type == TypedValue.TYPE_STRING) {
            parseCSV(codesValue.coerceToString().toString())
        } else {
            Logger.w(TAG, "Unknown mCodes values!")
            IntArray(0)
        }
    }

    private fun zoomFactorLimitation(value: Float): Float {
        if (value > 2.0f) return 2.0f
        return if (value < 0.2f) 0.2f else value
    }
}
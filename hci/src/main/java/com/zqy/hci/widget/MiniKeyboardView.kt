package com.zqy.hci.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.zqy.hci.R
import com.zqy.hci.theme.ThemeAbleView
import com.zqy.hci.theme.UITheme

/**
 * @Author:CWQ
 * @DATE:2022/1/20
 * @DESC:
 */
class MiniKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ThemeAbleView {
    private val TAG = MiniKeyboardView::class.java.simpleName

    private var mLocationX = 0
    private var mLocationY = 0
    private var mPopupParent: KeyboardView? = null
    private var mDefaultActiveIndex = 0
    private var mLastActiveIndex = 0
    private var keyPopWidth = 0f
    private var keyPopHeight = 0f
    private var keyTextColor = 0
    private var keyTextSize = 0f
    private var keyPopMargin = 0f
    private lateinit var keyPopBg: Drawable
    private var mInflate: LayoutInflater

    init {
        mInflate = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }


    override fun setUITheme(uiTheme: UITheme) {
        val typeArray = uiTheme.context.applicationContext.obtainStyledAttributes(
            uiTheme.themeResId,
            R.styleable.HciKeyboardViewTheme
        )
        keyPopWidth = typeArray.getDimension(R.styleable.HciKeyboardViewTheme_key_pop_width, 0f)
        keyPopHeight = typeArray.getDimension(R.styleable.HciKeyboardViewTheme_key_pop_height, 0f)
        keyTextColor =
            typeArray.getColor(R.styleable.HciKeyboardViewTheme_key_text_color, Color.WHITE)
        keyTextSize =
            typeArray.getDimension(R.styleable.HciKeyboardViewTheme_key_pop_font_size, 10f)
        keyPopMargin = typeArray.getDimension(R.styleable.HciKeyboardViewTheme_key_pop_margin, 8f)
        typeArray.recycle()

        val iconTypeArray = uiTheme.context.applicationContext.obtainStyledAttributes(
            uiTheme.iconThemeResId,
            R.styleable.HciKeyboardFunctionIconTheme
        )
        keyPopBg = iconTypeArray.getDrawable(R.styleable.HciKeyboardFunctionIconTheme_key_pop_bg)!!
        iconTypeArray.recycle()
    }


    fun setMiniKey(miniKey: CharSequence) {
        removeAllViews()
        for (index in miniKey.indices) {
            val keyLabel = miniKey[index]
            val key = mInflate.inflate(R.layout.key_pop, null) as TextView
            key.setText(keyLabel.toString())
            addView(key)
            val params = key.layoutParams as LinearLayout.LayoutParams
            params.width = keyPopWidth.toInt()
            params.height = keyPopHeight.toInt()
            params.setMargins(
                keyPopMargin.toInt(),
                keyPopMargin.toInt(),
                keyPopMargin.toInt(),
                keyPopMargin.toInt()
            )
            key.layoutParams = params
        }
        mDefaultActiveIndex = if (mDefaultActiveIndex > miniKey.length) {
            0
        } else {
            mDefaultActiveIndex
        }
        getChildAt(mDefaultActiveIndex).isActivated = true
        mLastActiveIndex = mDefaultActiveIndex
        requestLayout()
    }

    fun setMiniMiniKey(miniKey: CharSequence) {
        removeAllViews()
        for (index in miniKey.indices) {
            val keyLabel = miniKey[index]
            val key = mInflate.inflate(R.layout.key_pop, null) as TextView
            key.setText(keyLabel.toString())
            addView(key)
            val params = key.layoutParams as LinearLayout.LayoutParams
            params.width = (keyPopWidth * 6 / 8).toInt()
            params.height = (keyPopHeight * 6 / 8).toInt()
            val margin = (keyPopMargin * 6 / 8).toInt()
            params.setMargins(margin, margin, margin, margin)
            key.layoutParams = params
            if (keyLabel in '0'..'9') {
                mDefaultActiveIndex = index
            }
        }
        getChildAt(mDefaultActiveIndex).isActivated = true
        mLastActiveIndex = mDefaultActiveIndex
        requestLayout()
    }

    fun setDefaultActiveIndex(index: Int) {
        mDefaultActiveIndex = index
    }

    fun setPopupParent(parent: KeyboardView) {
        mPopupParent = parent
    }

    fun setPopupOffset(x: Int, y: Int) {
        mLocationX = x
        mLocationY = y
    }

    fun swipeTo(index: Int) {
        if (mDefaultActiveIndex + index < 0 || mDefaultActiveIndex + index >= childCount) {
            return
        }
        getChildAt(mLastActiveIndex).isActivated = false
        getChildAt(mDefaultActiveIndex + index).isActivated = true
        mLastActiveIndex = mDefaultActiveIndex + index
    }

    fun getCurrentLongPressChar(): String {
//        return (getChildAt(mLastActiveIndex) as TextView).text[0]
        return (getChildAt(mLastActiveIndex) as TextView).text.toString()
    }

    fun clearActive() {
        getChildAt(mLastActiveIndex).isActivated = false
        getChildAt(mDefaultActiveIndex).isActivated = true
        mLastActiveIndex = mDefaultActiveIndex
    }
}
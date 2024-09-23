package com.zqy.hci.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.zqy.hci.R
import com.zqy.hci.theme.ThemeAbleView
import com.zqy.hci.theme.UITheme

class ComposingView(
    private val mContext: Context
) : PopupWindow(), ThemeAbleView {

    private var composingText: TextView

    init {
        val resId: Int = R.layout.composing_pop
        contentView = LayoutInflater.from(mContext).inflate(resId, null)
        composingText = contentView.findViewById(R.id.composingtext)
        isOutsideTouchable = false
        isTouchable = false
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = mContext.resources.getDimension(R.dimen.composing_height).toInt()
        composingText.textSize = mContext.resources.getDimension(R.dimen.composing_text_size)
        val padding = mContext.resources.getDimension(R.dimen.composing_text_paddingbottom).toInt()
        composingText.setPadding(10, padding, 15, padding)
        animationStyle = 0
    }

    fun updateComposingText(str: String) {
        composingText.text = str
        width = composingText.maxWidth
        update()
    }

    override fun setUITheme(uiTheme: UITheme) {
        composingText.setBackgroundColor(mContext.getColor(R.color.composing_bg))
        composingText.setTextColor(mContext.getColor(R.color.composing_text_color))
    }
}
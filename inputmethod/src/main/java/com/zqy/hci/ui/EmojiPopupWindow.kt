package com.zqy.hci.ui

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import com.zqy.hci.R
import com.zqy.hci.listener.OnEmojiActionListener
import com.zqy.hci.provider.CustomRecentEmojiProvider
import com.zqyemoji.emoji2.emojipicker.EmojiPickerView
import com.zqyemoji.emoji2.emojipicker.RecentEmojiProviderAdapter


class EmojiPopupWindow : PopupWindow() {
    private lateinit var mEmoji: EmojiPickerView
    private lateinit var mEmojiListener: OnEmojiActionListener
    private lateinit var mParentView: View

    fun initView(view: View) {
        mParentView = view
        val resId: Int = R.layout.layout_emoji_picker
        contentView = LayoutInflater.from(view.context).inflate(resId, null, false)
        mEmoji = contentView.findViewById(R.id.emoji_picker)
        mEmoji.emojiGridColumns = 11
        mEmoji.emojiGridRows = 4.6f
        width = view.context.resources.getDimension(R.dimen.emojiPickerViewLayoutParamswidth).toInt()
        height = view.context.resources.getDimension(R.dimen.strokeViewLayoutParamsheight).toInt()
        mEmoji.setOnEmojiPickedListener {
            mEmojiListener.onClickEmoji(it.emoji)
        }
        mEmoji.setRecentEmojiProvider(
            RecentEmojiProviderAdapter(
                CustomRecentEmojiProvider(
                    mEmoji.context
                )
            )
        )
    }

    fun show(marginY: Int) {
        if (!isShowing) {
            showAtLocation(mParentView, Gravity.LEFT or Gravity.TOP, 0, marginY)
        }
    }

    fun setOnEmojiListener(listener: OnEmojiActionListener) {
        mEmojiListener = listener
    }
}
package com.zqy.hci.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.zqy.hci.R
import com.zqy.hci.listener.OnEmojiActionListener
import com.zqy.hci.provider.CustomRecentEmojiProvider
import com.zqyemoji.emoji2.emojipicker.EmojiPickerView
import com.zqyemoji.emoji2.emojipicker.RecentEmojiProviderAdapter

/**
 * EmojiPicker外壳.
 * @author:zhenqiyuan
 * @time : 2024/8/21 11:36
 **/
class SuperPenguinEmojiViewContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val emojiPickerView: EmojiPickerView
    private lateinit var mEmojiListener: OnEmojiActionListener

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.layout_superpenguin_emojicontainer, this, true)
        emojiPickerView = findViewById(R.id.emoji_picker)
    }

    // Show or hide the emoji picker
    fun toggleVisibility(isVisible: Boolean) {
        visibility = if (isVisible) VISIBLE else GONE
    }

    fun initView() {
        val layoutParams = emojiPickerView.layoutParams
        layoutParams.width =
            context.resources.getDimension(R.dimen.emojiPickerViewLayoutParamswidth).toInt()
        layoutParams.height =
            context.resources.getDimension(R.dimen.strokeViewLayoutParamsheight).toInt()
        emojiPickerView.layoutParams = layoutParams
        emojiPickerView.emojiGridColumns = 11
        emojiPickerView.emojiGridRows = 4.6f
        emojiPickerView.setOnEmojiPickedListener {
            mEmojiListener.onClickEmoji(it.emoji)
        }
        emojiPickerView.setRecentEmojiProvider(
            RecentEmojiProviderAdapter(
                CustomRecentEmojiProvider(
                    context
                )
            )
        )
    }

    fun show() {
        visibility = VISIBLE
    }

    fun hide() {
        visibility = GONE
    }

    fun setOnEmojiListener(listener: OnEmojiActionListener) {
        mEmojiListener = listener
    }
}
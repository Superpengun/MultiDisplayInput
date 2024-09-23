package com.zqy.hci.listener

/**
 * @author:zhenqiyuan
 * @data:2023/1/10
 * @描述：
 * @package:com.sinovoice.hci.listener
 */
interface OnEmojiActionListener {
    /**
     * 点击emoji表情回调
     * @param emojiType Int emoji类型 0图片 1颜文字
     * @param imgResId Int? emoji图片资源ID
     * @param symbol String 颜文字文本
     */
    fun onClickEmoji(symbol: String)

    fun onDeleteEmoji()
}
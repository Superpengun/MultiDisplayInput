package com.zqy.hci.listener

/**
 * @Author:CWQ
 * @DATE:2022/1/21
 * @DESC:
 */
interface OnNumKeyboardActionListener {

    companion object {
        val EMPTY_LISTENER: OnNumKeyboardActionListener = Adapter()
    }

    /**
     * Sends a sequence of characters to the listener.
     *
     * @param text the sequence of characters to be displayed.
     */
    fun onNumText(text: CharSequence?)


    // TODO: 2017/7/17  empty listener
    class Adapter : OnNumKeyboardActionListener {
        override fun onNumText(text: CharSequence?) {}
    }
}
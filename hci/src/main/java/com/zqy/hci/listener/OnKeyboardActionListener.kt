package com.zqy.hci.listener

/**
 * @Author:CWQ
 * @DATE:2022/1/21
 * @DESC:
 */
interface OnKeyboardActionListener {

    companion object {
        val EMPTY_LISTENER: OnKeyboardActionListener = Adapter()
    }

    /**
     * Called when the user presses a key. This is sent before the [.onKey] is called.
     * For keys that repeat, this is only called once.
     *
     * @param primaryCode the unicode of the key being pressed. If the touch is not on a valid
     * key, the value will be zero.
     */
    fun onPress(primaryCode: Int)

    /**
     * Called when the user releases a key. This is sent after the [.onKey] is called.
     * For keys that repeat, this is only called once.
     *
     * @param primaryCode the code of the key that was released
     */
    fun onRelease(primaryCode: Int)

    /**
     * Send a key press to the listener.
     *
     * @param primaryCode this is the key that was pressed
     * @param keyCodes    the codes for all the possible alternative keys
     * with the primary code being the first. If the primary key code is
     * a single character such as an alphabet or number or symbol, the alternatives
     * will include other characters that may be on the same key or adjacent keys.
     * These codes are useful to correct for accidental presses of a key adjacent to
     * the intended key.
     */
    fun onKey(primaryCode: Int, keyCodes: IntArray?)

    /**
     * Sends a sequence of characters to the listener.
     *
     * @param text the sequence of characters to be displayed.
     */
    fun onText(text: CharSequence?)

    /**
     * Called when the user quickly moves the finger from right to left.
     */
    fun swipeLeft()

    /**
     * Called when the user quickly moves the finger from left to right.
     */
    fun swipeRight()

    /**
     * Called when the user quickly moves the finger from up to down.
     */
    fun swipeDown()

    /**
     * Called when the user quickly moves the finger from down to up.
     */
    fun swipeUp()

    /**
     * Called when the user press the key long time
     * @param text the sequence of characters to be displayed.
     */
    fun onLongPress(text: CharSequence?): Boolean

    // TODO: 2017/7/17  empty listener
    class Adapter : OnKeyboardActionListener {
        override fun onPress(primaryCode: Int) {}
        override fun onRelease(primaryCode: Int) {}
        override fun onKey(primaryCode: Int, keyCodes: IntArray?) {}
        override fun onText(text: CharSequence?) {}
        override fun swipeLeft() {}
        override fun swipeRight() {}
        override fun swipeDown() {}
        override fun swipeUp() {}
        override fun onLongPress(text: CharSequence?): Boolean {
            return false
        }
    }
}
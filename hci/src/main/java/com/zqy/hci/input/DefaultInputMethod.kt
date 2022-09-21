package com.zqy.hci.input

import android.util.Log
import android.view.KeyEvent
import com.zqy.hci.listener.HciCloudInputConnection


/**
 * @author:zhenqiyuan
 * @data:2022/1/18
 * @描述：
 * @package:input
 */
class DefaultInputMethod(
    hciCloudInputConnection: HciCloudInputConnection?
) :
    InputMethod(hciCloudInputConnection!!) {

    companion object {
        protected val TAG = DefaultInputMethod::class.java.canonicalName
    }

    override fun changeLanRes(lan: String) {}

    /**
     * 默认模式下直接进行上屏幕操作
     */
    override fun appendRecognizeChar(inputChar: Char) {
        mHciCloudInputConnection.commitString(inputChar.toString(), 1)
        Log.i(TAG, "appendRecognizeChar() isWord==false commitString:   $inputChar")
    }

    override fun appendRecognizePoints(points: ShortArray) {
    }

    override fun appendSentence(str: String?) {
        //目前暂不考虑非候选键盘下进行语音输入
    }

    /**
     * 默认模式下空实现该方法
     */
    override fun getNextPage() {}

    /**
     * 默认模式下直接进行删除操作
     */
    override fun sendDelete() {
        mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_DEL)
    }

    /**
     * 默认模式下发送\n按键到屏幕
     */
    override fun sendEnter() {
        mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_ENTER)
    }

    /**
     * 默认模式下发送" "到屏幕
     */
    override fun sendSpace() {
        mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_SPACE)
    }

    /**
     * 默认模式下空实现该方法
     */
    override fun sendCandidateChosen(index: Int) {
        Log.e(TAG, "DefaultInputMethod has no candidate word to selected.")
        try {
            throw IllegalStateException("DefaultInputMethod has no candidate word to selected.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun sendSymbol(symbol: String?) {
        mHciCloudInputConnection.commitString(symbol, 1)
    }

    override fun setShiftState(shiftState: Int) {}

    override fun finishInput() {
        mHciCloudInputConnection.finishComposingText()
        onReset()
    }
}
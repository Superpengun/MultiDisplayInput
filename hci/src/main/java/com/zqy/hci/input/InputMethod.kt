package com.zqy.hci.input


import com.zqy.hci.listener.HciCloudInputConnection
import com.zqy.hci.listener.InputMethodListener
import java.util.*

/**
 * @author:zhenqiyuan
 * @data:2022/1/18
 * @描述：
 * @package:input
 */
abstract class InputMethod(
    hciCloudInputConnection: HciCloudInputConnection
) {
    protected var mHciCloudInputConnection: HciCloudInputConnection = hciCloudInputConnection
    protected val MAX_INPUT_LENGTH = 30
    protected val VERSION_CODE = "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv"
    protected var mComposingData = ComposingData()
    protected var mInputMethodListener: InputMethodListener? = null


    companion object {
        private val TAG = InputMethod::class.java.simpleName
    }

    /**
     * 候选词列表
     * 线程安全的
     */
    protected var mCandidateWordsList = Collections.synchronizedList(ArrayList<String>())

    /**
     * 语种信息
     */
    abstract fun changeLanRes(lan: String)

    protected fun clearCandidateWordsList() {
        mCandidateWordsList.clear()
    }

    open fun onReset() {
        clearCandidateWordsList()
        mHciCloudInputConnection.commitComposingStr("")
        notifyCandidateChange()
    }

    protected open fun notifyCandidateChange() {
        mInputMethodListener?.onSendCandidateWordsList(mCandidateWordsList)
    }

    fun setInputMethodListener(inputMethodListener: InputMethodListener) {
        mInputMethodListener = inputMethodListener
    }

    fun checkComposingLength(): Boolean {
        return mComposingData.composingText.isEmpty()
    }


    /**
     * 键盘输入方式下的候选词查询增加一个字符
     */
    abstract fun appendRecognizeChar(inputChar: Char)

    /**
     * 键盘输入方式下的候选词查询增加一个字符
     */
    abstract fun appendRecognizePoints(points: ShortArray)

    /**
     * 语音传整句文本功能
     */
    abstract fun appendSentence(str: String?)

    /**
     * 获取下一页候选词汇
     */
    abstract fun getNextPage()

    /**
     * 删除功能
     */
    abstract fun sendDelete()

    /**
     * 确定功能
     */
    abstract fun sendEnter()

    /**
     * 空格功能
     */
    abstract fun sendSpace()

    /**
     * 候选区（候选词或者联想词）选中处理
     */
    abstract fun sendCandidateChosen(index: Int)

    /**
     * 符号处理
     */
    abstract fun sendSymbol(symbol: String?)

    /**
     * shift处理
     */
    abstract fun setShiftState(shiftState: Int)

    /**
     * 完成当前默认提交
     */
    abstract fun finishInput()
}
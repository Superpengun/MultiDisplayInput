package com.zqy.multidisplayinput.editor

import android.util.Log
import com.zqy.hci.bean.InputModeConst
import com.zqy.hci.input.*
import com.zqy.hci.listener.HciCloudInputConnection
import com.zqy.hci.listener.InputMethodListener
import com.zqy.hci.listener.LogicControlListener
import com.zqy.hci.utils.LanguageUtil
import com.zqy.sdk.manager.HWSDKWrapperManager
import com.zqy.sdk.manager.KBSDKWrapperManager
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * @author:zhenqiyuan
 * @data:2022/5/10
 * @描述：
 * @package:com.zqy.multidisplayinput.editer
 */
class ImeEditor() : InputMethodListener {

    private var mDefaultInputMethod: InputMethod? = null
    private var mMultiInputMethod: InputMethod? = null
    private var mCurrentInputMethod: InputMethod? = null
    private var mPinYinInputMethod: InputMethod? = null
    private var mHWInputMethod: InputMethod? = null
    private var mCurrentInputMode = 0
    private lateinit var mHciCloudInputConnection: HciCloudInputConnection
    private lateinit var mLogicControlListener: LogicControlListener
    private var lastMode = -1
    private var mHWSDKWrapperManager: HWSDKWrapperManager = HWSDKWrapperManager()
    private var mKBSDKWrapperManager: KBSDKWrapperManager = KBSDKWrapperManager()

    private lateinit var mInputScope: CoroutineScope
    private var mInputDispatcher: CoroutineContext? = null

    companion object {
        private val TAG = ImeEditor::class.java.canonicalName
    }

    /**
     * 输入模式切换
     */
    fun inputMethodChange(mode: Int) {
        mInputScope.launch {
            Log.i(TAG, "onInputModeChange():mode=$mode")
            mCurrentInputMode = mode
            val currentConnection = mCurrentInputMethod?.getInputConnection()
            if (lastMode == InputModeConst.INPUT_CHINESE){
                mCurrentInputMethod?.onReset()
            }
            when (mCurrentInputMode) {
                InputModeConst.INPUT_DEFAULT -> {
                    mCurrentInputMethod = mDefaultInputMethod
                }
                InputModeConst.INPUT_CHINESE -> {
                    mCurrentInputMethod = mPinYinInputMethod
                    if (lastMode != mode) {
                        mCurrentInputMethod?.changeLanRes("_cn_")
                        Log.i(TAG, "changeLanRes()" + "_cn_")
                        lastMode = mode
                    }
                    mCurrentInputMethod?.setInputMethodListener(this@ImeEditor)
                }
                InputModeConst.INPUT_HWR_CN -> {
                    mCurrentInputMethod = mHWInputMethod
                    if (lastMode != mode) {
                        mCurrentInputMethod?.changeLanRes("_cn_")
                        Log.i(TAG, "changeLanRes()" + "_cn_")
                        lastMode = mode
                    }
                    mCurrentInputMethod?.setInputMethodListener(this@ImeEditor)
                }
                InputModeConst.INPUT_HWR_EN -> {
                    mCurrentInputMethod = mHWInputMethod
                    if (lastMode != mode) {
                        mCurrentInputMethod?.changeLanRes("_en_")
                        Log.i(TAG, "changeLanRes()" + "_en_")
                        lastMode = mode
                    }
                    mCurrentInputMethod?.setInputMethodListener(this@ImeEditor)
                }
                else -> {
                    mCurrentInputMethod = mMultiInputMethod
                    if (lastMode != mode) {
                        mCurrentInputMethod?.changeLanRes(LanguageUtil.getRexPreFix(mode))
                        Log.i(TAG, "changeLanRes()" + LanguageUtil.getRexPreFix(mode))
                        lastMode = mode
                    }
                    mCurrentInputMethod?.setInputMethodListener(this@ImeEditor)
                }
            }
            if (currentConnection != null){
                mCurrentInputMethod?.updateInputConnection(currentConnection)
            }
        }
    }

    ///////////////////////////////////////////////系统相关////////////////////////////////////////////////////////


    fun setListener(
        hciCloudInputConnection: HciCloudInputConnection,
        logicControlListener: LogicControlListener
    ) {
        mHciCloudInputConnection = hciCloudInputConnection
        mLogicControlListener = logicControlListener
        if (mMultiInputMethod == null) {
            mMultiInputMethod = MultiInputMethod(hciCloudInputConnection)
            (mMultiInputMethod as MultiInputMethod).setKBSDKWrapperManager(mKBSDKWrapperManager)
        }
        if (mDefaultInputMethod == null) {
            mDefaultInputMethod = DefaultInputMethod(hciCloudInputConnection)
        }
        if (mPinYinInputMethod == null) {
            mPinYinInputMethod = PinYinInputMethod(hciCloudInputConnection)
            (mPinYinInputMethod as PinYinInputMethod).setHWSDKWrapperManager(mHWSDKWrapperManager)
            (mPinYinInputMethod as PinYinInputMethod).setKBSDKWrapperManager(mKBSDKWrapperManager)
        }
        if (mHWInputMethod == null) {
            mHWInputMethod = HWInputMethod(hciCloudInputConnection)
            (mHWInputMethod as HWInputMethod).setHWSDKWrapperManager(mHWSDKWrapperManager)
        }
        if (mInputDispatcher == null) {
            mInputDispatcher = newSingleThreadContext("input")
            mInputScope = CoroutineScope(mInputDispatcher!!)
        }
        if (mCurrentInputMethod != null) {
            mCurrentInputMethod!!.updateInputConnection(hciCloudInputConnection)
        }
    }

    ///////////////////////////////////////////////功能相关////////////////////////////////////////////////////////
    /**
     * 键盘符号按键处理
     * 根据keyCode解析输入的符号并上屏
     */
    fun sendSymbol(primaryCode: Int?) {
        val symbol: Char? = primaryCode?.toChar()
        sendSymbol(symbol.toString())
    }

    fun sendSymbol(symbol: String?) {
        mInputScope.launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod!!.sendSymbol(symbol)
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    /**
     * 键盘普通字母按键处理
     */
    fun query(text: CharSequence?) {
        mInputScope.launch {
            if (mCurrentInputMethod != null && mShiftState.checkNeedChange()) {
                mCurrentInputMethod?.setShiftState(mShiftState.shiftMode)
            }
            val inputChar = text?.get(0)
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod!!.appendRecognizeChar(inputChar!!)
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    /**
     * 手写笔迹处理
     */
    fun recog(points: ShortArray) {
        mInputScope.launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod!!.appendRecognizePoints(points)
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    /**
     * 候选区选中一个词处理
     */
    fun sendCandChoosed(index: Int) {
        mInputScope.launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod?.sendCandidateChosen(index)
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    /**
     * 删除功能
     * 删除完之后，通过handler把结果传出去
     */
    fun sendDelete() {
        mInputScope.launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod?.sendDelete()
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    /**
     * 确定功能
     * 确定之后，通过handler把结果传出去
     */
    fun sendEnter() {
        mInputScope.launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod?.sendEnter()
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    /**
     * 语音传整句文本功能
     */
    fun sendSentence(sentence: String?) {
        mInputScope.launch {
            mCurrentInputMethod!!.appendSentence(sentence)
        }
    }

    /**
     * 空格功能
     * 空格之后，通过handler把结果传出去
     */
    fun sendSpace() {
        mInputScope.launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod?.sendSpace()
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    fun getNextPage() {
        mInputScope.launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod?.getNextPage()
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    fun setShiftState(shiftMode: Int) {
        mInputScope.launch {
            mShiftState.shiftMode = shiftMode
        }
    }

    fun clear() {
        mInputScope.launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod?.finishInput()
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    fun reset() {
        mInputScope.launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod?.onResetResult()
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    fun switchWordResult() {
        mInputScope.launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod?.switchWordResult()
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    fun switchLetterResult() {
        mInputScope.launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod?.switchLetterResult()
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    fun getComposingDataLength(): Int {
        var length = 0
        mInputScope.launch {
            if (mCurrentInputMethod != null) {
                length = mCurrentInputMethod!!.getComposingLength()
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
        return length
    }

    ///////////////////////////////////////////////Shift内部处理////////////////////////////////////////////////////////
    private inner class ShiftState {
        var needChange = false
        var shiftMode = 0
            set(shiftMode) {
                if (this.shiftMode != shiftMode) {
                    needChange = true
                    field = shiftMode
                }
            }

        fun checkNeedChange(): Boolean {
            if (needChange) {
                needChange = false
                return true
            }
            return false
        }
    }

    private val mShiftState: ShiftState = ShiftState()

    ///////////////////////////////////////////////结果信息传递////////////////////////////////////////////////////////

    override fun onSendComposingText(composingText: String) {
        mInputScope.launch(Dispatchers.Main.immediate) {
            mLogicControlListener.onUpdateComposingText(composingText)
        }
    }

    override fun onSendCandidateWordsList(updateCandidateWordsList: List<String>) {
        mLogicControlListener.onUpdateCandidateWordsList(updateCandidateWordsList)
    }

    override fun onHideView() {
        mInputScope.launch(Dispatchers.Main.immediate) {
            mLogicControlListener.onHideView()
        }
    }
}
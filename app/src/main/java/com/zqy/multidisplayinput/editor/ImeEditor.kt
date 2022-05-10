package com.zqy.multidisplayinput.editor

import android.content.Context
import android.util.Log
import com.zqy.hci.bean.InputModeConst
import com.zqy.hci.input.DefaultInputMethod
import com.zqy.hci.input.InputMethod
import com.zqy.hci.input.LogicControl
import com.zqy.hci.input.MultiInputMethod
import com.zqy.hci.listener.HciCloudInputConnection
import com.zqy.hci.listener.InputMethodListener
import com.zqy.hci.listener.LogicControlListener
import com.zqy.hci.utils.LanguageUtil
import com.zqy.sdk.tools.HciCloudUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author:zhenqiyuan
 * @data:2022/5/10
 * @描述：
 * @package:com.zqy.multidisplayinput.editer
 */
class ImeEditor (context: Context): InputMethodListener {

    private var mDefaultInputMethod: InputMethod? = null
    private var mMultiInputMethod: InputMethod? = null
    private var mCurrentInputMethod: InputMethod? = null
    private var mCurrentInputMode = 0
    private var mContext = context
    private lateinit var mHciCloudInputConnection: HciCloudInputConnection
    private lateinit var mLogicControlListener: LogicControlListener
    private var mInitHciCloudSys = false
    private var lastMode = -1

    companion object {
        private val TAG = ImeEditor::class.java.canonicalName
    }
    /**
     * 输入模式切换
     */
    fun inputMethodChange(mode: Int) {
        Log.i(TAG, "onInputModeChange():mode=$mode")
        mCurrentInputMode = mode
        if(!LogicControl.instance.mInitHciCloudSys){
            mCurrentInputMode = InputModeConst.INPUT_DEFAULT
        }
        when (mCurrentInputMode) {
            InputModeConst.INPUT_DEFAULT -> mCurrentInputMethod = mDefaultInputMethod
            else -> {
                mCurrentInputMethod = mMultiInputMethod
                if (lastMode != mode) {
                    mCurrentInputMethod?.changeLanRes(LanguageUtil.getRexPreFix(mode))
                    Log.i(TAG, "changeLanRes()" + LanguageUtil.getRexPreFix(mode))
                    lastMode = mode
                }
                mCurrentInputMethod?.setInputMethodListener(this)
            }
        }
    }

    ///////////////////////////////////////////////系统相关////////////////////////////////////////////////////////


    fun setListener(hciCloudInputConnection: HciCloudInputConnection,
                    logicControlListener: LogicControlListener){
        mHciCloudInputConnection = hciCloudInputConnection
        mLogicControlListener = logicControlListener
        mMultiInputMethod = MultiInputMethod(hciCloudInputConnection)
        mDefaultInputMethod = DefaultInputMethod(hciCloudInputConnection)
    }

    ///////////////////////////////////////////////功能相关////////////////////////////////////////////////////////
    /**
     * 键盘符号按键处理
     * 根据keyCode解析输入的符号并上屏
     */
    fun sendSymbol(primaryCode: Int?) {
        val symbol: Char? =primaryCode?.toChar()
        sendSymbol(symbol.toString())
    }

    fun sendSymbol(symbol: String?) {
        CoroutineScope(Dispatchers.IO).launch {
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
        CoroutineScope(Dispatchers.IO).launch {
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
     * 候选区选中一个词处理
     */
    fun sendCandChoosed(index: Int) {
        CoroutineScope(Dispatchers.IO).launch {
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
        CoroutineScope(Dispatchers.IO).launch {
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
        CoroutineScope(Dispatchers.IO).launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod?.sendEnter()
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    /**
     * 空格功能
     * 空格之后，通过handler把结果传出去
     */
    fun sendSpace() {
        CoroutineScope(Dispatchers.IO).launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod?.sendSpace()
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    fun getNextPage() {
        CoroutineScope(Dispatchers.IO).launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod?.getNextPage()
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    fun setShiftState(shiftMode: Int) {
        mShiftState.shiftMode = shiftMode
    }

    fun clear() {
        CoroutineScope(Dispatchers.IO).launch {
            if (mCurrentInputMethod != null) {
                mCurrentInputMethod?.onReset()
            } else {
                Log.i(TAG, "getInputMethod() return null")
            }
        }
    }

    fun getVersionName(): String? {
        return HciCloudUtils.getLocalVersionName(mContext)
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
        CoroutineScope(Dispatchers.Main).launch{
            mLogicControlListener.onUpdateComposingText(composingText)
        }
    }

    override fun onSendCandidateWordsList(updateCandidateWordsList: List<String>) {
        CoroutineScope(Dispatchers.Main).launch{
            mLogicControlListener.onUpdateCandidateWordsList(updateCandidateWordsList)
        }
    }
}
package com.zqy.hci.input

import android.util.Log
import android.view.KeyEvent
import com.zqy.hci.listener.HciCloudInputConnection
import com.zqy.hci.service.Settings
import com.zqy.hci.utils.LanguageUtil
import com.zqy.sdk.keyboard.RecogResult
import com.zqy.sdk.manager.KBSDKWrapperManager
import com.superpenguin.foreigninputandoutput.inputEngine.KeyboardEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * @author:zhenqiyuan
 * @data:2022/1/18
 * @描述：
 * @package:input
 */
class MultiInputMethod(
    hciCloudInputConnection: HciCloudInputConnection?
) :
    InputMethod(hciCloudInputConnection!!) {

    private var shiftModeTobeChangeTo = 0
    private var mLanResPreFix = ""
    private lateinit var mKBManager : KBSDKWrapperManager
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    companion object {
        protected val TAG = MultiInputMethod::class.java.simpleName
    }

    /**
     * 是否为单词模式
     * 默认为单词模式
     */
    private var isWord = true

    /**
     * 语种信息传递
     */
    override fun changeLanRes(lan: String) {
        mLanResPreFix = lan
        Log.d(TAG, "changeLanRes: $lan")
        mKBManager.changeLanguage(lan)
    }

    /**
     * 在长度限制内进行查询
     */
    override fun appendRecognizeChar(inputChar: Char) {
        if (composingExceedLengthCheck()) return
        mComposingData.onComposingStart()
        clearCandidateWordsList()
        try {
            mComposingData.composingText.append(inputChar)
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
        mHciCloudInputConnection.commitComposing(mComposingData.composingText.toString())
        notifyComposingChange()
        queryAndUpdateCandidate(mComposingData.composingText.toString())
        notifyCandidateChange()
    }

    override fun appendRecognizePoints(points: ShortArray) {
    }

    override fun appendSentence(str: String?) {
        mHciCloudInputConnection.commitComposing(str)
        mHciCloudInputConnection.finishComposingText()
    }


    /**
     * 处理删除删除按键
     */
    override fun sendDelete() {
        if (checkComposingLength() || mComposingData.composingText.length >mHciCloudInputConnection.getCandidateLength()) {
            mComposingData.onComposingStop()
            mComposingData.composingText.clear()
            mHciCloudInputConnection.finishComposingText()
            mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_DEL)
            clearCandidateWordsList()
            notifyCandidateChange()
            return
        }
        when (mComposingData.composingState) {
            ComposingData.ComposingState.INCOMPOSING -> {
                try {
                    mComposingData.composingText.deleteCharAt(mComposingData.composingText.length - 1)
                } catch (e: StringIndexOutOfBoundsException) {
                    e.printStackTrace()
                }
                mHciCloudInputConnection.commitComposing(mComposingData.composingText.toString())
                clearCandidateWordsList()
                notifyComposingChange()
                queryAndUpdateCandidate(mComposingData.composingText.toString())
                if (mComposingData.composingText.isEmpty()){
                    clearComposing()
                    mInputMethodListener?.onHideView()
                }
            }
            ComposingData.ComposingState.FINSHCOMPOSING -> {
                clearCandidateWordsList()
                mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_DEL)
            }

            else -> {}
        }
        notifyCandidateChange()
    }

    /**
     * 当前有候选词的时候，提交当前composing上屏
     * 无候选词汇提交\
     */
    override fun sendEnter() {
        if (checkComposingLength()) {
            mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_ENTER)
            return
        }
        when (mComposingData.composingState) {
            ComposingData.ComposingState.INCOMPOSING -> {
                mHciCloudInputConnection.commitString(mComposingData.composingText.toString(), 1)
                clearComposing()
                mHciCloudInputConnection.finishComposingText()
                notifyComposingChange()
                clearCandidateWordsList()
            }
            ComposingData.ComposingState.FINSHCOMPOSING -> {
                clearCandidateWordsList()
                mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_ENTER)
            }

            else -> {}
        }
        notifyCandidateChange()
    }

    /**
     * 处理空格按键 有候选情况下提交 第一个候选上屏
     */
    override fun sendSpace() {
        if (checkComposingLength()) {
            mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_SPACE)
            return
        }
        var strTemp = ""
        if (mCandidateWordsList.size > 0) {
            strTemp = mCandidateWordsList[0]
        }
        when (mComposingData.composingState) {
            ComposingData.ComposingState.INCOMPOSING -> {
                if (strTemp != "") {
                    sendCandidateChosen(0)
                } else {
                    mHciCloudInputConnection.commitString(
                        mComposingData.composingText.toString(),
                        1
                    )
                }
                clearComposing()
                mHciCloudInputConnection.finishComposingText()
                notifyComposingChange()
                clearCandidateWordsList()
            }
            ComposingData.ComposingState.FINSHCOMPOSING -> {
                clearCandidateWordsList()
                mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_SPACE)
            }

            else -> {}
        }
        notifyCandidateChange()
    }

    /**
     * 根据候选择去进行联想词汇查询
     */
    override fun sendCandidateChosen(index: Int) {
        val candidateCount = mCandidateWordsList.size
        if (index < candidateCount) {
            val chosenWord = mCandidateWordsList[index]
            if (Settings.instance.getCurrent()?.isAutoSpace() == true) {
                mHciCloudInputConnection.commitString("$chosenWord ", 1)
            } else {
                mHciCloudInputConnection.commitString(chosenWord, 1)
            }
            when (mComposingData.composingState) {
                ComposingData.ComposingState.INCOMPOSING -> {
                    mHciCloudInputConnection.finishComposingText()
                }
                ComposingData.ComposingState.FINSHCOMPOSING -> {
                    // TODO: 目前不处理二次选择时的composing
                }

                else -> {}
            }
            clearComposing()
            notifyComposingChange()
            clearCandidateWordsList()
            associateAndUpdateCandidate(chosenWord)
            notifyCandidateChange()
        } else {
            Log.e(TAG, "handleCandidateChosen index error")
        }
    }

    /**
     * 处理符号输入
     */
    override fun sendSymbol(symbol: String?) {
        if (checkComposingLength()) {
            mHciCloudInputConnection.commitString(symbol, 1)
            clearCandidateWordsList()
            notifyCandidateChange()
            return
        }
        var firstCandidate = ""
        if (mCandidateWordsList.size > 0) {
            firstCandidate = mCandidateWordsList[0]
        }
        when (mComposingData.composingState) {
            ComposingData.ComposingState.INCOMPOSING -> {
                clearComposing()
                mHciCloudInputConnection.commitString(firstCandidate + symbol, 1)
                mHciCloudInputConnection.finishComposingText()
                notifyComposingChange()
            }
            ComposingData.ComposingState.FINSHCOMPOSING -> {
                mHciCloudInputConnection.commitString(symbol, 1)
            }

            else -> {}
        }
        clearCandidateWordsList()
        notifyCandidateChange()
    }

    /**
     * 更改当前是shift模式
     */
    override fun setShiftState(shiftState: Int) {
        shiftModeTobeChangeTo = shiftState
    }

    override fun finishInput() {
        onReset()
//        mKBManager.release()
    }

    override fun switchLetterResult() {}

    override fun switchWordResult() {}

    /**
     * 获取下一页候选词汇
     */
    override fun getNextPage() {
    }

    /**
     * 清空数据
     */
    override fun onReset() {
        mHciCloudInputConnection.finishComposingText()
        clearComposing()
        notifyComposingChange()
        clearCandidateWordsList()
        super.notifyCandidateChange()
    }

    override fun onResetResult() {
        clearComposing()
        notifyComposingChange()
        notifyCandidateChange()
        super.onResetResult()
    }

    fun setKBSDKWrapperManager(m: KBSDKWrapperManager) {
        mKBManager = m
    }

    private fun queryAndUpdateCandidate(query: String) {
        if (query.isEmpty()) return
        checkEngineState()
        val recogResult: RecogResult = mKBManager.multiQuery(query)
        val items: ArrayList<String> = recogResult.getRecogResult()
        mCandidateWordsList.clear()
        synchronized(this) {
            for (item in items) {
                mCandidateWordsList.add(item)
            }
            if (mCandidateWordsList.size == 0) mCandidateWordsList.add(mComposingData.composingText.toString())
            processCandidateData(shiftModeTobeChangeTo)
        }
    }

    private fun notifyComposingChange() {
        try {
            mInputMethodListener?.onSendComposingText(mComposingData.composingText.toString())
        } catch (e: StringIndexOutOfBoundsException) {
            Log.d(TAG, "notifyComposingChange: composingtext outoflength")
        }
    }

    private fun clearComposing() {
        mComposingData.onComposingStop()
        try {
            mComposingData.composingText.delete(0, mComposingData.composingText.length)
        } catch (e: StringIndexOutOfBoundsException) {
            Log.d(TAG, "clearComposing: composingtext outoflength")
        }
    }

    private fun composingExceedLengthCheck(): Boolean {
        if (mComposingData.composingText.length > MAX_INPUT_LENGTH) {
            if (mComposingData.composingText.toString().equals(VERSION_CODE, ignoreCase = true)) {
                clearCandidateWordsList()
                mCandidateWordsList.add(LogicControl.instance.getVersionName())
                notifyCandidateChange()
            }
            return true
        }
        return false
    }

    private val setSum: MutableSet<Int> = HashSet()
    private val resultString: ArrayList<String?> = ArrayList()
    private fun rMList(al: List<String>?): ArrayList<String?> {
        setSum.clear()
        resultString.clear()
        if (al != null) {
            val iter: Iterator<*> = al.iterator()
            while (iter.hasNext()) {
                val element = iter.next() as String
                if (setSum.add(element.hashCode())) resultString.add(element)
            }
        }
        return resultString
    }

    private fun processCandidateData(shiftMode: Int) {
        if (checkComposingLength()) return
        //阿拉伯语和波斯语不用大小写检查
        if (LanguageUtil.notNeedCheckUpper(mLanResPreFix)) return
        //波斯语去重操作
        if (LanguageUtil.needRemoveRepeat(mLanResPreFix)) {
            mCandidateWordsList = rMList(mCandidateWordsList)
        }
    }

    private fun associateAndUpdateCandidate(query: String) {
        if (query.isEmpty()) return
        checkEngineState()
        val associateList = KeyboardEngine.associateWord(query)
        mCandidateWordsList.clear()
        synchronized(this) {
            mCandidateWordsList.addAll(associateList)
        }
        oldList.clear()
        oldList.addAll(mCandidateWordsList)
    }

    private fun checkEngineState(){
        if (LogicControl.instance.currentLanguage != mKBManager.resPreFix){
            scope.launch {
                KeyboardEngine.switchLanguage(mKBManager.resPreFix)
                LogicControl.instance.currentLanguage = mKBManager.resPreFix
            }
        }
    }
}
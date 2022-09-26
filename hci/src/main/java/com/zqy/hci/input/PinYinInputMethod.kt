package com.zqy.hci.input

import android.util.Log
import android.view.KeyEvent
import com.zqy.hci.listener.HciCloudInputConnection
import com.zqy.sdk.keyboard.RecogResult
import com.zqy.sdk.keyboard.RecogResultItem
import com.zqy.sdk.manager.HWSDKWrapperManager
import com.zqy.sdk.manager.KBSDKWrapperManager
import java.util.ArrayList
import java.util.HashSet

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.hci.input
 */
class PinYinInputMethod(
    hciCloudInputConnection: HciCloudInputConnection?
) :
    InputMethod(hciCloudInputConnection!!) {
    private val mCompoing: PinyinComposingInterface = PinYinProcesser()
    private var shiftModeTobeChangeTo = 0
    private var mLanResPreFix = ""
    private val NUMBER_EN_TABLE = charArrayOf('a', 'd', 'g', 'j', 'm', 'p', 't', 'w')
    private lateinit var mHWManager : HWSDKWrapperManager
    private lateinit var mKBManager : KBSDKWrapperManager

    companion object {
        val TAG = PinYinInputMethod::class.java.simpleName
    }

    /**
     * 是否为单词模式
     * 默认为单词模式
     */
    private var isWord = true

    private var mRecogResult: RecogResult = RecogResult()

    /**
     * 语种信息传递
     */
    override fun changeLanRes(lan: String) {
        mLanResPreFix = lan
        Log.d(TAG, "changeLanRes: $lan")
        mKBManager.changeLanguage(lan)
        mHWManager.changeLanguage(lan)
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
        if (mCompoing.getLastCharOfCompoingToQuery() == '\'' && inputChar == '\'') {
            return
        }
        if (inputChar == '\'' && mCompoing.getCompoingToQuery()?.length == 0) {
            return
        }
        mCompoing.appendLetter(inputChar)
        queryAndUpdateCandidate(mComposingData.composingText.toString())
        mHciCloudInputConnection.commitComposing(mCompoing.getComposingToCommiting())
        notifyCandidateChange()
    }

    override fun appendRecognizePoints(points: ShortArray) {
    }

    override fun appendSentence(str: String?) {
        mComposingData.onComposingStart()
        clearCandidateWordsList()
        try {
            mComposingData.composingText.append(str)
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
        mHciCloudInputConnection.commitComposing(mComposingData.composingText.toString())
    }


    /**
     * 处理删除删除按键
     */
    override fun sendDelete() {
        if (checkComposingLength()) {
            mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_DEL)
            return
        }
        when (mComposingData.composingState) {
            ComposingData.ComposingState.INCOMPOSING -> {
                mCompoing.del()
                mCompoing.getCompoingToQuery()?.let { queryAndUpdateCandidate(it) }
                try {
                    mComposingData.composingText.deleteCharAt(mComposingData.composingText.length - 1)
                } catch (e: StringIndexOutOfBoundsException) {
                    e.printStackTrace()
                }
                mHciCloudInputConnection.commitComposing(mCompoing.getComposingToCommiting())
                if (mCompoing.getComposingToCommiting()?.isEmpty() == true) {
                    clearCandidateWordsList()
                    clearComposing()
                }
                notifyComposingChange()
            }
            ComposingData.ComposingState.FINSHCOMPOSING -> {
                clearCandidateWordsList()
                mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_DEL)
            }
        }
        notifyCandidateChange()
    }

    /**
     * 当前有候选词的时候，提交当前composing上屏
     * 无候选词汇提交\
     */
    override fun sendEnter() {
        if (checkComposingLength()) return
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
        }
        notifyCandidateChange()
    }

    /**
     * 根据候选择去进行联想词汇查询
     */
    override fun sendCandidateChosen(index: Int) {
        if (checkComposingLength()) return
        val candidateCount = mCandidateWordsList.size
        if (index < candidateCount) {
            val chosenWord = mCandidateWordsList[index]
            if (mRecogResult.getRecogResultItems()?.size == 0) {
                sendEnter()
                return
            } else {
                val word: String? = mRecogResult.getRecogResultItems()?.get(index)?.getResult()
                val symbols: String? =
                    mRecogResult.getRecogResultItems()?.get(index)?.getSymbols()
                if (word != null) {
                    if (symbols != null) {
                        mCompoing.appendSelectedItem(word, symbols)
                    }
                }
            }
            if (mCompoing.isMatchComplete()) {   //匹配结束
                //获取用户输入的完整结果
                val userData = mCompoing.getCurrentOutput()
                if (userData!!.isNotEmpty()) {
                    mRecogResult.getRecogResultItems()?.get(index)?.getSymbols()?.let {
                        mKBManager.submitUDB(
                            userData,
                            it
                        )
                    }

                    //联想词提前
                    mHWManager.raisePriority(userData)
                    //上屏
                    mHciCloudInputConnection.commitString(userData, 1)
                    Log.i(TAG, "handleCandidateChosen()  commitString:  $userData")
                } else {
                    //直接上屏
                    mHciCloudInputConnection.commitString(chosenWord, 1)
                    Log.i(
                        TAG,
                        "handleCandidateChosen()  commitString:  " + mCandidateWordsList[index]
                    )
                }
                mCompoing.clear()
                //根据选择的候选区获取联想词
                associateAndUpdateCandidate(chosenWord)
                notifyCandidateChange()
            } else {     //匹配未结束
                clearCandidateWordsList()
                queryAndUpdateCandidate(mCompoing.getCompoingToQuery()!!)
                notifyCandidateChange()
            }
            when (mComposingData.composingState) {
                ComposingData.ComposingState.INCOMPOSING -> {
                    mHciCloudInputConnection.finishComposingText()
                }
                ComposingData.ComposingState.FINSHCOMPOSING -> {
                    // TODO: 目前不处理二次选择时的composing
                }
            }
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
        mKBManager.release()
        mHWManager.release()
    }

    fun getHWSDKWrapperManager(m: HWSDKWrapperManager) {
        mHWManager = m
    }

    fun getKBSDKWrapperManager(m: KBSDKWrapperManager) {
        mKBManager = m
    }

    /**
     * 获取下一页候选词汇
     */
    override fun getNextPage() {
        if (checkComposingLength()) return
        val composing = mCompoing.getComposingToCommiting()
        val recogResult: RecogResult? = mKBManager.multiGetMore()
        val items: ArrayList<RecogResultItem>? = recogResult?.getRecogResultItems()
        if (composing != null && composing.isNotEmpty()) {
            if (items!!.isNotEmpty()) {
                mCandidateWordsList.clear()
            }

            for (item in items) {
                mCandidateWordsList.add(item.getResult())
            }
        }
        notifyCandidateChange()

    }

    /**
     * 清空数据
     */
    override fun onReset() {
        mHciCloudInputConnection.finishComposingText()
        clearComposing()
        notifyComposingChange()
        clearCandidateWordsList()
        mHciCloudInputConnection.commitComposingStr("")
        super.notifyCandidateChange()
    }

    private fun queryAndUpdateCandidate(query: String) {
        if (query.isEmpty()) return
        mRecogResult = mKBManager.multiQuery(query)
        val items: ArrayList<RecogResultItem>? = mRecogResult.getRecogResultItems()
        mCandidateWordsList.clear()
        if (items!!.isEmpty()) {
            mCandidateWordsList.add(query)
        } else {
            mCompoing.updateFirstCandidate(items[0].getResult(), items[0].getSymbols())
        }
        synchronized(this) {
            for (item in items) {
                mCandidateWordsList.add(item.getResult())
            }
        }
    }

    private fun associateAndUpdateCandidate(query: String) {
        if (query.isEmpty()) return
        val associateList = mHWManager.associateQuery(query)
        mCandidateWordsList.clear()
        synchronized(this) {
            if (associateList != null) {
                for (item in associateList) {
                    mCandidateWordsList.addAll(associateList)
                }
            }
        }
    }

    private fun notifyComposingChange() {
        mInputMethodListener?.onSendComposingText(mComposingData.composingText.toString())
    }

    private fun clearComposing() {
        mComposingData.onComposingStop()
        mCompoing.clear()
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

}
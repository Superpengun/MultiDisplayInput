package com.zqy.hci.input

import android.util.Log
import android.view.KeyEvent
import com.zqy.hci.listener.HciCloudInputConnection
import com.zqy.hci.service.Settings
import com.zqy.hci.utils.LanguageUtil
import com.zqy.sdk.keyboard.RecogResult
import com.zqy.sdk.keyboard.RecogResultItem
import com.zqy.sdk.manager.KBSDKWrapperManager
import java.lang.IndexOutOfBoundsException
import java.util.*
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
                try {
                    mComposingData.composingText.deleteCharAt(mComposingData.composingText.length - 1)
                } catch (e: StringIndexOutOfBoundsException) {
                    e.printStackTrace()
                }
                mHciCloudInputConnection.commitComposing(mComposingData.composingText.toString())
                clearCandidateWordsList()
                notifyComposingChange()
                queryAndUpdateCandidate(mComposingData.composingText.toString())
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
            }
            clearComposing()
            notifyComposingChange()
            clearCandidateWordsList()
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
    }

    /**
     * 获取下一页候选词汇
     */
    override fun getNextPage() {
        if (checkComposingLength()) return
        val recogResult: RecogResult? = mKBManager.multiGetMore()
        val items: ArrayList<RecogResultItem>? = recogResult?.getRecogResultItems()
        mCandidateWordsList.clear()
        for (item in items!!) {
            mCandidateWordsList.add(item.getResult())
        }
        processCandidateData(shiftModeTobeChangeTo)
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

    fun getKBSDKWrapperManager(m: KBSDKWrapperManager) {
        mKBManager = m
    }

    private fun queryAndUpdateCandidate(query: String) {
        if (query.isEmpty()) return
        val recogResult: RecogResult = mKBManager.multiQuery(query)
        val items: ArrayList<RecogResultItem>? = recogResult.getRecogResultItems()
        mCandidateWordsList.clear()
        synchronized(this) {
            for (item in items!!) {
                mCandidateWordsList.add(item.getResult())
            }
            if (mCandidateWordsList.size == 0) mCandidateWordsList.add(mComposingData.composingText.toString())
            processCandidateData(shiftModeTobeChangeTo)
        }
    }

    private fun notifyComposingChange() {
        mInputMethodListener?.onSendComposingText(mComposingData.composingText.toString())
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
        var item: String
        var subPart2: String
        Log.d(TAG, "shiftMode:$shiftMode")
        try {
            for (i in 0 until mCandidateWordsList.size) {
                item = mCandidateWordsList[i]
                if (item.length >= mComposingData.composingText.length) {
                    subPart2 = item.substring(mComposingData.composingText.length)
                    when (shiftMode) {
                        2 -> mCandidateWordsList[i] =
                            mComposingData.composingText.toString() + subPart2.uppercase(Locale.getDefault())
                        1 -> mCandidateWordsList[i] =
                            mComposingData.composingText.toString() + subPart2
                        else -> mCandidateWordsList[i] =
                            mComposingData.composingText.toString() + subPart2.lowercase(Locale.getDefault())
                    }
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }
}
package com.zqy.hci.input

import android.util.Log
import android.view.KeyEvent
import com.zqy.hci.listener.HciCloudInputConnection
import com.zqy.hci.service.Settings
import com.zqy.sdk.HWInputEngineInstance
import com.zqy.sdk.KBInputEngineInstance
import com.zqy.sdk.keyboard.RecogResult
import com.zqy.sdk.keyboard.RecogResultItem
import java.util.ArrayList
import java.util.HashSet

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.hci.input
 */
class HWInputMethod(
    hciCloudInputConnection: HciCloudInputConnection?
) :
    InputMethod(hciCloudInputConnection!!) {
    private val mCompoing: PinyinComposingInterface = PinYinProcesser()
    private var shiftModeTobeChangeTo = 0
    private var mLanResPreFix = ""
    private val NUMBER_EN_TABLE = charArrayOf('a', 'd', 'g', 'j', 'm', 'p', 't', 'w')

    companion object {
        val TAG = HWInputMethod::class.java.simpleName
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
        KBInputEngineInstance.get().changeLanguage(lan)
        HWInputEngineInstance.get().changeLanguage(lan)
    }

    /**
     * 在长度限制内进行查询
     */
    override fun appendRecognizeChar(inputChar: Char) {
        mHciCloudInputConnection.commitString(inputChar.toString(), 1)
    }

    override fun appendRecognizePoints(points: ShortArray) {
        if (composingExceedLengthCheck()) return
        when (mComposingData.composingState) {
            ComposingData.ComposingState.INCOMPOSING -> {
                mHciCloudInputConnection.commitString(mComposingData.composingText.toString(), 1)
                clearComposing()
                mHciCloudInputConnection.finishComposingText()
            }
            ComposingData.ComposingState.FINSHCOMPOSING -> {

            }
        }
        mComposingData.onComposingStart()
        clearCandidateWordsList()
        val recogList = HWInputEngineInstance.get().recog(points)
        try {
            if (recogList.size != 0) {
                mComposingData.composingText.append(recogList[0])
                synchronized(this) {
                    mCandidateWordsList.addAll(recogList)
                }
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
        mHciCloudInputConnection.commitComposing(mComposingData.composingText.toString())
        notifyCandidateChange()
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
                    clearCandidateWordsList()
                    associateAndUpdateCandidate(mComposingData.composingText.toString())
                    clearComposing()
                    notifyCandidateChange()
                }
                ComposingData.ComposingState.FINSHCOMPOSING -> {
                    clearComposing()
                    notifyComposingChange()
                    clearCandidateWordsList()
                    notifyCandidateChange()
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
    }

    /**
     * 获取下一页候选词汇
     */
    override fun getNextPage() {
        if (checkComposingLength()) return
        val recogResult: RecogResult? = KBInputEngineInstance.get().multiGetMore()
        val items: ArrayList<RecogResultItem>? = recogResult?.getRecogResultItems()
        mCandidateWordsList.clear()
        for (item in items!!) {
            mCandidateWordsList.add(item.getResult())
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
        val recogResult: RecogResult = KBInputEngineInstance.get().multiQuery(query)
        val items: ArrayList<RecogResultItem>? = recogResult.getRecogResultItems()
        mCandidateWordsList.clear()
        synchronized(this) {
            for (item in items!!) {
                mCandidateWordsList.add(item.getResult())
            }
            if (mCandidateWordsList.size == 0) mCandidateWordsList.add(mComposingData.composingText.toString())
        }
    }

    private fun associateAndUpdateCandidate(query: String) {
        if (query.isEmpty()) return
        val associateList = HWInputEngineInstance.get().associateQuery(query)
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
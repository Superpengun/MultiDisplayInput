package com.zqy.hci.input

import android.util.Log
import android.view.KeyEvent
import com.zqy.hci.listener.HciCloudInputConnection
import com.zqy.sdk.keyboard.RecogResult
import com.zqy.sdk.keyboard.RecogResultItem
import com.zqy.sdk.manager.HWSDKWrapperManager
import com.zqy.sdk.manager.KBSDKWrapperManager

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
    private lateinit var mHWManager: HWSDKWrapperManager
    private lateinit var mKBManager: KBSDKWrapperManager
    private var mLetterCheck: Boolean = false

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
        mLetterCheck = false
    }

    /**
     * 在长度限制内进行查询
     */
    override fun appendRecognizeChar(inputChar: Char) {
        try {
            mLetterCheck = false
            if (composingExceedLengthCheck()) return
            if (mCompoing.getLastCharOfCompoingToQuery() == '\'' && inputChar == '\'') {
                return
            }
            if (inputChar == '\'' && mCompoing.getCompoingToQuery().isEmpty()) {
                return
            }
            clearCandidateWordsList()
            mCompoing.appendLetter(inputChar)
            queryAndUpdateCandidate(mCompoing.getCompoingToQuery())
            mHciCloudInputConnection.updateComposingText(mCompoing.getComposingToCommiting())
            notifyEnterStateChange()
            notifyCandidateChange()
        } catch (e: ArrayIndexOutOfBoundsException) {
            Log.e(TAG, "appendRecognizeChar: " + e.printStackTrace())
        }
    }

    override fun appendRecognizePoints(points: ShortArray) {
    }

    override fun appendSentence(str: String?) {
        mLetterCheck = false
        mHciCloudInputConnection.commitComposing(str)
        mHciCloudInputConnection.finishComposingText()
    }


    /**
     * 处理删除删除按键
     */
    override fun sendDelete() {
        mLetterCheck = false
        if (checkComposingLength()) {
            clearComposing()
            mHciCloudInputConnection.finishComposingText()
            mHciCloudInputConnection.updateComposingText("")
            oldList.clear()
            mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_DEL)
            clearCandidateWordsList()
            notifyCandidateChange()
            mInputMethodListener?.onHideView()
        } else {
            mCompoing.del()
            queryAndUpdateCandidate(mCompoing.getCompoingToQuery())
            mHciCloudInputConnection.updateComposingText(mCompoing.getComposingToCommiting())
            if (mCompoing.getCompoingToQuery().isEmpty()) {
                clearCandidateWordsList()
                clearComposing()
                mInputMethodListener?.onHideView()
            }
            notifyEnterStateChange()
            notifyCandidateChange()
        }
    }

    /**
     * 当前有候选词的时候，提交当前composing上屏
     * 无候选词汇提交\
     */
    override fun sendEnter() {
        mLetterCheck = false
        if (checkComposingLength()) {
            mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_ENTER)
            return
        }
        mHciCloudInputConnection.commitString(
            mCompoing.getComposingToCommiting().replace("'", ""),
            1
        );
        clearComposing()
        mHciCloudInputConnection.updateComposingText("")
        notifyEnterStateChange()
        clearCandidateWordsList()

        notifyCandidateChange()
    }

    /**
     * 处理空格按键 有候选情况下提交 第一个候选上屏
     */
    override fun sendSpace() {
        mLetterCheck = false
        if (checkComposingLength()) {
            mHciCloudInputConnection.keyDownUp(KeyEvent.KEYCODE_SPACE)
            clearCandidateWordsList()
            notifyCandidateChange()
            return
        }
        if (mCandidateWordsList.size > 0) {
            sendCandidateChosen(0)
        } else {
            mCompoing.clear()
            mHciCloudInputConnection.updateComposingText("")
            notifyEnterStateChange()
            clearCandidateWordsList()
            notifyCandidateChange()
        }
    }

    /**
     * 根据候选择去进行联想词汇查询
     */
    override fun sendCandidateChosen(index: Int) {
        if (mCompoing.getComposingToCommiting().isNotEmpty()) {
            val candidateCount = mCandidateWordsList.size
            if (index < candidateCount) {
                val chosenWord = mCandidateWordsList[index]
                if (mRecogResult.getRecogResultItems().size == 0) {
                    sendEnter()
                    return
                } else {
                    val word: String? =
                        mRecogResult.getRecogResultItems()[index].getResult()
                    val symbols: String? =
                        mRecogResult.getRecogResultItems()[index].getSymbols()
                    if (word != null) {
                        if (symbols != null) {
                            mCompoing.appendSelectedItem(word, symbols)
                        }
                    }
                }
                if (mCompoing.isMatchComplete()) {   //匹配结束
                    //获取用户输入的完整结果
                    val userData = mCompoing.getCurrentOutput()
                    if (userData.isNotEmpty() && !mLetterCheck) {
                        mRecogResult.getRecogResultItems()[index].getSymbols()?.let {
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
                        if (mLetterCheck) {
                            mHciCloudInputConnection.commitString(
                                chosenWord,
                                1
                            )
                        } else {
                            mHciCloudInputConnection.commitString(
                                mCompoing.getComposingToCommiting(),
                                1
                            )
                            Log.i(
                                TAG,
                                "handleCandidateChosen()  commitString:  " + mCompoing.getComposingToCommiting()
                            )
                        }
                    }
                    mCompoing.clear()
                    //根据选择的候选区获取联想词
                    associateAndUpdateCandidate(chosenWord)
                    notifyCandidateChange()
                    mHciCloudInputConnection.updateComposingText("")
                    notifyEnterStateChange()
                } else {     //匹配未结束
                    clearCandidateWordsList()
                    queryAndUpdateCandidate(mCompoing.getCompoingToQuery())
                    notifyCandidateChange()

                    mHciCloudInputConnection.updateComposingText(mCompoing.getComposingToCommiting())
//                        mCompoing.clear()
                }
            } else {
                Log.e(TAG, "handleCandidateChosen index error")
            }
        } else { //在联想中
            if (index >= mCandidateWordsList.size) {
                return
            }
            mHciCloudInputConnection.commitString(mCandidateWordsList[index], 1)
            associateAndUpdateCandidate(mCandidateWordsList[index])
            notifyCandidateChange()
        }
    }

    /**
     * 处理符号输入
     */
    override fun sendSymbol(symbol: String?) {
        mLetterCheck = false
        if (checkComposingLength()) {
            mHciCloudInputConnection.commitString(symbol, 1)
            clearCandidateWordsList()
            notifyCandidateChange()
            return
        }
        getWordWithSymbol()
        mHciCloudInputConnection.commitString(mCompoing.getCurrentOutput() + symbol, 1)
        mHciCloudInputConnection.finishComposingText()
        mHciCloudInputConnection.updateComposingText("")
        clearComposing()
        notifyEnterStateChange()
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

    override fun switchLetterResult() {
        oldList.clear()
        oldList.addAll(mCandidateWordsList)
        wordToLetterCheck()
        notifyCandidateChange()
    }

    override fun switchWordResult() {
        letterToWordCheck()
        notifyCandidateChange()
    }

    fun setHWSDKWrapperManager(m: HWSDKWrapperManager) {
        mHWManager = m
    }

    fun setKBSDKWrapperManager(m: KBSDKWrapperManager) {
        mKBManager = m
    }

    /**
     * 获取下一页候选词汇
     */
    override fun getNextPage() {
    }

    /**
     * 清空数据
     */
    override fun onReset() {
        mLetterCheck = false
        mHciCloudInputConnection.finishComposingText()
        clearComposing()
        notifyEnterStateChange()
        clearCandidateWordsList()
        mHciCloudInputConnection.updateComposingText("")
        super.notifyCandidateChange()
    }

    override fun onResetResult() {
        mLetterCheck = false
        clearComposing()
        notifyEnterStateChange()
        notifyCandidateChange()
        mHciCloudInputConnection.updateComposingText("")
        super.onResetResult()
    }

    override fun checkComposingLength(): Boolean {
        return mCompoing.getComposingToCommiting().isEmpty()
    }

    private fun queryAndUpdateCandidate(query: String) {
        if (query.isEmpty()) return
        mRecogResult = mKBManager.multiQuery(query)
        val items: ArrayList<RecogResultItem> = mRecogResult.getRecogResultItems()
        mCandidateWordsList.clear()
        if (items.isEmpty()) {
            mCandidateWordsList.add(query)
        } else {
            mCompoing.updateFirstCandidate(items[0].getResult(), items[0].getSymbols())
        }
        synchronized(this) {
            for (item in items) {
                mCandidateWordsList.add(item.getResult())
            }
        }
        oldList.clear()
        oldList.addAll(mCandidateWordsList)
    }

    private fun associateAndUpdateCandidate(query: String) {
        if (query.isEmpty()) return
        val associateList = mHWManager.associateQuery(query)
        mCandidateWordsList.clear()
        synchronized(this) {
            if (associateList != null) {
                mCandidateWordsList.addAll(associateList)
            }
        }
        oldList.clear()
        oldList.addAll(mCandidateWordsList)
    }

    private fun notifyEnterStateChange() {
        try {
            mInputMethodListener?.onSendComposingText(mCompoing.getCurrentOutput())
        } catch (e: StringIndexOutOfBoundsException) {
            Log.d(TAG, "notifyComposingChange OutOfBounds")
        }
    }

    private fun clearComposing() {
        mCompoing.clear()
    }

    private fun composingExceedLengthCheck(): Boolean {
        if (mCompoing.getComposingToCommiting().length > MAX_INPUT_LENGTH) {
            if (mCompoing.getComposingToCommiting().equals(VERSION_CODE, ignoreCase = true)) {
                clearCandidateWordsList()
                mCandidateWordsList.add(LogicControl.instance.getVersionName())
                notifyCandidateChange()
            }
            return true
        }
        return false
    }

    private fun wordToLetterCheck() {
        mLetterCheck = true
        val parentList: ArrayList<String?> = ArrayList<String?>()
        parentList.addAll(mCandidateWordsList)
        mCandidateWordsList.clear()
        Log.d(TAG, "wordToLetterCheck: ${parentList.size}")
        synchronized(this) {
            for (item in parentList) {
                if (item?.length!! < 2) {
                    mCandidateWordsList.add(item)
                }
            }
        }
    }

    private fun letterToWordCheck() {
        mLetterCheck = false
        mCandidateWordsList.clear()
        mCandidateWordsList.addAll(oldList)
    }

    private fun getWordWithSymbol() {
        if (mCompoing.getComposingToCommiting().isNotEmpty()) {
            val candidateCount = mCandidateWordsList.size
            if (0 < candidateCount) {
                mCandidateWordsList[0]
                if (mRecogResult.getRecogResultItems().size != 0) {
                    val word: String? =
                        mRecogResult.getRecogResultItems()[0].getResult()
                    val symbols: String? =
                        mRecogResult.getRecogResultItems()[0].getSymbols()
                    if (word != null) {
                        if (symbols != null) {
                            mCompoing.appendSelectedItem(word, symbols)
                        }
                    }
                }
            }
        }
    }

    private val setSum: MutableSet<Int> = HashSet()
    private val resultString: ArrayList<String?> = ArrayList()

}
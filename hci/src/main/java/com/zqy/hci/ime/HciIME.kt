package com.zqy.hci.ime

import android.R
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.zqy.hci.bean.FunctionKeyCode
import com.zqy.hci.input.LogicControl
import com.zqy.hci.listener.HciCloudInputConnection
import com.zqy.hci.listener.LogicControlListener
import com.zqy.hci.listener.OnCandidateActionListener
import com.zqy.hci.listener.OnKeyboardActionListener
import com.zqy.hci.service.AudioAndHapticFeedbackManager
import com.zqy.hci.service.Settings
import com.zqy.hci.utils.LayoutUtils.updateLayoutGravityOf
import com.zqy.hci.utils.LayoutUtils.updateLayoutHeightOf
import com.orhanobut.logger.Logger

class HciIME : InputMethodService(), OnKeyboardActionListener, OnCandidateActionListener,
    HciCloudInputConnection, LogicControlListener {
    var mInputViewContainer: View? = null
    private var mSettings: Settings? = null
    private var mLogicControlInit: Boolean = false

    override fun onCreate() {
        super.onCreate()
        AudioAndHapticFeedbackManager.init(this)
        InputViewController.instance.init(this)
        loadSettings()
    }

    override fun onDestroy() {
        InputViewController.instance.clear()
        super.onDestroy()
    }

    override fun onCreateInputView(): View {
        if (!mLogicControlInit){
            mLogicControlInit= LogicControl.instance.init(applicationContext, this, this)
        }
        mInputViewContainer = InputViewController.instance.createInputView(layoutInflater)
        return mInputViewContainer!!
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
    }

    override fun onFinishInput() {
        super.onFinishInput()
    }

    override fun onStartInputView(attribute: EditorInfo?, restarting: Boolean) {
        Logger.v(
            TAG,
            "onStartInputView(EditorInfo{imeOptions %d, inputType %d}, restarting %s",
            attribute?.imeOptions,
            attribute?.inputType,
            restarting
        )
        super.onStartInputView(attribute, restarting)
        InputViewController.instance.loadTheme()
        InputViewController.instance.handleEditorInfo(attribute)
        InputViewController.instance.setUIListener(this, this)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        LogicControl.instance.clear()
        super.onFinishInputView(finishingInput)
        InputViewController.instance.onFinishInputView()
    }

    override fun setInputView(view: View?) {
        super.setInputView(view)
        updateSoftInputWindowLayoutParameters()
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        try {
            if (candidatesStart != -1 && newSelEnd != candidatesEnd){
                LogicControl.instance.clear()
            }
            if (newSelStart ==0 && newSelEnd ==0){
                InputViewController.instance.onFinishInputView()
                LogicControl.instance.clear()
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
    }


    private fun loadSettings(){
        mSettings = Settings.instance
        Settings.init(this)
        mSettings?.loadSettings(this)
    }

    private fun updateSoftInputWindowLayoutParameters() {
        val window = window.window
        // Override layout parameters to expand {@link SoftInputWindow} to the entire screen.
        // See {@link InputMethodService#setinputView(View)} and
        // {@link SoftInputWindow#updateWidthHeight(WindowManager.LayoutParams)}.
        updateLayoutHeightOf(
            window,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        // This method may be called before {@link #setInputView(View)}.
        if (mInputViewContainer != null) {
            // In non-fullscreen mode, {@link InputView} and its parent inputArea should expand to
            // the entire screen and be placed at the bottom of {@link SoftInputWindow}.
            // In fullscreen mode, these shouldn't expand to the entire screen and should be
            // coexistent with {@link #mExtractedArea} above.
            // See {@link InputMethodService#setInputView(View) and
            // com.android.internal.R.layout.input_method.xml.
            val inputArea = window!!.findViewById<View>(R.id.inputArea)
            updateLayoutHeightOf(
                inputArea.parent as View,
                if (isFullscreenMode) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
            )
            updateLayoutGravityOf(
                inputArea.parent as View,
                Gravity.BOTTOM
            )
        }
    }

    companion object {
        private const val TAG = "HciIME"
    }


    override fun onCandidateSelected(index: Int, word: String) {
        Log.d(TAG,"onCandidateSelected,index:$index,word:$word")
        LogicControl.instance.sendCandChoosed(index)
    }

    override fun getMoreList() {
        Log.d(TAG,"getMoreList")
        LogicControl.instance.getNextPage()
    }

    /**
     * 点击候选区展开更多候选词
     */
    override fun onMore() {
        Log.d(TAG,"onMore")
        InputViewController.instance.showMoreCandidate(this)
    }

    override fun onClose() {
        Log.d(TAG,"onClose")
        requestHideSelf(0)
    }

    override fun onClear() {
        Log.d(TAG,"onClear")
    }

    override fun onBack() {
        InputViewController.instance.handleOnBack()
    }


    override fun onPress(primaryCode: Int) {
        AudioAndHapticFeedbackManager.getInstance().performAudioFeedback(primaryCode)
    }

    override fun onRelease(primaryCode: Int) {
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        when (primaryCode) {
            FunctionKeyCode.KEY_SHIFT -> {
                InputViewController.instance.setShifted()
                LogicControl.instance.setShiftState(InputViewController.instance.getShiftState())
            }
            FunctionKeyCode.KEY_SHIFT_LOCK -> {
                InputViewController.instance.lockShift()
                LogicControl.instance.setShiftState(InputViewController.instance.getShiftState())
            }
            FunctionKeyCode.SELECT_LANGUAGE -> {
                InputViewController.instance.showChooseLanOption()
                LogicControl.instance.clear()
            }
            FunctionKeyCode.KEY_BACK -> {
                InputViewController.instance.switchKb()
                LogicControl.instance.clear()
            }
            FunctionKeyCode.KEY_MULTI_QWERTY_NUM -> {
                InputViewController.instance.switchNumKb()
                LogicControl.instance.clear()
            }
            FunctionKeyCode.KEY_MUTTI_QWERTY_SYMBOL -> {
                InputViewController.instance.switchSymbolKb()
                LogicControl.instance.clear()
            }
            FunctionKeyCode.KEY_DEL -> {
                LogicControl.instance.sendDelete()
            }
            FunctionKeyCode.KEY_SPACE -> {
                LogicControl.instance.sendSpace()
            }
            FunctionKeyCode.KEY_ENTER -> {
                LogicControl.instance.sendEnter()
            }
            FunctionKeyCode.KEY_ARAB_ALPHABET ->{
                LogicControl.instance.sendSymbol("ٓ")
            }
            FunctionKeyCode.KEY_NORAWAY_KR ->{
                LogicControl.instance.sendSymbol("Kr")
            }
            else -> {
                LogicControl.instance.sendSymbol(primaryCode)
            }
        }
    }

    override fun onText(text: CharSequence?) {
        val isLockShift = InputViewController.instance.isLockShifted()
        val isShift = InputViewController.instance.isShifted()
        if (isShift && !isLockShift){
            InputViewController.instance.setShifted()
            LogicControl.instance.setShiftState(InputViewController.instance.getShiftState())
        }
        LogicControl.instance.query(text)
    }

    override fun swipeLeft() {

    }

    override fun swipeRight() {
    }

    override fun swipeDown() {
    }

    override fun swipeUp() {
    }

    override fun onLongPress(text: CharSequence?): Boolean {
        return false
    }

    override fun keyDownUp(keyEventCode: Int) {
        val ic = currentInputConnection
        if (ic != null) {
            if (keyEventCode == KeyEvent.KEYCODE_SPACE) {
                ic.commitText(" ", 1)
            } else if (keyEventCode == KeyEvent.KEYCODE_ENTER) {
                sendKeyChar('\n')
            } else {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyEventCode))
            }
        }
    }

    override fun commitString(str: String?, cursorPos: Int) {
        val ic = currentInputConnection
        if (ic != null) {
            if (str != null && str.isNotEmpty()) // 如果没有内容，那就不提交了，不然会删除选中的内容。提交空字符串
                ic.commitText(str, cursorPos)
        }
    }

    override fun commitComposingStr(str: String?) {
    }

    override fun commitComposing(str: String?) {
        val ic = currentInputConnection
        ic?.setComposingText(str, 1)
    }

    override fun finishComposingText() {
        val ic = currentInputConnection
        ic?.finishComposingText()

    }

    override fun onUpdateComposingText(composingText: String) {

    }

    override fun onUpdateCandidateWordsList(upDateCandidateWordsList: List<String>) {
        InputViewController.instance.setCandidateData(ArrayList(upDateCandidateWordsList))
    }
}
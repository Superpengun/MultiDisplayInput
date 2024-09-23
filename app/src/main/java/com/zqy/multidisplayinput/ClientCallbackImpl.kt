/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zqy.multidisplayinput

import android.inputmethodservice.MultiClientInputMethodServiceDelegate
import android.inputmethodservice.MultiClientInputMethodServiceDelegate.ClientCallback
import android.os.*
import android.util.Log
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.zqy.hci.bean.FunctionKeyCode
import com.zqy.hci.handwrite.OnStrokeActionListener
import com.zqy.hci.listener.*
import com.zqy.hci.service.AudioAndHapticFeedbackManager

internal class ClientCallbackImpl(
    private val mInputMethod: MultiClientInputMethod,
    private val mDelegate: MultiClientInputMethodServiceDelegate,
    private val mSoftInputWindowManager: SoftInputWindowManager,
    private val mClientId: Int,
    private val mUid: Int,
    private val mPid: Int,
    private val mSelfReportedDisplayId: Int
) : ClientCallback, OnKeyboardActionListener, OnNumKeyboardActionListener,
    OnCandidateActionListener, HciCloudInputConnection,
    OnStrokeActionListener,
    LogicControlListener,
    OnSymbolActionListener,
    OnPlateActionListener,
    OnEmojiActionListener,
    OnNewPlateActionListener {
    val dispatcherState: KeyEvent.DispatcherState
    val looper: Looper
    private var mCurrentInputConnection: InputConnection? = null
    private var mEditorInfo: EditorInfo? = null
    private var mCandidateLength = 0
    override fun onAppPrivateCommand(action: String?, data: Bundle?) {}
    override fun onDisplayCompletions(completions: Array<CompletionInfo>?) {}
    override fun onFinishSession() {
        if (DEBUG) {
            Log.v(TAG, "onFinishSession clientId=$mClientId")
        }
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        // SoftInputWindow also needs to be cleaned up when this IME client is still associated with
        // it.
        if (mClientId == window.clientId) {
            window.onFinishClient()
        }
        mCurrentInputConnection = null
        mEditorInfo = null
    }

    override fun onHideSoftInput(flags: Int, resultReceiver: ResultReceiver?) {
        if (DEBUG) {
            Log.v(TAG, "onHideSoftInput clientId=$mClientId flags=$flags")
        }
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        // Seems that the Launcher3 has a bug to call onHideSoftInput() too early so we cannot
        // enforce clientId check yet.
        // TODO: Check clientId like we do so for onShowSoftInput().
        onClose()
    }

    override fun onShowSoftInput(flags: Int, resultReceiver: ResultReceiver?) {
        if (DEBUG) {
            Log.v(TAG, "onShowSoftInput clientId=$mClientId flags=$flags")
        }
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        if (mClientId != window?.clientId) {
            Log.w(
                TAG, "onShowSoftInput() from a background client is ignored."
                        + " windowClientId=" + window?.clientId
                        + " clientId=" + mClientId
            )
            return
        }
        if (!window.isShowing) {
            window.show()
        }
        window.mSwitcher.showStrokeView()
    }

    override fun onStartInputOrWindowGainedFocus(
        inputConnection: InputConnection?,
        editorInfo: EditorInfo?, startInputFlags: Int, softInputMode: Int, targetWindowHandle: Int
    ) {
        if (DEBUG) {
            Log.v(
                TAG, "onStartInputOrWindowGainedFocus clientId=" + mClientId
                        + " editorInfo=" + editorInfo
                        + " startInputFlags="
                        + InputMethodDebug.startInputFlagsToString(startInputFlags)
                        + " softInputMode=" + InputMethodDebug.softInputModeToString(softInputMode)
                        + " targetWindowHandle=" + targetWindowHandle
            )
        }
        val state = softInputMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE
        val forwardNavigation =
            softInputMode and WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION != 0

        val window =
            mSoftInputWindowManager.getOrCreateSoftInputWindow(mSelfReportedDisplayId) ?: return
        if (window.targetWindowHandle != targetWindowHandle) {
            // Target window has changed.  Report new IME target window to the system.
            mDelegate.reportImeWindowTarget(
                mClientId, targetWindowHandle, window.window!!.attributes.token
            )
        }
        val lastClientId = mInputMethod.mDisplayToLastClientId[mSelfReportedDisplayId]
        if (lastClientId != mClientId) {
            // deactivate previous client and activate current.
            mDelegate.setActive(lastClientId, false /* active */)
            mDelegate.setActive(mClientId, true /* active */)
        }
        window.mSwitcher.mImeEditor.setListener(this, this)
        window.mSwitcher.onFinishInputView()
        if (mEditorInfo?.fieldId != editorInfo?.fieldId) {
            window.mSwitcher.handleEditorInfo(editorInfo)
        }
        window.mSwitcher.setUIListener(this, this, this, this, this, this,this)
        mEditorInfo = editorInfo
        window.mSwitcher.resetShifted()
        window.mSwitcher.mImeEditor.setShiftState(0)
        window.mSwitcher.mImeEditor.clear()
        if (inputConnection == null || editorInfo == null) {
            // Placeholder InputConnection case.
            if (window.clientId == mClientId) {
                // Special hack for temporary focus changes (e.g. notification shade).
                // If we have already established a connection to this client, and if a placeholder
                // InputConnection is notified, just ignore this event.
            } else {
                window.onDummyStartInput(mClientId, targetWindowHandle)
            }
        } else {
            mCurrentInputConnection = inputConnection
            window.onStartInput(mClientId, targetWindowHandle, inputConnection)
        }
        when (state) {
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE -> if (forwardNavigation) {
                window.show()
            }
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE -> window.show()
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN -> if (forwardNavigation) {
                window.hide()
            }
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN -> window.hide()
        }
        mInputMethod.mDisplayToLastClientId.put(mSelfReportedDisplayId, mClientId)
    }

    override fun onToggleSoftInput(p0: Int, p1: Int) {
    }

    override fun onUpdateCursorAnchorInfo(info: CursorAnchorInfo) {}
    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
//        Log.d(
//            TAG,
//            "onUpdateSelection:  " +
//                    "\noldSelStart is $oldSelStart " +
//                    "\noldSelEnd is $oldSelEnd" +
//                    "\nnewSelStart is $newSelStart " +
//                    "\nnewSelEnd is $newSelEnd  " +
//                    "\ncandidatesStart is $candidatesStart  " +
//                    "\ncandidatesEnd is $candidatesEnd"
//        )
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        if (candidatesEnd - candidatesStart > -1) {
            mCandidateLength = candidatesEnd - candidatesStart
        }
        if (candidatesStart != -1 && newSelEnd != candidatesEnd) {
            window?.mSwitcher?.mImeEditor?.clear()
        }
//        if (newSelEnd != candidatesEnd && mCandidateLength <=
//            window?.mSwitcher?.mImeEditor?.getComposingDataLength()!!
//            && newSelEnd < oldSelEnd
//        ) {
//            window.mSwitcher.mImeEditor.clear()
//        }
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (DEBUG) {
            Log.v(
                TAG, "onKeyDown clientId=" + mClientId + " keyCode=" + keyCode
                        + " event=" + event
            )
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
            if (window != null && window.isShowing) {
                event.startTracking()
                return true
            }
        }
        return false
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        return false
    }

    override fun onKeyMultiple(keyCode: Int, event: KeyEvent): Boolean {
        return false
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (DEBUG) {
            Log.v(
                TAG, "onKeyUp clientId=" + mClientId + "keyCode=" + keyCode
                        + " event=" + event
            )
        }
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking && !event.isCanceled) {
            val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
            if (window != null && window.isShowing) {
                window.hide()
                return true
            }
        }
        return false
    }

    override fun onTrackballEvent(event: MotionEvent): Boolean {
        return false
    }

    companion object {
        private const val TAG = "ClientCallbackImpl"
        private const val DEBUG = true
    }

    init {
        dispatcherState = KeyEvent.DispatcherState()
        // For simplicity, we use the main looper for this sample.
        // To use other looper thread, make sure that the IME Window also runs on the same looper
        // and introduce an appropriate synchronization mechanism instead of directly accessing
        // MultiClientInputMethod#mDisplayToLastClientId.
        looper = Looper.getMainLooper()
    }

    override fun onCandidateSelected(index: Int, word: String) {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.mImeEditor?.sendCandChoosed(index)
        window?.mSwitcher?.handleOnBack()
    }

    override fun getMoreList() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.mImeEditor?.getNextPage()
    }

    override fun onMore() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.showMoreCandidate(this)
    }

    override fun onClose() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        if (window != null && window.isShowing) {
            window.mSwitcher.onFinishInputView()
            window.mSwitcher.mImeEditor.clear()
            window.hide()
        }
    }

    override fun onClear() {

    }

    override fun onBack() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.handleOnBack()
    }

    override fun onHW() {
        onKey(FunctionKeyCode.KEY_LAST_HWR, null)
//        onKey(FunctionKeyCode.SELECT_LANGUAGE, null)
    }

    override fun onKeyboard() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.mImeEditor?.setShiftState(0)
        onKey(FunctionKeyCode.KEY_LAST_KB, null)
//        onKey(FunctionKeyCode.SELECT_LANGUAGE, null)
    }

    override fun onEmoji() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.showEmojiView()
//        onKey(FunctionKeyCode.SELECT_LANGUAGE, null)
    }

    override fun onNumberPlate() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window!!.mSwitcher.showParkingKeyboard(window)
    }

    override fun onLetter() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.mImeEditor?.switchLetterResult()
    }

    override fun onWord() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.mImeEditor?.switchWordResult()
    }

    override fun onDelete() {
        onKey(FunctionKeyCode.KEY_DEL, null)
    }

    override fun onCandidateReset() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.mImeEditor?.reset()
        val ic = getCurrentInputConnection() ?: return
        ic.deleteSurroundingText(100, 100)
    }

    override fun onCandidateAction() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.mImeEditor?.sendEnter()
    }

    override fun onCandidateSpace() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.mImeEditor?.sendSpace()
    }

    override fun onPlateSelected(index: Int, word: String) {
        val ic = getCurrentInputConnection()
        ic?.commitText(word, 1)
        AudioAndHapticFeedbackManager.getInstance().performAudioFeedback(0)
    }

    override fun onPlateBack() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        onKey(FunctionKeyCode.KEY_PLATE_CONTENT, null)
    }

    override fun onPlateDelete() {
        val ic = getCurrentInputConnection() ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
    }

    override fun onPlateReset() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.mImeEditor?.reset()
        val ic = getCurrentInputConnection() ?: return
        ic.deleteSurroundingText(100, 100)
    }

    override fun onPlateAction() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.mImeEditor?.sendEnter()
    }

    override fun onPress(primaryCode: Int) {
        AudioAndHapticFeedbackManager.getInstance().performAudioFeedback(primaryCode)
    }

    override fun onRelease(primaryCode: Int) {
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        when (primaryCode) {
            FunctionKeyCode.KEY_SHIFT -> {
                window.mSwitcher.setShifted()
                window.mSwitcher.mImeEditor.setShiftState(window.mSwitcher.getShiftState())
            }
            FunctionKeyCode.KEY_SHIFT_LOCK -> {
                window.mSwitcher.lockShift()
                window.mSwitcher.mImeEditor.setShiftState(window.mSwitcher.getShiftState())
            }
            FunctionKeyCode.KEY_MUTTI_QWERTY_SYMBOL -> {
                window.mSwitcher.showSymbolView()
            }
            FunctionKeyCode.KEY_DEL -> {
                window.mSwitcher.mImeEditor.sendDelete()
            }
            FunctionKeyCode.KEY_SPACE -> {
                window.mSwitcher.mImeEditor.sendSpace()
            }
            FunctionKeyCode.KEY_ENTER -> {
                window.mSwitcher.mImeEditor.sendEnter()
            }
            FunctionKeyCode.KEY_ARAB_ALPHABET -> {
                window.mSwitcher.mImeEditor.sendSymbol("ٓ")
            }
            FunctionKeyCode.KEY_NORAWAY_KR -> {
                window.mSwitcher.mImeEditor.sendSymbol("Kr")
            }
            FunctionKeyCode.KEY_VOICE -> {
                window.mSwitcher.showVoiceView()
            }
            FunctionKeyCode.KEY_CN_QWERTY -> {
                window.mSwitcher.mImeEditor.clear()
                window.mSwitcher.switchKb(1)
            }
            FunctionKeyCode.KEY_CN_HW -> {
                window.mSwitcher.mImeEditor.clear()
                window.mSwitcher.switchKb(2)
            }
            FunctionKeyCode.KEY_EN_QWERTY -> {
                window.mSwitcher.mImeEditor.clear()
                window.mSwitcher.switchKb(0)
            }
            FunctionKeyCode.KEY_EN_HW -> {
                window.mSwitcher.mImeEditor.clear()
                window.mSwitcher.switchKb(3)
            }
            FunctionKeyCode.KEY_LAST_KB -> {
                window.mSwitcher.mImeEditor.clear()
                window.mSwitcher.switchToLastKB()
            }
            FunctionKeyCode.KEY_LAST_HWR -> {
                window.mSwitcher.mImeEditor.clear()
                window.mSwitcher.switchToLastHWR()
            }
            FunctionKeyCode.SELECT_LANGUAGE -> {
                window.mSwitcher.mImeEditor.clear()
                window.mSwitcher.showChooseLanOption()
            }
            FunctionKeyCode.KEY_PLATE_CONTENT -> {
                window.mSwitcher.switchKb(0)
                window.mSwitcher.setShifted()
                window.mSwitcher.mImeEditor.setShiftState(window.mSwitcher.getShiftState())
            }
            FunctionKeyCode.KEY_TEST_ACTION -> {
                window.mSwitcher.showComposing()
            }
            else -> {
                window.mSwitcher.mImeEditor.sendSymbol(primaryCode)
            }
        }
    }

    override fun onText(text: CharSequence?) {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        val isLockShift = window.mSwitcher.isLockShifted()
        val isShift = window.mSwitcher.isShifted()
        if (isShift && !isLockShift) {
            window.mSwitcher.setShifted()
        }
        window.mSwitcher.mImeEditor.setShiftState(window.mSwitcher.getShiftState())
        window.mSwitcher.mImeEditor.query(text)
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

    override fun onNumText(text: CharSequence?) {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        window.mSwitcher.mImeEditor.sendSymbol(text.toString())
        AudioAndHapticFeedbackManager.getInstance().performAudioFeedback(0)
        window.mSwitcher.numberActionBack()
    }

    override fun keyDownUp(keyEventCode: Int) {
        val ic = getCurrentInputConnection()
        if (ic != null) {
            if (keyEventCode == KeyEvent.KEYCODE_SPACE) {
                ic.commitText(" ", 1)
            } else if (keyEventCode == KeyEvent.KEYCODE_ENTER) {
                clickEnter()
            } else {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyEventCode))
            }
        }
    }

    override fun commitString(str: String?, cursorPos: Int) {
        val ic = getCurrentInputConnection()
        if (ic != null) {
            if (str != null && str.isNotEmpty()) // 如果没有内容，那就不提交了，不然会删除选中的内容。提交空字符串
                ic.commitText(str, cursorPos)
        }
    }

    override fun commitComposingStr(str: String?) {
        val ic = getCurrentInputConnection()
        ic?.setComposingText(str, 1)
    }

    override fun commitComposing(str: String?) {
        val ic = getCurrentInputConnection()
        ic?.setComposingText(str, 1)
    }

    override fun finishComposingText() {
        val ic = getCurrentInputConnection()
        ic?.finishComposingText()
    }

    override fun getCandidateLength(): Int {
        return mCandidateLength
    }

    override fun updateComposingText(str: String) {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        window.mSwitcher.updateComposing(str)
    }

    override fun onUpdateComposingText(composingText: String) {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        if (composingText.isNotEmpty()) {
            window.mSwitcher.updateEnterTextInComposing()
        } else {
            window.mSwitcher.updateEnterTextOutComposing()
        }
    }

    override fun onUpdateCandidateWordsList(upDateCandidateWordsList: List<String>) {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        window.mSwitcher.setCandidateData(ArrayList(upDateCandidateWordsList))
    }

    override fun onHideView() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        window.mSwitcher.handleOnBack()
    }

    override fun onWriteEnd(stroke: ShortArray) {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        window.mSwitcher.mImeEditor.recog(stroke)
    }

    override fun onPointTouch() {
    }

    override fun onSymbolDelete() {
        onKey(FunctionKeyCode.KEY_DEL, null)
    }

    override fun onSymbolSelected(str: String?) {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        window.mSwitcher.mImeEditor.sendSymbol(str.toString())
        AudioAndHapticFeedbackManager.getInstance().performAudioFeedback(0)
    }

    override fun onSymbolBack() {
    }

    override fun onSymbolReset() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.mImeEditor?.reset()
        val ic = getCurrentInputConnection() ?: return
        ic.deleteSurroundingText(100, 100)
    }

    override fun onSymbolAction() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window?.mSwitcher?.mImeEditor?.sendEnter()
    }

    override fun onClickEmoji(symbol: String) {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        window.mSwitcher.mImeEditor.sendSymbol(symbol)
        AudioAndHapticFeedbackManager.getInstance().performAudioFeedback(0)
    }

    override fun onDeleteEmoji() {
        keyDownUp(KeyEvent.KEYCODE_DEL)
    }

    override fun onPlateCommit(word : String) {
        val ic = getCurrentInputConnection()
        ic?.commitText(word, 1)
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        window!!.mSwitcher.hideParkingKeyboard(window)
    }

    private fun getCurrentInputConnection(): InputConnection? {
        val ic: InputConnection? = mCurrentInputConnection
        return ic ?: mCurrentInputConnection
    }

    private fun getCurrentEditorInfo(): EditorInfo? {
        val ei: EditorInfo? = mEditorInfo
        return ei ?: mEditorInfo
    }

    private fun sendDownUpKeyEvents(keyEventCode: Int) {
        val ic = getCurrentInputConnection() ?: return
        val eventTime = SystemClock.uptimeMillis()
        ic.sendKeyEvent(
            KeyEvent(
                eventTime, eventTime,
                KeyEvent.ACTION_DOWN, keyEventCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD or KeyEvent.FLAG_KEEP_TOUCH_MODE
            )
        )
        ic.sendKeyEvent(
            KeyEvent(
                eventTime, SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP, keyEventCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD or KeyEvent.FLAG_KEEP_TOUCH_MODE
            )
        )
    }

    private fun clickEnter() {
        if (!sendDefaultEditorAction(true)) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
    }

    private fun sendDefaultEditorAction(fromEnterKey: Boolean): Boolean {
        val ei = getCurrentEditorInfo()
        if (ei != null &&
            (!fromEnterKey || ei.imeOptions and
                    EditorInfo.IME_FLAG_NO_ENTER_ACTION == 0) && ei.imeOptions and EditorInfo.IME_MASK_ACTION !=
            EditorInfo.IME_ACTION_NONE
        ) {
            // If the enter key was pressed, and the editor has a default
            // action associated with pressing enter, then send it that
            // explicit action instead of the key event.
            val ic = getCurrentInputConnection()
            ic?.performEditorAction(ei.imeOptions and EditorInfo.IME_MASK_ACTION)
            return true
        }
        return false
    }
}
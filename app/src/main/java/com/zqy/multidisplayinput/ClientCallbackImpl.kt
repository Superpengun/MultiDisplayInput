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
import android.os.Bundle
import android.os.Looper
import android.os.ResultReceiver
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.zqy.hci.bean.FunctionKeyCode
import com.zqy.hci.handwrite.OnStrokeActionListener
import com.zqy.hci.listener.HciCloudInputConnection
import com.zqy.hci.listener.LogicControlListener
import com.zqy.hci.listener.OnCandidateActionListener
import com.zqy.hci.listener.OnKeyboardActionListener
import com.zqy.hci.service.AudioAndHapticFeedbackManager

internal class ClientCallbackImpl(
    private val mInputMethod: MultiClientInputMethod,
    private val mDelegate: MultiClientInputMethodServiceDelegate,
    private val mSoftInputWindowManager: SoftInputWindowManager,
    private val mClientId: Int,
    private val mUid: Int,
    private val mPid: Int,
    private val mSelfReportedDisplayId: Int
) : ClientCallback, OnKeyboardActionListener, OnCandidateActionListener, HciCloudInputConnection,
    OnStrokeActionListener,
    LogicControlListener {
    val dispatcherState: KeyEvent.DispatcherState
    val looper: Looper
    var mCurrentInputConnection: InputConnection? = null
    override fun onAppPrivateCommand(action: String, data: Bundle) {}
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
    }

    override fun onHideSoftInput(flags: Int, resultReceiver: ResultReceiver?) {
        if (DEBUG) {
            Log.v(TAG, "onHideSoftInput clientId=$mClientId flags=$flags")
        }
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        // Seems that the Launcher3 has a bug to call onHideSoftInput() too early so we cannot
        // enforce clientId check yet.
        // TODO: Check clientId like we do so for onShowSoftInput().
        window.hide()
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
        window.show()
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
        window.mSwitcher.handleEditorInfo(editorInfo)
        window.mSwitcher.setUIListener(this, this, this)
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

    override fun onUpdateCursorAnchorInfo(info: CursorAnchorInfo) {}
    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
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

    fun getCurrentInputConnection(): InputConnection? {
        val ic: InputConnection? = mCurrentInputConnection
        return ic ?: mCurrentInputConnection
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
        onKey(FunctionKeyCode.KEY_HW, null)
    }

    override fun onKeyboard() {
        onKey(FunctionKeyCode.KEY_CN_QWERTY, null)
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
            FunctionKeyCode.SELECT_LANGUAGE -> {
                window.mSwitcher.showChooseLanOption()
                window.mSwitcher.mImeEditor.clear()
            }
            FunctionKeyCode.KEY_BACK -> {
                window.mSwitcher.switchKb()
                window.mSwitcher.mImeEditor.clear()
            }
            FunctionKeyCode.KEY_HW -> {
                window.mSwitcher.showChooseLanOption()
                window.mSwitcher.mImeEditor.clear()
            }
            FunctionKeyCode.KEY_MULTI_QWERTY_NUM -> {
                window.mSwitcher.switchNumKb()
                window.mSwitcher.mImeEditor.clear()
            }
            FunctionKeyCode.KEY_MUTTI_QWERTY_SYMBOL -> {
                window.mSwitcher.switchSymbolKb()
                window.mSwitcher.mImeEditor.clear()
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
            FunctionKeyCode.KEY_CN_QWERTY -> {
                window.mSwitcher.showChooseLanOption()
                window.mSwitcher.mImeEditor.clear()
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
            window.mSwitcher.mImeEditor.setShiftState(window.mSwitcher.getShiftState())
        }
        window.mSwitcher.mImeEditor.query(text)
    }

    override fun swipeLeft() {
        TODO("Not yet implemented")
    }

    override fun swipeRight() {
        TODO("Not yet implemented")
    }

    override fun swipeDown() {
        TODO("Not yet implemented")
    }

    override fun swipeUp() {
        TODO("Not yet implemented")
    }

    override fun onLongPress(text: CharSequence?): Boolean {
        TODO("Not yet implemented")
    }

    override fun keyDownUp(keyEventCode: Int) {
        val ic = getCurrentInputConnection()
        if (ic != null) {
            if (keyEventCode == KeyEvent.KEYCODE_SPACE) {
                ic.commitText(" ", 1)
            }
//            else if (keyEventCode == KeyEvent.KEYCODE_ENTER) {
//                sendKeyChar('\n')
//            }
            else {
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
    }

    override fun commitComposing(str: String?) {
        val ic = getCurrentInputConnection()
        ic?.setComposingText(str, 1)
    }

    override fun finishComposingText() {
        val ic = getCurrentInputConnection()
        ic?.finishComposingText()
    }

    override fun onUpdateComposingText(composingText: String) {
    }

    override fun onUpdateCandidateWordsList(upDateCandidateWordsList: List<String>) {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        window.mSwitcher.setCandidateData(ArrayList(upDateCandidateWordsList))
    }

    override fun onWriteEnd(stroke: ShortArray) {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId) ?: return
        window.mSwitcher.mImeEditor.recog(stroke)
    }

    override fun onPointTouch() {
    }
}
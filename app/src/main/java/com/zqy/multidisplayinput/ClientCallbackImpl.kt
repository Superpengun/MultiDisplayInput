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
import com.zqy.hci.ime.InputViewController
import com.zqy.hci.input.LogicControl
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
) : ClientCallback , OnKeyboardActionListener, OnCandidateActionListener, HciCloudInputConnection,
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
        InputViewController.instance.handleEditorInfo(editorInfo)
        InputViewController.instance.setUIListener(this,this)
        LogicControl.instance.setListener(this,this)
        mCurrentInputConnection = inputConnection
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
        TODO("Not yet implemented")
    }

    override fun getMoreList() {
        TODO("Not yet implemented")
    }

    override fun onMore() {
        TODO("Not yet implemented")
    }

    override fun onClose() {
        val window = mSoftInputWindowManager.getSoftInputWindow(mSelfReportedDisplayId)
        if (window != null && window.isShowing) {
            window.hide()
        }
    }

    override fun onClear() {
        TODO("Not yet implemented")
    }

    override fun onBack() {
        TODO("Not yet implemented")
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
        InputViewController.instance.setCandidateData(ArrayList(upDateCandidateWordsList))
    }
}
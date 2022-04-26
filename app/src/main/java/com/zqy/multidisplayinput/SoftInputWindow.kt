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

import android.app.Dialog
import android.content.Context
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.MultiClientInputMethodServiceDelegate
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import java.util.*

internal class SoftInputWindow(context: Context?, token: IBinder?) : Dialog(
    context!!, android.R.style.Theme_DeviceDefault_InputMethod
) {
    private val mKeyboardView: KeyboardView
    private val mQwertygKeyboard: Keyboard
    private val mSymbolKeyboard: Keyboard
    private val mSymbolShiftKeyboard: Keyboard
    var clientId = MultiClientInputMethodServiceDelegate.INVALID_CLIENT_ID
        private set
    var targetWindowHandle = MultiClientInputMethodServiceDelegate.INVALID_WINDOW_HANDLE
        private set
    val isQwertyKeyboard: Boolean
        get() = mKeyboardView.keyboard === mQwertygKeyboard
    val isSymbolKeyboard: Boolean
        get() {
            val keyboard = mKeyboardView.keyboard
            return keyboard === mSymbolKeyboard || keyboard === mSymbolShiftKeyboard
        }

    fun onFinishClient() {
        mKeyboardView.setOnKeyboardActionListener(sNoopListener)
        clientId = MultiClientInputMethodServiceDelegate.INVALID_CLIENT_ID
        targetWindowHandle = MultiClientInputMethodServiceDelegate.INVALID_WINDOW_HANDLE
    }

    fun onDummyStartInput(clientId: Int, targetWindowHandle: Int) {
        if (DEBUG) {
            Log.v(
                TAG, "onDummyStartInput clientId=" + clientId
                        + " targetWindowHandle=" + targetWindowHandle
            )
        }
        mKeyboardView.setOnKeyboardActionListener(sNoopListener)
        this.clientId = clientId
        this.targetWindowHandle = targetWindowHandle
    }

    fun onStartInput(clientId: Int, targetWindowHandle: Int, inputConnection: InputConnection?) {
        if (DEBUG) {
            Log.v(
                TAG, "onStartInput clientId=" + clientId
                        + " targetWindowHandle=" + targetWindowHandle
            )
        }
        this.clientId = clientId
        this.targetWindowHandle = targetWindowHandle
        mKeyboardView.setOnKeyboardActionListener(object : NoopKeyboardActionListener() {
            override fun onKey(primaryCode: Int, keyCodes: IntArray) {
                if (DEBUG) {
                    Log.v(
                        TAG, "onKey clientId=" + clientId + " primaryCode=" + primaryCode
                                + " keyCodes=" + Arrays.toString(keyCodes)
                    )
                }
                val isShifted = isShifted // Store the current state before resetting it.
                resetShift()
                when (primaryCode) {
                    Keyboard.KEYCODE_CANCEL -> hide()
                    Keyboard.KEYCODE_DELETE -> {
                        inputConnection!!.sendKeyEvent(
                            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
                        )
                        inputConnection.sendKeyEvent(
                            KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL)
                        )
                    }
                    Keyboard.KEYCODE_MODE_CHANGE -> handleSwitchKeyboard()
                    Keyboard.KEYCODE_SHIFT -> handleShift(isShifted)
                    else -> handleCharacter(inputConnection, primaryCode, isShifted)
                }
            }

            override fun onText(text: CharSequence) {
                if (DEBUG) {
                    Log.v(TAG, "onText clientId=$clientId text=$text")
                }
                if (inputConnection == null) {
                    return
                }
                inputConnection.commitText(text, 0)
            }
        })
    }

    fun handleSwitchKeyboard() {
        if (isQwertyKeyboard) {
            mKeyboardView.keyboard = mSymbolKeyboard
        } else {
            mKeyboardView.keyboard = mQwertygKeyboard
        }
    }

    val isShifted: Boolean
        get() = mKeyboardView.isShifted

    fun resetShift() {
        if (isSymbolKeyboard && isShifted) {
            mKeyboardView.keyboard = mSymbolKeyboard
        }
        mKeyboardView.isShifted = false
    }

    fun handleShift(isShifted: Boolean) {
        if (isSymbolKeyboard) {
            mKeyboardView.keyboard = if (isShifted) mSymbolKeyboard else mSymbolShiftKeyboard
        }
        mKeyboardView.isShifted = !isShifted
    }

    fun handleCharacter(inputConnection: InputConnection?, primaryCode: Int, isShifted: Boolean) {
        var primaryCode = primaryCode
        if (isQwertyKeyboard && isShifted) {
            primaryCode = Character.toUpperCase(primaryCode)
        }
        inputConnection!!.commitText(primaryCode.toChar().toString(), 1)
    }

    companion object {
        private const val TAG = "SoftInputWindow"
        private const val DEBUG = true
        private val sNoopListener: KeyboardView.OnKeyboardActionListener =
            NoopKeyboardActionListener()
    }

    init {
        val lp = window!!.attributes
        lp.type = WindowManager.LayoutParams.TYPE_INPUT_METHOD
        lp.title = "InputMethod"
        lp.gravity = Gravity.BOTTOM
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.token = token
        window!!.attributes = lp
        val windowSetFlags = (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val windowModFlags = (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_DIM_BEHIND
                or WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window!!.setFlags(windowSetFlags, windowModFlags)
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        mKeyboardView = layoutInflater.inflate(R.layout.input, null) as KeyboardView
        mQwertygKeyboard = Keyboard(context, R.xml.qwerty)
        mSymbolKeyboard = Keyboard(context, R.xml.symbols)
        mSymbolShiftKeyboard = Keyboard(context, R.xml.symbols_shift)
        mKeyboardView.keyboard = mQwertygKeyboard
        mKeyboardView.setOnKeyboardActionListener(sNoopListener)

        // TODO(b/158663354): Disabling keyboard popped preview key since it is currently broken.
        mKeyboardView.isPreviewEnabled = false
        layout.addView(mKeyboardView)
        setContentView(
            layout, ViewGroup.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
            )
        )

        // TODO: Check why we need to call this.
        window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}
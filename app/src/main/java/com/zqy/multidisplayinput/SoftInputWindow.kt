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
import android.view.*
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import com.zqy.hci.input.LogicControl
import com.zqy.hci.service.AudioAndHapticFeedbackManager
import com.zqy.multidisplayinput.window.Switcher
import java.util.*

internal class SoftInputWindow(context: Context?, token: IBinder?) : Dialog(
    context!!, android.R.style.Theme_DeviceDefault_InputMethod
) {
    lateinit var mSwitcher : Switcher
    var clientId = MultiClientInputMethodServiceDelegate.INVALID_CLIENT_ID
        private set
    var targetWindowHandle = MultiClientInputMethodServiceDelegate.INVALID_WINDOW_HANDLE
        private set

    var mInputViewContainer: View? = null

    fun onFinishClient() {
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
    }

    companion object {
        private const val TAG = "SoftInputWindow"
        private const val DEBUG = true
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

        context?.let {
            mSwitcher = Switcher()
            mSwitcher.init(it)
            AudioAndHapticFeedbackManager.init(it)
            if (!LogicControl.instance.mInitHciCloudSys){
                LogicControl.instance.init(it)
            }
        }
        mInputViewContainer = mSwitcher.createInputView(layoutInflater)
        mSwitcher.loadTheme()

        // TODO(b/158663354): Disabling keyboard popped preview key since it is currently broken.
        layout.addView(mInputViewContainer)
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
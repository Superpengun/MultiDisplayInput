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

import android.content.Context
import android.hardware.display.DisplayManager
import com.zqy.multidisplayinput.MultiClientInputMethod
import android.inputmethodservice.MultiClientInputMethodServiceDelegate
import com.zqy.multidisplayinput.SoftInputWindowManager
import android.inputmethodservice.MultiClientInputMethodServiceDelegate.ClientCallback
import android.util.SparseArray
import com.zqy.multidisplayinput.ClientCallbackImpl
import com.android.internal.inputmethod.StartInputFlags
import com.zqy.multidisplayinput.NoopKeyboardActionListener

internal class SoftInputWindowManager(
    private val mContext: Context,
    private val mDelegate: MultiClientInputMethodServiceDelegate
) {
    private val mSoftInputWindows = SparseArray<SoftInputWindow>()
    fun getOrCreateSoftInputWindow(displayId: Int): SoftInputWindow? {
        val existingWindow = mSoftInputWindows[displayId]
        if (existingWindow != null) {
            return existingWindow
        }
        val display = mContext.getSystemService(
            DisplayManager::class.java
        ).getDisplay(displayId) ?: return null
        val windowToken = mDelegate.createInputMethodWindowToken(displayId) ?: return null
        val displayContext = mContext.createDisplayContext(display)
        val newWindow = SoftInputWindow(displayContext, windowToken)
        mSoftInputWindows.put(displayId, newWindow)
        return newWindow
    }

    fun getSoftInputWindow(displayId: Int): SoftInputWindow {
        return mSoftInputWindows[displayId]
    }
}
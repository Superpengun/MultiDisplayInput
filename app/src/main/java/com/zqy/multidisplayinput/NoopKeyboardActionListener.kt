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

import android.inputmethodservice.KeyboardView
import com.zqy.multidisplayinput.MultiClientInputMethod
import android.inputmethodservice.MultiClientInputMethodServiceDelegate
import com.zqy.multidisplayinput.SoftInputWindowManager
import android.inputmethodservice.MultiClientInputMethodServiceDelegate.ClientCallback
import com.zqy.multidisplayinput.ClientCallbackImpl
import com.android.internal.inputmethod.StartInputFlags
import com.zqy.multidisplayinput.NoopKeyboardActionListener

/**
 * Provides the no-op implementation of [KeyboardView.OnKeyboardActionListener]
 */
internal open class NoopKeyboardActionListener : KeyboardView.OnKeyboardActionListener {
    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onKey(primaryCode: Int, keyCodes: IntArray) {}
    override fun onText(text: CharSequence) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
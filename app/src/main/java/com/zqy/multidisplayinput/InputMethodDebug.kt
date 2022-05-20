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

import com.zqy.multidisplayinput.MultiClientInputMethod
import android.inputmethodservice.MultiClientInputMethodServiceDelegate
import com.zqy.multidisplayinput.SoftInputWindowManager
import android.inputmethodservice.MultiClientInputMethodServiceDelegate.ClientCallback
import android.view.WindowManager
import com.zqy.multidisplayinput.ClientCallbackImpl
import com.android.internal.inputmethod.StartInputFlags
import java.util.*

/**
 * Provides useful methods for debugging.
 */
internal object InputMethodDebug {
    /**
     * Converts soft input flags to [String] for debug logging.
     *
     * @param softInputMode integer constant for soft input flags.
     * @return [String] message corresponds for the given `softInputMode`.
     */
    fun softInputModeToString(softInputMode: Int): String {
        val joiner = StringJoiner("|")
        val state = softInputMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE
        val adjust = softInputMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST
        val isForwardNav =
            softInputMode and WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION != 0
        when (state) {
            WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED -> joiner.add("STATE_UNSPECIFIED")
            WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED -> joiner.add("STATE_UNCHANGED")
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN -> joiner.add("STATE_HIDDEN")
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN -> joiner.add("STATE_ALWAYS_HIDDEN")
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE -> joiner.add("STATE_VISIBLE")
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE -> joiner.add("STATE_ALWAYS_VISIBLE")
            else -> joiner.add("STATE_UNKNOWN($state)")
        }
        when (adjust) {
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED -> joiner.add("ADJUST_UNSPECIFIED")
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE -> joiner.add("ADJUST_RESIZE")
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN -> joiner.add("ADJUST_PAN")
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING -> joiner.add("ADJUST_NOTHING")
            else -> joiner.add("ADJUST_UNKNOWN($adjust)")
        }
        if (isForwardNav) {
            // This is a special bit that is set by the system only during the window navigation.
            joiner.add("IS_FORWARD_NAVIGATION")
        }
        return joiner.setEmptyValue("(none)").toString()
    }

    /**
     * Converts start input flags to [String] for debug logging.
     *
     * @param startInputFlags integer constant for start input flags.
     * @return [String] message corresponds for the given `startInputFlags`.
     */
    fun startInputFlagsToString(startInputFlags: Int): String {
        val joiner = StringJoiner("|")
        if (startInputFlags and StartInputFlags.VIEW_HAS_FOCUS != 0) {
            joiner.add("VIEW_HAS_FOCUS")
        }
        if (startInputFlags and StartInputFlags.IS_TEXT_EDITOR != 0) {
            joiner.add("IS_TEXT_EDITOR")
        }
        if (startInputFlags and StartInputFlags.INITIAL_CONNECTION != 0) {
            joiner.add("INITIAL_CONNECTION")
        }
        return joiner.setEmptyValue("(none)").toString()
    }
}
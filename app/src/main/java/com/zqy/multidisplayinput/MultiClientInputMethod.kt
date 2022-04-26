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

import android.app.Service
import android.content.Intent
import android.hardware.display.DisplayManager
import android.inputmethodservice.MultiClientInputMethodServiceDelegate
import android.inputmethodservice.MultiClientInputMethodServiceDelegate.ServiceCallback
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.util.SparseIntArray
import android.view.Display

/**
 * A [Service] that implements multi-client IME protocol.
 */
class MultiClientInputMethod : Service(), DisplayManager.DisplayListener {
    // last client that had active InputConnection for a given displayId.
    @JvmField
    val mDisplayToLastClientId = SparseIntArray()

    // Mapping table from the display where IME is attached to the display where IME window will be
    // shown.  Assumes that missing display will use the same display for the IME window.
    var mInputDisplayToImeDisplay: SparseIntArray? = null
    internal lateinit var mSoftInputWindowManager: SoftInputWindowManager
    lateinit var mDelegate: MultiClientInputMethodServiceDelegate
    private lateinit var mDisplayManager: DisplayManager
    override fun onCreate() {
        if (DEBUG) {
            Log.v(TAG, "onCreate")
        }
        mInputDisplayToImeDisplay = buildInputDisplayToImeDisplay()
        mDelegate = MultiClientInputMethodServiceDelegate.create(this,
            object : ServiceCallback {
                override fun initialized() {
                    if (DEBUG) {
                        Log.i(TAG, "initialized")
                    }
                }

                override fun addClient(
                    clientId: Int, uid: Int, pid: Int,
                    selfReportedDisplayId: Int
                ) {
                    val imeDisplayId =
                        mInputDisplayToImeDisplay!![selfReportedDisplayId, selfReportedDisplayId]
                    val callback = ClientCallbackImpl(
                        this@MultiClientInputMethod, mDelegate,
                        mSoftInputWindowManager, clientId, uid, pid, imeDisplayId
                    )
                    if (DEBUG) {
                        Log.v(
                            TAG, "addClient clientId=" + clientId + " uid=" + uid
                                    + " pid=" + pid + " displayId=" + selfReportedDisplayId
                                    + " imeDisplayId=" + imeDisplayId
                        )
                    }
                    mDelegate.acceptClient(
                        clientId, callback, callback.dispatcherState,
                        callback.looper
                    )
                }

                override fun removeClient(clientId: Int) {
                    if (DEBUG) {
                        Log.v(TAG, "removeClient clientId=$clientId")
                    }
                }
            })
        mSoftInputWindowManager = SoftInputWindowManager(this, mDelegate)
    }

    override fun onDisplayAdded(displayId: Int) {
        mInputDisplayToImeDisplay = buildInputDisplayToImeDisplay()
    }

    override fun onDisplayRemoved(displayId: Int) {
        mDisplayToLastClientId.delete(displayId)
    }

    override fun onDisplayChanged(displayId: Int) {}
    override fun onBind(intent: Intent): IBinder? {
        if (DEBUG) {
            Log.v(TAG, "onBind intent=$intent")
        }
        mDisplayManager = applicationContext.getSystemService(
            DisplayManager::class.java
        )
        mDisplayManager.registerDisplayListener(this, Handler(Looper.getMainLooper()))
        return mDelegate.onBind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        if (DEBUG) {
            Log.v(TAG, "onUnbind intent=$intent")
        }
        mDisplayManager.unregisterDisplayListener(this)
        return mDelegate.onUnbind(intent)
    }

    override fun onDestroy() {
        if (DEBUG) {
            Log.v(TAG, "onDestroy")
        }
        mDelegate.onDestroy()
    }

    private fun buildInputDisplayToImeDisplay(): SparseIntArray {
        val context = applicationContext
        val config = context.resources.getStringArray(
            R.array.config_inputDisplayToImeDisplay
        )
        val inputDisplayToImeDisplay = SparseIntArray()
        val displays = context.getSystemService(
            DisplayManager::class.java
        ).displays
        for (item in config) {
            val pair = item.split("/").toTypedArray()
            if (pair.size != 2) {
                Log.w(TAG, "Skip illegal config: $item")
                continue
            }
            val inputDisplay = findDisplayId(displays, pair[0])
            val imeDisplay = findDisplayId(displays, pair[1])
            if (inputDisplay != Display.INVALID_DISPLAY && imeDisplay != Display.INVALID_DISPLAY) {
                inputDisplayToImeDisplay.put(inputDisplay, imeDisplay)
            }
        }
        return inputDisplayToImeDisplay
    }

    companion object {
        private const val TAG = "MultiClientInputMethod"
        private const val DEBUG = true
        private fun findDisplayId(displays: Array<Display>, regexp: String): Int {
            for (display in displays) {
                if (display.getUniqueId().matches(regexp.toRegex())) {
                    val displayId = display.displayId
                    if (DEBUG) {
                        Log.v(TAG, "$regexp matches displayId=$displayId")
                    }
                    return displayId
                }
            }
            Log.w(TAG, "Can't find the display of $regexp")
            return Display.INVALID_DISPLAY
        }
    }
}

private fun Display.getUniqueId(): String {
    return "";
}

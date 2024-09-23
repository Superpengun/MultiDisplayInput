package com.parking.keyboard

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.support.annotation.ColorInt
import android.view.Window
import com.parking.keyboard.KeyboardInputController.Companion.with
import com.parking.keyboard.engine.KeyboardEngine
import com.parking.keyboard.view.InputView
import com.parking.keyboard.view.KeyboardView

class PopupKeyboard {
    private val keyboardView: KeyboardView
    private var mController: KeyboardInputController? = null
    private var isDialog = false

    constructor(context: Context?) {
        keyboardView = KeyboardView(context)
    }

    constructor(
        context: Context?,
        @ColorInt bubbleTextColor: Int,
        okKeyBackgroundColor: ColorStateList?
    ) {
        keyboardView = KeyboardView(context)
        keyboardView.setBubbleTextColor(bubbleTextColor)
        keyboardView.setOkKeyTintColor(okKeyBackgroundColor)
    }

    fun attach(inputView: InputView, activity: Activity) {
        isDialog = false
        attach(inputView, activity.window)
    }

    fun attach(inputView: InputView, dialog: Dialog) {
        isDialog = true
        attach(inputView, dialog.window)
    }

    private fun attach(inputView: InputView, window: Window?) {
        if (mController == null) {
            mController = with(keyboardView, inputView)
            mController!!.useDefaultMessageHandler()
            inputView.addOnFieldViewSelectedListener { show(window) }
        }
    }

    val controller: KeyboardInputController
        get() = checkAttachedController()
    val keyboardEngine: KeyboardEngine
        get() = keyboardView.keyboardEngine

    fun show(activity: Activity) {
        show(activity.window)
    }

    fun show(window: Window?) {
        checkAttachedController()
        PopupHelper.showToWindow(window, keyboardView, isDialog)
    }

    fun dismiss(activity: Activity) {
        dismiss(activity.window)
    }

    fun dismiss(window: Window?) {
        checkAttachedController()
        PopupHelper.dismissFromWindow(window)
    }

    val isShown: Boolean
        get() = keyboardView.isShown

    private fun checkAttachedController(): KeyboardInputController {
        checkNotNull(mController) { "Try attach() first" }
        return mController!!
    }
}
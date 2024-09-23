package com.zqy.hci.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * @author:zhenqiyuan
 * @data:2022/11/8
 * @描述：
 * @package:com.zqy.hci.ui
 */
class VoiceViewManager {

    private var mVoicePopupWindow: VoicePopupWindow? = null
    private var mVoiceWaitPopupWindow: VoiceWaitPopupWindow? = null
    private var isShow: Boolean = false

    fun init(view: View) {
        isShow = mVoicePopupWindow?.isShowing == true
        if (isShow) {
            if (mVoiceWaitPopupWindow == null) {
                mVoiceWaitPopupWindow = VoiceWaitPopupWindow()
                mVoiceWaitPopupWindow?.initView(view)
            }
        } else {
            if (mVoicePopupWindow == null) {
                mVoicePopupWindow = VoicePopupWindow()
                mVoicePopupWindow?.initView(view)
                mVoicePopupWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mVoicePopupWindow?.isOutsideTouchable = true
                mVoicePopupWindow?.isTouchable = true
                mVoicePopupWindow?.setTouchInterceptor(object : View.OnTouchListener {
                    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                        dismiss()
                        return false
                    }
                })
            }
        }
    }

    fun show(
        listener: VoicePopupWindow.VoicePopupListener
    ) {
        if (isShow) {
            Log.d("VoiceManager", "mVoiceWaitPopupWindow")
            mVoiceWaitPopupWindow?.show()
        } else {
            mVoicePopupWindow?.show(listener)
            Log.d("VoiceManager", "mVoicePopupWindow")
            isShow = true
        }
    }

    fun dismiss() {
        if (mVoicePopupWindow?.isShowing == true) {
            Log.d("VoiceManager", "mVoicePopupWindow dismiss")
            mVoicePopupWindow?.dismiss()
        }
        if (mVoiceWaitPopupWindow?.isShowing == true) {
            Log.d("VoiceManager", "mVoiceWaitPopupWindow dismiss")
            mVoiceWaitPopupWindow?.dismiss()
        }
        isShow = false
    }
}
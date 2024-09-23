package com.zqy.hci.ui

import android.content.Context
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.sinovoice.ai.recognize.RecognizeResult
import com.zqy.hci.R
import com.zqy.hci.theme.ThemeState
import com.zqy.sdk.voiceview.RecognitionProgressView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch



/**
 * @author:zhenqiyuan
 * @data:2022/11/14
 * @描述：
 * @package:com.zqy.hci.ui
 */
class VoiceWaitPopupWindow : PopupWindow() {
    private var recognizeResult: RecognizeResult? = null
    private val TAG = VoicePopupWindow::class.java.simpleName
    private lateinit var tvAsrResult: TextView
    private var waveViewIsWaving: Boolean = false
    private lateinit var vwPopup: RecognitionProgressView
    private var timer: CountDownTimer? = null
    private lateinit var mContext: Context
    private lateinit var mParentView: View


    fun initView(view: View) {
        val resId: Int = R.layout.voice_pop_window
        mContext = view.context
        mParentView = view
        contentView = LayoutInflater.from(mContext).inflate(resId, null, false)
        setBackgroundDrawable(null)
        if (!ThemeState.isDefault) {
            contentView.setBackgroundColor(mContext.getColor(R.color.whitebg))
        }
        vwPopup = contentView.findViewById<RecognitionProgressView>(R.id.vw_popup)
        val vwPopupParams = vwPopup.layoutParams as ConstraintLayout.LayoutParams
        vwPopupParams.bottomMargin = mContext.resources.getDimension(R.dimen.voicemargin).toInt()
        vwPopup.layoutParams = vwPopupParams
        tvAsrResult = contentView.findViewById<TextView>(R.id.tv_popup_asr_result)
        val tvAsrResultParams = tvAsrResult.layoutParams as ConstraintLayout.LayoutParams
        tvAsrResultParams.topMargin = mContext.resources.getDimension(R.dimen.textmargin).toInt()
        tvAsrResult.layoutParams = tvAsrResultParams
        tvAsrResult.textSize = mContext.resources.getDimension(com.zqy.hci.R.dimen.textsize)
        if (!ThemeState.isDefault) {
            tvAsrResult.setTextColor(mContext.getColor(R.color.whitetextcolor))
        }
        initVoiceProgressView(50, 65)
        isClippingEnabled = false
        timer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                dismiss()
            }
        }
    }


    fun show() {
        tvAsrResult.textSize = mContext.resources.getDimension(R.dimen.textsize)
        tvAsrResult.text = "语音输入已在另一屏幕使用，请关闭后再在本屏幕使用"
        width = mContext.resources.getDimensionPixelSize(R.dimen.voice_width)
        height = mContext.resources.getDimensionPixelSize(R.dimen.voice_height)
        val x = mContext.resources.getDimension(R.dimen.voice_locateX).toInt()
        val y = mContext.resources.getDimension(R.dimen.voice_locateY).toInt()
        showAtLocation(mParentView, Gravity.LEFT or Gravity.TOP, x, y)
        startWaveView()
        timer?.start()
    }

    override fun dismiss() {
        GlobalScope.launch(Dispatchers.Main) {
            super.dismiss()
            stopWaveView()
            tvAsrResult.text = ""
            timer?.cancel()
        }
    }

    private fun stopWaveView() {
        if (waveViewIsWaving) {
            vwPopup.onEndOfSpeech()
            waveViewIsWaving = false
        }
    }

    private fun startWaveView() {
        if (waveViewIsWaving) {
            return
        }
        vwPopup.onBeginningOfSpeech()
        waveViewIsWaving = true
    }


    private fun initVoiceProgressView(barCount: Int, maxHeigh: Int) {
        var color: Int = mContext.getColor(R.color.defaultcolor)
        if (!ThemeState.isDefault) {
            color = mContext.getColor(R.color.whitecolor)
        }
        vwPopup.setBarsCount(barCount)
        vwPopup.setSingleColor(color)
        val radius = mContext.resources.getDimension(com.zqy.hci.R.dimen.radius)
        vwPopup.setCircleRadiusInDp(radius)
        val spacedp = mContext.resources.getDimension(com.zqy.hci.R.dimen.spacedp).toInt()
        vwPopup.setSpacingInDp(spacedp)
        vwPopup.setIdleStateAmplitudeInDp(1)
        vwPopup.setRotationRadiusInDp(10)
        //最大高度
        val max = mContext.resources.getDimension(com.zqy.hci.R.dimen.max).toInt()
        vwPopup.setBarMaxHeightInDp(max)
        vwPopup.play()
    }
}
package com.zqy.hci.ui

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.sinovoice.ai.AIEvent
import com.sinovoice.ai.AIEventListener
import com.sinovoice.ai.AIEventType
import com.sinovoice.ai.AIRecorder
import com.sinovoice.ai.recognize.RecognizeResult
import com.zqy.hci.R

import com.zqy.hci.theme.ThemeState
import com.zqy.sdk.voiceview.RecognitionProgressView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @author:zhenqiyuan
 * @data:2022/11/8
 * @描述：
 * @package:com.zqy.hci.ui
 */
class VoicePopupWindow : PopupWindow() {
    private var recognizeResult: RecognizeResult? = null
    private val TAG = VoicePopupWindow::class.java.simpleName
    private lateinit var tvAsrResult: TextView
    private var waveViewIsWaving: Boolean = false
    private var mListener: VoicePopupListener? = null
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
        vwPopup = contentView.findViewById(R.id.vw_popup)
        val vwPopupParams = vwPopup.layoutParams as ConstraintLayout.LayoutParams
        vwPopupParams.bottomMargin =
            mContext.resources.getDimension(com.zqy.hci.R.dimen.voicemargin).toInt()
        vwPopup.layoutParams = vwPopupParams
        tvAsrResult = contentView.findViewById(R.id.tv_popup_asr_result)
        val tvAsrResultParams = tvAsrResult.layoutParams as ConstraintLayout.LayoutParams
        tvAsrResultParams.topMargin =
            mContext.resources.getDimension(com.zqy.hci.R.dimen.textmargin).toInt()
        tvAsrResult.layoutParams = tvAsrResultParams
        tvAsrResult.textSize = mContext.resources.getDimension(com.zqy.hci.R.dimen.textsize)
        if (!ThemeState.isDefault) {
            tvAsrResult.setTextColor(mContext.getColor(R.color.whitetextcolor))
        }
        initVoiceProgressView(50, 65)
        isClippingEnabled = false
        timer = object : CountDownTimer(55 * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                mListener?.onLongTime()
                dismiss()
            }
        }
        initAsr()
    }


    fun show(
        listener: VoicePopupListener,
    ) {
        tvAsrResult.text = ""
        mListener = listener
        width = mContext.resources.getDimensionPixelSize(com.zqy.hci.R.dimen.voice_width)
        height = mContext.resources.getDimensionPixelSize(com.zqy.hci.R.dimen.voice_height)
        val x = mContext.resources.getDimension(com.zqy.hci.R.dimen.voice_locateX).toInt()
        val y = mContext.resources.getDimension(com.zqy.hci.R.dimen.voice_locateY).toInt()
        showAtLocation(mParentView, Gravity.LEFT or Gravity.TOP, x, y)
        initAsr()
        startWaveView()
        timer?.start()
    }

    override fun dismiss() {
        GlobalScope.launch(Dispatchers.Main) {
            super.dismiss()
            stopWaveView()
            tvAsrResult.text = ""
            timer?.cancel()
            mListener = null
            AIRecorder.get().setEventListener(null)
        }
    }

    interface VoicePopupListener {
        fun onVoiceEnd(text: String)
        fun onError()
        fun onLongTime()
    }


    private fun initAsr() {
        AIRecorder.get().init(mContext, null, false)
        setListener()
    }

    private fun setListener() {
        AIRecorder.get().setEventListener(object : AIEventListener{
            override fun onEvent(event: AIEvent) {
                when (event.eventType) {
                    AIEventType.RECOGNIZE_START -> {
                        recognizeResult = null
                    }

                    AIEventType.RECOGNIZE_RESULT -> {
                        recognizeResult =
                            Gson().fromJson(event.result, RecognizeResult::class.java)
                        vwPopup.handler?.post {
                            processVoiceData(recognizeResult?.result?.text!!)
                        }
                    }

                    AIEventType.RECOGNIZE_STOP -> {
                        if (recognizeResult?.result?.text.isNullOrEmpty()) return
                        mListener?.onVoiceEnd(recognizeResult?.result?.text!!)
                    }

                    AIEventType.RECORDER_RECOGNIZE_VOICE_DATA -> {
                        vwPopup.handler?.post {
                            updateVolume(event.result.toInt())
                        }
                    }

                    AIEventType.SDK_INIT_FAIL -> {
                        Log.d(TAG, "onEvent: 语音初始化异常，请检查网络")
                    }

                    else -> {}
                }
            }

        })
    }


    private fun processVoiceData(result: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Log.d(TAG, "processVoiceData: ${result}")
            if (result.length < 2) {
                tvAsrResult.text = mContext.getString(com.sinovoice.ai.R.string.none_result)
            } else {
                tvAsrResult.text = result
            }
            if (tvAsrResult.lineCount > tvAsrResult.maxLines) {
                val lineStartIndex = tvAsrResult.layout.getLineStart(tvAsrResult.lineCount - 2)
                val lineEndIndex = tvAsrResult.layout.getLineEnd(1)
                val lineCount = lineEndIndex - lineStartIndex
                val text = "..." + tvAsrResult.text.toString()
                    .substring(lineStartIndex, tvAsrResult.text.length)
            }
        }
    }

    fun releaseAsr() {
        AIRecorder.get().release()
    }

    private fun stopWaveView() {
        AIRecorder.get().stopRecognize(false)
        if (waveViewIsWaving) {
            vwPopup.onEndOfSpeech()
            waveViewIsWaving = false
        }
    }

    //更新UI
    private fun updateVolume(rms: Int) {
        var amplitude = rms.toFloat()
        amplitude /= 1.8f
        vwPopup.onRmsChanged(amplitude)
    }

    private fun startWaveView() {
        AIRecorder.get().startRecognize(false)
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
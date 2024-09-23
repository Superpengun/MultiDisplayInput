package com.sinovoice.ai

import android.content.Context
import android.util.Log
import com.orhanobut.logger.Logger
import com.sinovoice.sdk.asr.*
import com.sinovoice.ai.Constant.RECOGNIZE_THREAD
import com.sinovoice.ai.Constant.RECORD_THREAD
import com.sinovoice.ai.data.DataManager
import com.sinovoice.ai.nlu.NLUEventListener
import com.sinovoice.ai.nlu.NLUManager
import com.sinovoice.ai.nlu.NluResult
import com.sinovoice.ai.recognize.RecognizeManager
import com.sinovoice.ai.recorder.AndroidRecorder
import com.sinovoice.ai.recorder.AsrRecorderEvent
import com.sinovoice.ai.recorder.AudioRecordListener
import com.sinovoice.ai.sdk.SDKManager
import com.sinovoice.ai.services.AnswerResult
import com.sinovoice.ai.services.ServiceListener
import com.sinovoice.ai.services.ServiceManager
import com.sinovoice.ai.tts.TTSPlayerListener
import com.sinovoice.ai.tts.TTSPlayerManager
import com.sinovoice.ai.utils.GsonUtils
import com.sinovoice.ai.utils.VoiceFocusManager
import com.sinovoice.ai.utils.Volume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AIRecorder : IFreetalkHandler, AudioRecordListener, TTSPlayerListener, ServiceListener,
    NLUEventListener {
    companion object {
        private var instance: AIRecorder? = null
        private const val TAG = "AIRecorder"

        @Synchronized
        fun get(): AIRecorder {
            if (instance == null) {
                instance = AIRecorder()
            }
            return instance!!
        }
    }

    private var singleRecognize: Boolean = false
    private var needCancelRecognize: Boolean = false


    private lateinit var recognizeThread: Thread
    private var mContext: Context? = null
    private var mInit = false

    enum class State {
        UNINITIALIZED, IDLE, RECORDING, RECOGNIZING
    }

    private lateinit var recorderThread: Thread
    private var mEventListener: AIEventListener? = null


    fun init(context: Context, listener: AIEventListener?, callbackOnMainThread: Boolean = false) {
        setEventListener(null)
        if (mInit) return
        Logger.t(TAG).d("init: ")
        if (callbackOnMainThread) {
            mEventListener = listener
        } else {
            setEventListener(listener)
        }
        mContext = context
        mInit = if (!SDKManager.get().initSdk(context)) {
            Logger.t(TAG).e("sdk sys init error")
            notifyAIEvent(AIEventType.SDK_INIT_FAIL)
            false
        } else {
            //初始化操作
            VoiceFocusManager.getInstance().init(context)
            initAIEngine()
            notifyAIEvent(AIEventType.SDK_INIT)
            true
        }
    }

    fun setEventListener(listener: AIEventListener?) {
        mEventListener = listener
    }

    fun release() {
        Logger.t(TAG).d("release: ")
        releaseAIEngine()
        SDKManager.get().releaseSdk()
        mContext = null
        instance = null
        VoiceFocusManager.getInstance().release()
        setEventListener(null)
    }


    private fun initAIEngine() {
        RecognizeManager.get().initRecognize()
        DataManager.get().initData()
    }

    private fun releaseAIEngine() {
        Logger.t(TAG).d("releaseAIEngine: ")
        RecognizeManager.get().releaseRecognize()
    }

    /**
     * 开启ASR识别
     */
    fun startRecognize(continueRecognize: Boolean = true) {
        Logger.t(TAG).d("startRecognize")
        if (!AndroidRecorder.get().prepare(this)) {
            Logger.t(TAG).e("start AndroidRecorder: error")
            notifyAIEvent(AIEventType.RECORDER_ERROR)
            return
        }
        DataManager.get().clearData()
        recorderThread = Thread(Runnable {
            AndroidRecorder.get().startRecording()
        }, RECORD_THREAD)
        recorderThread.start()
        if (RecognizeManager.get().startSession(continueRecognize, this)) {
            recognizeThread = Thread(Runnable {
                RecognizeManager.get().startRecognize()
            }, RECOGNIZE_THREAD)
            recognizeThread.start()
            singleRecognize = !continueRecognize
            notifyAIEvent(AIEventType.RECOGNIZE_START)
        } else {
            AndroidRecorder.get().stopRecording()
            Logger.t(TAG)
                .e("startOneSentenceRecognize Fail session Failed")
            notifyAIEvent(AIEventType.RECOGNIZE_START_ERROR)
        }
        return
    }

    /**
     * 停止asr识别
     * @param stopDealResult 是否停止继续输出结果
     */
    fun stopRecognize(stopDealResult: Boolean = false) {
        Logger.t(TAG).d("stopRecognize: needResult $stopDealResult")
        needCancelRecognize = stopDealResult
        AndroidRecorder.get().stopRecording()
        RecognizeManager.get().stopRecognize(stopDealResult)
    }


    /**
     * 获取tts数据
     * @param text String tts文本
     * @param type String 音色 见TTSConstants
     */
    fun getTTSData(text: String, type: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val ttsResult = TTSPlayerManager.get().cloudTTSData(text, type)
            notifyAIEvent(AIEventType.TTS_RESULT, GsonUtils.toJson(ttsResult))
        }
    }

    fun playTTS(text: String, type: String, id: Int = 0, localTts: Boolean = false) {
        Logger.t(TAG).d("playTTS： $text")
        TTSPlayerManager.get().cloudTtsPlay(text = text, type = type, id = id)
    }

    fun playTTSSync(text: String, type: String, id: Int = 0, localTts: Boolean = false) {
        TTSPlayerManager.get().cloudTtsPlay(text = text, type = type, id = id)
    }

    fun stopTTSPlaySync(text: String, type: String) {

    }

    fun stopPlayTTS(cancel: Boolean = false) {
        Logger.t(TAG).d("stopPlayTTS")
        GlobalScope.launch(Dispatchers.Main) {
            TTSPlayerManager.get().ttsStop(cancel)
        }
    }

    fun startNLUSession(): Boolean {
        return NLUManager.get().startNLUSession()
    }

    fun stopNLUSession() {
        return NLUManager.get().stopNLUSession()
    }

    fun getNLUResult(question: String) {
        NLUManager.get().processNLU(question, this)
    }

    fun cancelNLU() {
        NLUManager.get().cancelNLU()
    }

    fun isTTSPlaying(): Boolean {
        return TTSPlayerManager.get().isPlaying()
    }

    /**
     * 获取客服回答
     * @param question String
     * @param domain String
     */
    fun getServiceAnswer(question: String) {
        ServiceManager.get().getAnswer(question, this)
    }

    override fun onRecordEvent(asrRecorderEvent: AsrRecorderEvent) {
        when (asrRecorderEvent) {
            AsrRecorderEvent.EVENT_RECORDER_BEGIN_RECORD -> {
                notifyAIEvent(AIEventType.RECORDER_START)
            }

            AsrRecorderEvent.EVENT_RECORDER_STOP_RECORD -> {
                notifyAIEvent(AIEventType.RECORDER_STOP)
            }

            else -> Unit
        }
    }

    override fun onRecordError(msg: String) {
        notifyAIEvent(AIEventType.RECORDER_ERROR, msg)
    }

    override fun onRecording(voiceData: ByteArray) {
        notifyAIEvent(
            AIEventType.RECORDER_RECOGNIZE_VOICE_DATA,
            GsonUtils.toJson(Volume.calculateVolume(voiceData, 16))
        )
    }

    override fun onEvent(session: FreetalkStream?, event: FreetalkEvent?) {
        when (event?.type()) {
            FreetalkEvent.VOICE_END -> {
                notifyAIEvent(AIEventType.RECOGNIZE_VAD_TAIL)
            }

            FreetalkEvent.VOICE_START -> {
                notifyAIEvent(AIEventType.RECOGNIZE_VAD_HEAD)
            }
        }
    }

    override fun onResult(session: FreetalkStream?, sentence: FreetalkResult?) {
        Log.d(TAG, "onResult: ${sentence.toString()}")
        notifyAIEvent(AIEventType.RECOGNIZE_RESULT, sentence.toString())
    }

    override fun onEnd(session: FreetalkStream?, errorCode: Int) {
        notifyAIEvent(AIEventType.RECOGNIZE_STOP, needCancelRecognize.toString())
        if (singleRecognize) {
            stopRecognize(false)
        }
        needCancelRecognize = false
    }

    override fun onError(session: FreetalkStream?, errorCode: Int) {
        notifyAIEvent(AIEventType.RECOGNIZE_ERROR, errorCode.toString())
    }

    override fun onStart(session: FreetalkStream?, errorCode: Int, p2: Array<out Warning>?) {
    }


    private fun notifyAIEvent(event: AIEventType, data: String = "") {
        Log.d(TAG, "notifyAIEvent: " + event.toString())
        mEventListener?.onEvent(AIEvent(event, data))
    }

    override fun onStartPlay() {
        VoiceFocusManager.getInstance().requestAudioFocus()
        notifyAIEvent(AIEventType.TTS_START)
    }

    override fun onPlayEnd(id: Int, isCancel: Boolean) {
        VoiceFocusManager.getInstance().abandonAudioFocus()
        if (isCancel) {
            notifyAIEvent(AIEventType.TTS_CANCEL, id.toString())
        } else {
            notifyAIEvent(AIEventType.TTS_STOP, id.toString())
        }
    }

    override fun onServiceResult(result: AnswerResult?) {
        if (result == null) {
            notifyAIEvent(AIEventType.SERVICE_ERROR, GsonUtils.toJson(result))
        } else {
            notifyAIEvent(AIEventType.SERVICE_RESULT, GsonUtils.toJson(result))
        }
    }

    override fun onNLUResult(result: NluResult) {
        notifyAIEvent(AIEventType.NLU_RESULT, GsonUtils.toJson(result))
    }
}




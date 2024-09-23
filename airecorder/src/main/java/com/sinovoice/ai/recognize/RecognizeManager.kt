package com.sinovoice.ai.recognize

import com.sinovoice.sdk.HciError
import com.sinovoice.sdk.SessionState
import com.sinovoice.sdk.asr.*
import com.sinovoice.sdk.audio.HciAudioBuffer
import com.sinovoice.sdk.audio.HciAudioMetrics
import com.sinovoice.sdk.audio.HciSampleFormat
import com.sinovoice.ai.sdk.SDKManager
import com.sinovoice.ai.data.DataManager
import com.sinovoice.ai.data.VoiceDataType
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class RecognizeManager private constructor() : IFreetalkHandler {


    private var cancel: Boolean = false
    private var endRecognize: Boolean = true
    private lateinit var hciAudioBuffer: HciAudioBuffer
    private var sessionStartResult = false
    private var sessionCountDown: CountDownLatch? = null
    private var handlerImp: IFreetalkHandler? = null

    companion object {
        private var session: FreetalkStream? = null
        private var instance: RecognizeManager? = null

        @Synchronized
        fun get(): RecognizeManager {
            if (instance == null) {
                instance = RecognizeManager()
            }
            return instance!!
        }
    }

    fun initRecognize() {
        session = FreetalkStream(SDKManager.get().getSDK(), CloudAsrConfig())
        return
    }

    fun startSession(continueRecognize: Boolean, handler: IFreetalkHandler): Boolean {
        hciAudioBuffer =
            HciAudioBuffer(getAudioData(), SDKConstants.DEFAULT_AUDIO_DATA_BUFFER_TIME)
        sessionStartResult = false
        sessionCountDown = CountDownLatch(1)
        handlerImp = handler
        var sessionConfig: FreetalkConfig
        if (continueRecognize) {
            sessionConfig = getSessionConfig()
        } else {
            sessionConfig = getShortSessionConfig()
        }
        session?.start(sessionConfig, hciAudioBuffer, this, true)
        sessionCountDown?.await(2000, TimeUnit.MILLISECONDS)
        return sessionStartResult
    }

    fun startRecognize() {
        endRecognize = false
        hciAudioBuffer.audioSink().startWrite(getAudioData())
        val audioSink = hciAudioBuffer.audioSink()
        val frameSize = getAudioData().frameSize()
        while (!endRecognize) {
            val recognizeData = DataManager.get().getRecognizeData()
            if (recognizeData.voiceTag == VoiceDataType.VOICE_DATA) {
                val byteArray = recognizeData.voiceData
                val buffer = ByteBuffer.allocateDirect(frameSize)
                buffer.position(0)
                buffer.put(byteArray)
                buffer.limit(byteArray.size)
                buffer.position(0)
                audioSink.write(buffer, false)
            } else {
                break
            }
        }
        hciAudioBuffer.audioSink().endWrite(cancel)
        stopSession(cancel)
    }

    fun stopRecognize(cancel: Boolean) {
        this.cancel = cancel
        endRecognize = true
        sessionCountDown = CountDownLatch(1)
        sessionCountDown?.await(200, TimeUnit.MILLISECONDS)
    }

    private fun stopSession(cancel: Boolean) {
        if (session?.state() == SessionState.RUNNING) {
            session?.stop(cancel)
        }
    }

    private fun endSession() {
        session?.close()
    }

    fun releaseRecognize() {
        endSession()
    }


    private fun getAudioData(): HciAudioMetrics {
        val metrics: HciAudioMetrics = HciAudioMetrics()
        metrics.channels = SDKConstants.DEFAULT_AUDIO_DATA_CHANNEL
        metrics.format = HciSampleFormat.S16LE
        metrics.sampleRate = SDKConstants.DEFAULT_SAMPLE_RATE
        metrics.frameTime = SDKConstants.DEFAULT_AUDIO_DATA_FRAME_TIME
        return metrics
    }

    private fun getLocalAsrConfig(): LocalAsrConfig {
        val localAsrConfig = LocalAsrConfig()
        localAsrConfig.setModelPath("/sdcard/sinovoicedata/model_common_20190722")
        return localAsrConfig
    }

    private fun getSessionConfig(): FreetalkConfig {
        val freetalkConfig = FreetalkConfig()
        freetalkConfig.setProperty(SDKConstants.DEFAULT_PROPERTY)
        freetalkConfig.setAudioFormat(SDKConstants.DEFAULT_AUDIO_FORMAT)
        freetalkConfig.setMode(FreetalkConfig.CONTINUE_STREAM_MODE)
        freetalkConfig.setAddPunc(true)
        freetalkConfig.setInterimResults(true)
        freetalkConfig.setMakeParagraph(true) //分段
        freetalkConfig.setTextSmooth(false) //文 本顺滑
        freetalkConfig.setDigitNorm(true) //数字归一化
        freetalkConfig.setSlice(200)
        freetalkConfig.setVadHead(0)
        freetalkConfig.setVadTail(700)
        freetalkConfig.setTimeout(SDKConstants.DEFAULT_TIME_OUT)
        return freetalkConfig
    }


    private fun getShortSessionConfig(): FreetalkConfig {
        val freetalkConfig = FreetalkConfig()
        freetalkConfig.setProperty(SDKConstants.DEFAULT_PROPERTY)
        freetalkConfig.setAudioFormat(SDKConstants.DEFAULT_AUDIO_FORMAT)
        freetalkConfig.setMode(FreetalkConfig.UTTERANCE_STREAM_MODE)
//        freetalkConfig.setAddPunc(true)
        freetalkConfig.setInterimResults(true)
//        freetalkConfig.setMakeParagraph(true) //分段
//        freetalkConfig.setTextSmooth(false) //文 本顺滑
        freetalkConfig.setDigitNorm(true) //数字归一化
        freetalkConfig.setSlice(200)
        freetalkConfig.setVadHead(0)
        freetalkConfig.setVadTail(1000)
        freetalkConfig.setTimeout(SDKConstants.DEFAULT_TIME_OUT)
        return freetalkConfig
    }


    override fun onStart(p0: FreetalkStream?, p1: Int, p2: Array<out Warning>?) {
        sessionStartResult = p1 == HciError.OK || (p1 == HciError.FAILED_PRECONDITION)
        sessionCountDown?.countDown()
        handlerImp?.onStart(p0, p1, p2)
    }

    override fun onEnd(p0: FreetalkStream?, p1: Int) {
        sessionCountDown?.countDown()
        handlerImp?.onEnd(p0, p1)
    }

    override fun onResult(p0: FreetalkStream?, p1: FreetalkResult?) {
        handlerImp?.onResult(p0, p1)
    }

    override fun onEvent(p0: FreetalkStream?, p1: FreetalkEvent?) {
        handlerImp?.onEvent(p0, p1)
    }

    override fun onError(p0: FreetalkStream?, p1: Int) {
        handlerImp?.onError(p0, p1)
    }

}
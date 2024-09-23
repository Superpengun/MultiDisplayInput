package com.sinovoice.ai.recorder

import android.annotation.SuppressLint
import android.media.*
import android.media.audiofx.AcousticEchoCanceler
import com.orhanobut.logger.Logger
import com.sinovoice.ai.data.DataManager
import com.sinovoice.ai.data.RecordData
import com.sinovoice.ai.data.VoiceDataType
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


@SuppressLint("MissingPermission")
internal class AndroidRecorder {

    private var needRecognize: Boolean = false
    private lateinit var mCountDownLatch: CountDownLatch
    private var endRecording: Boolean = true
    var voiceStateListener: AudioRecordListener? = null
    var bufferSize by Delegates.notNull<Int>()
    private val DEFAULT_SAMPLE_RATE = 16000


    companion object {
        private var instance: AndroidRecorder? = null
        private var acousticEchoCanceler: AcousticEchoCanceler? = null
        private const val TAG = "AndroidRecorder"

        @Synchronized
        fun get(): AndroidRecorder {
            if (instance == null) {
                instance =
                    AndroidRecorder()
            }
            return instance!!
        }
    }

    private val audioRecord: AudioRecord by lazy {
        bufferSize = AudioRecord.getMinBufferSize(
            DEFAULT_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            DEFAULT_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, bufferSize
        )
    }

    fun prepare(listener: AudioRecordListener): Boolean {
        voiceStateListener = listener
        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            return false
        }
        if (audioRecord.state == AudioRecord.RECORDSTATE_RECORDING) {
            Logger.t(TAG).e("already recording")
            return true
        }
//        initAEC()
        return true
    }


    fun getAudioRecorderState(): Int {
        return audioRecord.recordingState
    }

    fun getAudioSession(): Int {
        return audioRecord.audioSessionId
    }


    /**
     * 供持续运行采音
     */
    fun startRecording() {
        endRecording = false
        audioRecord.startRecording()
        if (audioRecord.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            Logger.t(TAG).d("record state device error")
            notifyDeviceError("录音机开启失败")
            return
        }
        notifyRecordEvent(AsrRecorderEvent.EVENT_RECORDER_BEGIN_RECORD)
        var len: Int
        val piece = ByteArray(4096)
        while (!endRecording) {
            len = audioRecord.read(piece, 0, piece.size)
            if (len > 0) {
                // 防止因为复用piece造成的音频覆盖问题，造成送去识别的音频不正常，这里clone一份再放置到队列中
                val clone = piece.clone()
                // 向数据管道中添加录音数据,若空间不足，则抛弃最早的数据片段以容纳最新数据
                notifyRecordEvent(piece)
                val cloneData = RecordData(clone)
                while (!DataManager.get().putRecognizeData(cloneData)) {
                    DataManager.get().pollRecognizeData()
                    notifyRecordEvent(AsrRecorderEvent.EVENT_RECORDER_DATA_FULL)
                }
            }
        }
        audioRecord.stop()
        val finalData = RecordData(ByteArray(0), VoiceDataType.END_VOICE_DATA)
        DataManager.get().putRecognizeData(finalData)
        notifyRecordEvent(AsrRecorderEvent.EVENT_RECORDER_STOP_RECORD)
        mCountDownLatch.countDown()
    }

    private fun initAEC() {
        if (AcousticEchoCanceler.isAvailable()) {
            if (acousticEchoCanceler == null) {
                acousticEchoCanceler = AcousticEchoCanceler.create(audioRecord.audioSessionId)
                acousticEchoCanceler?.enabled = true
            }
        } else {
            Logger.t(TAG).w("AEC not support")
        }
    }

    /**
     * 停止录音
     */
    fun stopRecording() {
        endRecording = true
        // 因为AsrRecorder先后调用录音机线程的stop和识别线程的stop方法，但是由于两个线程无法保证时序性，
        // 经常会出现startRecognize事件先于stopRecorder事件这种异常现象，因此为保证两个事件顺序正常，
        // 这里使用CountDownLatch来将stop的调用线程暂时阻塞，当录音线程的while循环结束后继续往下执行，
        // 这样就能保证录音机的线程先结束后才会调用识别线程的stop方法。
        mCountDownLatch = CountDownLatch(1)
        try {
            mCountDownLatch.await(100, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * 反初始化
     */
    fun release() {
        audioRecord.release()
    }


    /**
     * 录音设备问题，需要该回调中，复位录音和识别状态
     *
     * @param msg
     * 内部错误描述信息
     */
    private fun notifyDeviceError(msg: String) {
        voiceStateListener?.onRecordError(msg)
    }

    /**
     * 需要外部通知的事件
     * @param event
     * 录音机事件
     */
    private fun notifyRecordEvent(event: AsrRecorderEvent) {
        voiceStateListener?.onRecordEvent(event)
    }

    /**
     * 需要外部通知的事件
     * @param event
     * 录音机事件
     */
    private fun notifyRecordEvent(byteArray: ByteArray) {
        voiceStateListener?.onRecording(byteArray)
    }
}
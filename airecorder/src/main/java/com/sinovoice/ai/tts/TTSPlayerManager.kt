package com.sinovoice.ai.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import com.orhanobut.logger.Logger
import com.sinovoice.sdk.SessionState
import com.sinovoice.sdk.android.AudioPlayer
import com.sinovoice.sdk.android.HciAudioManager
import com.sinovoice.sdk.android.IAudioPlayerHandler
import com.sinovoice.sdk.tts.*
import com.sinovoice.ai.sdk.SDKManager
import com.sinovoice.ai.recognize.SDKConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TTSPlayerManager : IAudioPlayerHandler, ISynthHandler {
    companion object {
        private const val TAG = "TTSPlayerManager"
        private var instance: TTSPlayerManager? = null
            get() {
                if (field == null) {
                    field = TTSPlayerManager()
                }
                return field
            }

        @Synchronized
        fun get(): TTSPlayerManager {
            return instance!!
        }
    }

    private var shortText: SynthShortText? = null
    private var isCancel: Boolean = false
    private var currentId: Int = 0
    private var ttsText: String = ""
    private var playType: String = ""
    private var synthetizing: Boolean = false
    private var needPlay: Boolean = false
    private var playing: Boolean = false
    private var listener: TTSPlayerListener? = null
    private var countDownLatch: CountDownLatch = CountDownLatch(1)

    private lateinit var stream: SynthStream
    private lateinit var audioPlayer: AudioPlayer
    private var hciAudioManager: HciAudioManager? = null

    private var currentConfig: TTSConfig? = null

    private var localTts: Boolean = false
    private var localPlayType: String = TTSConstants.TTS_ROU_MEI_QIAN

    fun initTTS(context: Context, listener: TTSPlayerListener) {
        // AudioPlayer 采样率可以与 HciAudioManager 的采样率不同
        // HciAudioManager 默认使用 AudioAttributes.USAGE_VOICE_COMMUNICATION 和
        // AudioAttributes.CONTENT_TYPE_SPEECH。使用下面的参数将默认打开 speaker。
        this.listener = listener
        GlobalScope.launch(Dispatchers.Main) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            if (hciAudioManager == null) {
                hciAudioManager =
                    HciAudioManager.builder(context).setSampleRate(SDKConstants.DEFAULT_SAMPLE_RATE)
                        .setAudioAttributes(attributes)
                        .create()
            }
            audioPlayer = AudioPlayer(
                hciAudioManager, "pcm_s16le_16k", SDKConstants.DEFAULT_AUDIO_DATA_BUFFER_TIME
            )
        }
//        copyDefaultResource(context)
    }

    fun initTTS(context: Context) {
        // AudioPlayer 采样率可以与 HciAudioManager 的采样率不同
        // HciAudioManager 默认使用 AudioAttributes.USAGE_VOICE_COMMUNICATION 和
        // AudioAttributes.CONTENT_TYPE_SPEECH。使用下面的参数将默认打开 speaker。
        GlobalScope.launch(Dispatchers.Main) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            if (hciAudioManager == null) {
                hciAudioManager =
                    HciAudioManager.builder(context).setSampleRate(SDKConstants.DEFAULT_SAMPLE_RATE)
                        .setAudioAttributes(attributes)
                        .create()
            }
            audioPlayer = AudioPlayer(
                hciAudioManager, "pcm_s16le_16k", SDKConstants.DEFAULT_AUDIO_DATA_BUFFER_TIME
            )
        }
//        copyDefaultResource(context)
    }


    private fun localTtsConfig(): LocalTtsConfig {
        val localTtsConfig = LocalTtsConfig()
        localTtsConfig.setCNLib("/sdcard/tts/${localPlayType}/CNPackage.dat")
        localTtsConfig.setDMLib("/sdcard/tts/${localPlayType}/DMPackage.dat")
        localTtsConfig.setENLib("/sdcard/tts/${localPlayType}/ENPackage.dat")
        return localTtsConfig
    }

    private fun streamConfig(): SynthConfig {
        val config = SynthConfig()
        config.setFormat("pcm")
        config.setSampleRate(16000)
        config.setProperty(playType)
        currentConfig?.let {
            config.setPitch(it.pitch)
            config.setProperty(it.playType)
            config.setVolume(it.volume)
            config.setSpeed(it.speed)
        }
        return config
    }


    fun cloudTtsPlay(
        text: String,
        type: String,
        id: Int = 0,
        l: TTSPlayerListener? = null,
        ttsConfig: TTSConfig? = null
    ) {
        Logger.t("TTSPlayerManager cloudTtsPlay").d("text:$text" + "id:$id")
        GlobalScope.launch(Dispatchers.Main) {
            if (l != null) {
                listener = l
            }
            if (playing || synthetizing) {
                ttsStop(false)
            }
            localTts = false
            playType = type
            ttsText = text
            currentId = id
            currentConfig = ttsConfig
            ttsPlay()
        }
    }

    fun cloudTTSData(text: String, type: String): SynthResult? {
        if (synthetizing) {
            stream.cancel()
        }
        localTts = false
        playType = type
        ttsText = text
        shortText = SynthShortText(SDKManager.get().getSDK(), CloudTtsConfig())
        return shortText?.synth(text, streamConfig())
    }


    fun localTtsPlay(text: String, ttsConfig: TTSConfig, id: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            Logger.t("TTSPlayerManager localTtsPlay").d("text:$text" + "id:$id")
            if (playing || synthetizing) {
                ttsStop(true)
            }
            isCancel = false
            localTts = true
            currentConfig = ttsConfig
            localPlayType = ttsConfig.playType
            ttsText = text
            currentId = id
            ttsPlay()
        }
    }

    private fun ttsPlay() {
        // 启动播放成功后，再启动合成
        val sink = audioPlayer.audioSink()
        val m = sink.defaultMetrics().clone()
        val ret = sink.startWrite(m)
        if (ret != 0) {
            Logger.d("HciAudioSink.startWrite return $ret")
            return
        }
        Logger.d("启动播放器")
        playing = audioPlayer.start(AudioManager.STREAM_SYSTEM, this)
        if (!playing) {
            sink.endWrite(true)
        }
    }

    fun isPlaying(): Boolean {
        return playing
    }

    fun ttsStop(cancel: Boolean = false) {
        Logger.t(TAG).d("ttsStop start")
        countDownLatch = CountDownLatch(2)
        isCancel = cancel
        if (synthetizing) {
            // 取消合成
            if (stream.state() == SessionState.RUNNING) {
                stream.cancel()
            }
        } else {
            countDownLatch.countDown()
        }
        // 停止播放
        // 停止播放
        if (playing) {
            audioPlayer.stop()
        } else {
            countDownLatch.countDown()
        }
        try {
            countDownLatch.await(1500, TimeUnit.MILLISECONDS)
        } catch (ie: InterruptedException) {
            // continue?
        }
        Logger.t(TAG).d("ttsStop end")
    }

    fun ttsRelease() {
        listener = null
    }

    private fun streamStart() {
        synthetizing = true
        if (localTts) {
            stream = SynthStream(SDKManager.get().getSDK(), localTtsConfig())
        } else {
            stream = SynthStream(SDKManager.get().getSDK(), CloudTtsConfig())
        }
        stream.start(
            ttsText,
            streamConfig(),
            this,
            true
        )
    }

    override fun onStart(player: AudioPlayer?) {
        Logger.d("播放器已启动，启动合成会话")
        streamStart()
        listener?.onStartPlay()
    }

    override fun onStartFail(player: AudioPlayer?, message: String) {
        Logger.d("播放器启动失败: $message")
    }

    override fun onSinkEnded(player: AudioPlayer, cancel: Boolean) {
        Logger.d("播放器音频槽已停止写入: cancel = $cancel")
        player.stop()
    }

    override fun onStop(player: AudioPlayer?) {
        Logger.d("播放器启已停止")
        playing = false
        listener?.onPlayEnd(currentId, isCancel)
        if (synthetizing) {
            stream.cancel()
        }
        countDownLatch.countDown()
    }

    override fun onAudio(player: AudioPlayer?, audio: ByteBuffer?, timestamp: Long) {
        // 反馈播放数据，调用频率较高，一般不做处理
    }

    override fun onBufferEmpty(player: AudioPlayer) {
        Logger.d("播放缓冲区空")

    }

    override fun onError(player: AudioPlayer, message: String) {
        Logger.d("播放发生错误: $message")
        player.stop()
        synthetizing = true
    }

    override fun onStart(s: SynthStream?, code: Int, warnings: Array<Warning?>?) {
        if (code == 0) {
            Logger.d("流式合成会话启动成功")
            return
        }
        Logger.d("流式合成会话启动失败: code = $code")
        // 会让播放器丢弃缓冲数据并立即停止播放
        audioPlayer.audioSink().endWrite(true)
        synthetizing = false
    }

    override fun onEnd(s: SynthStream?, reason: Int) {
        synthetizing = false
        when (reason) {
            ISynthHandler.NORMAL -> {
                Logger.d("流式合成正常结束")
                // 会让播放器播放完所有数据后停止
                audioPlayer.audioSink().endWrite(false)
            }
            ISynthHandler.ABNORMAL -> {
                Logger.d("流式合成因发生错误结束")
                // 会让播放器丢弃缓冲数据并立即停止播放
                audioPlayer.audioSink().endWrite(true)
            }
            ISynthHandler.CANCEL -> {
                Logger.d("流式合成被取消")
                // 会让播放器丢弃缓冲数据并立即停止播放
                audioPlayer.audioSink().endWrite(true)
            }
        }
        s?.close()
        countDownLatch.countDown()
    }

    override fun onAudio(s: SynthStream?, audio: ByteBuffer?, done: Runnable) {
        // 异步写入到播放器中，完成写操作前**不能**调用 done.run() 释放 audio
        audioPlayer.audioSink().asyncWrite(audio) { written ->
            Logger.d("播放音频完成写入: $written")
            // 调用 done.run() 释放 audio
            done.run()
        }

    }

    override fun onError(s: SynthStream?, code: Int) {
        Logger.d("流式合成发生错误: code = $code")
        synthetizing = true
        s?.close()
    }

    override fun onSentence(p0: SynthStream?, p1: Sentence?) {

    }

    override fun onSyllable(p0: SynthStream?, p1: Array<out Syllable>?) {

    }

    override fun onMark(p0: SynthStream?, p1: Array<out Mark>?) {

    }

}


interface TTSPlayerListener {
    fun onStartPlay()
    fun onPlayEnd(id: Int = 0, isCancel: Boolean)
}

object TTSConstants {
    const val TTS_QIN_QIE_HONG = "cn_qinqiehong_common"
    const val TTS_ROU_MEI_QIAN = "cn_kefuman_common"
    const val TTS_CHENG_SHU_XING = "cn_chengshuxing_common"
    const val TTS_RE_QING_MAN = "cn_reqingman_common"
    const val TTS_DAO_HANG_FENG = "cn_daohangfeng_common"
    const val TTS_DAO_HANG_FAN = "cn_daohangfan_common"
    const val TTS_TIAN_MEI_RU = "cn_tianmeiru_common"
    const val TTS_DAI_MENG_XI = "cn_daimengxi_common"
    const val TTS_KE_FU_MAN = "cn_kefuman_common"
}
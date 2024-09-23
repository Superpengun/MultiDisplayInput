package com.sinovoice.ai.tts

import android.content.Context
import android.media.MediaPlayer
import com.orhanobut.logger.Logger
import java.lang.Runnable
import kotlin.random.Random

/**
 * 应答语播放器
 */
class MediaPlayManager {
    private lateinit var mMediaPlayer: MediaPlayer
    private val TAG: String = "MediaPlayController"
    private var resList = ArrayList<Int>()
    private var listener: OnCompletionListener? = null


    companion object {
        private var instance: MediaPlayManager? = null
        public final val TTS_TYPE_WAKEUP = 0
        public final val TTS_TYPE_NET_ERROR = 1
        public final val TTS_TYPE_SERVICE_ERROR = 2

        @Synchronized
        fun get(): MediaPlayManager {
            if (instance == null) {
                instance = MediaPlayManager()
            }
            return instance!!
        }
    }

    fun play(context: Context, type: Int, listener: OnCompletionListener, ttsConfig: TTSConfig) {
        when (type) {
            TTS_TYPE_WAKEUP -> {
                playWakeUpTip(context, listener, ttsConfig)
            }
            TTS_TYPE_NET_ERROR -> {
                playWifiDisConnectTip(context, listener, ttsConfig)
            }
            TTS_TYPE_SERVICE_ERROR -> {
                playServiceProblemTip(context, listener, ttsConfig)
            }
        }
    }

    fun playWakeUpTip(context: Context, listener: OnCompletionListener, ttsConfig: TTSConfig) {
        val random = Random.nextInt(2)
        val path = "/sdcard/tts/${ttsConfig.playType}/ding_$random.wav"
        initMediaPlayer(context, path)
    }

    fun playWifiDisConnectTip(
        context: Context,
        listener: OnCompletionListener,
        ttsConfig: TTSConfig
    ) {
        this.listener = listener
        val path = "/sdcard/tts/${ttsConfig.playType}/wifi_disconnect.wav"
        initMediaPlayer(context, path)
    }

    fun playServiceProblemTip(
        context: Context,
        listener: OnCompletionListener,
        ttsConfig: TTSConfig
    ) {
        this.listener = listener
        val path = "/sdcard/tts/${ttsConfig.playType}/wifi_disconnect.wav"
        initMediaPlayer(context, path)
    }

    private fun initMediaPlayer(context: Context, path: String) {
        Thread(
            object : Runnable {
                override fun run() {
                    mMediaPlayer = MediaPlayer()
                    mMediaPlayer.setDataSource(path)
                    mMediaPlayer.prepare()
                    mMediaPlayer.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
                        override fun onCompletion(mediaPlayer: MediaPlayer?) {
                            Logger.t(TAG).d("onCompletion")
                            listener?.onCompletion()
                        }
                    })
                    mMediaPlayer.setOnErrorListener(object : MediaPlayer.OnErrorListener {
                        override fun onError(
                            mediaPlayer: MediaPlayer?,
                            what: Int,
                            extra: Int
                        ): Boolean {
                            Logger.t(TAG).d("onError,what---$what,extra--$extra")
                            listener?.onError()
                            return false
                        }
                    })
                    mMediaPlayer.isLooping = false
                    if (!mMediaPlayer.isPlaying) {
                        mMediaPlayer.start()
                    }
                }
            }
        ).start()
    }

    interface OnCompletionListener {
        fun onCompletion()
        fun onError()
    }

}

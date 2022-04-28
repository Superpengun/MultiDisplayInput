package com.zqy.hci.service

import android.content.Context
import android.media.AudioManager
import android.os.Vibrator
import com.zqy.hci.bean.FunctionKeyCode

/**
 * @Author:CWQ
 * @DATE:2022/2/14
 * @DESC:
 */
class AudioAndHapticFeedbackManager private constructor() {
    private var mAudioManager: AudioManager? = null
    private var mVibrator: Vibrator? = null
    private var mSoundOn = true

    companion object {
        private val sInstance = AudioAndHapticFeedbackManager()

        fun getInstance() = sInstance

        fun init(context: Context) {
            sInstance.initInternal(context)
        }
    }

    private fun initInternal(context: Context) {
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }


    fun hasVibrator() = mVibrator != null && mVibrator!!.hasVibrator()


    fun performAudioFeedback(code: Int) {
        if (mAudioManager == null) {
            return
        }
        mSoundOn = Settings.instance.getCurrent()!!.isSoundOn()
        if (!mSoundOn) {
            return
        }
        val sound = when (code) {
            FunctionKeyCode.KEY_DEL -> AudioManager.FX_KEYPRESS_DELETE
            FunctionKeyCode.KEY_ENTER -> AudioManager.FX_KEYPRESS_RETURN
            FunctionKeyCode.KEY_SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
            else -> AudioManager.FX_KEY_CLICK
        }
        mAudioManager?.playSoundEffect(sound)
    }
}
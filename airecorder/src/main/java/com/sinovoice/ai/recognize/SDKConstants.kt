package com.sinovoice.ai.recognize

import java.io.File

object SDKConstants {
    const val CA_FILE_NAME = "tingyuaicloud.cer"
    const val DEFAULT_PROPERTY = "cn_16k_common"
    const val SPEAK_CHINESE = "cn_16k_huiyi"
    const val SPEAK_ENGLISH = "en_16k_common"
    const val SPEAK_UR = "ug_16k_common"
    const val DEFAULT_AUDIO_FORMAT = "jtx_speex"
    const val DEFAULT_TIME_OUT = 20000
    const val DEFAULT_SAMPLE_RATE = 16000
    const val DEFAULT_AUDIO_DATA_FRAME_TIME = 200
    const val DEFAULT_AUDIO_DATA_BUFFER_TIME = 1000
    const val DEFAULT_WAKE_AUDIO_DATA_BUFFER_TIME = 5000
    const val DEFAULT_AUDIO_DATA_CHANNEL = 1
}


object AsrRecorderConstants {
    const val DEFAULT_AUDIO_DATA_BUFFER_TIME = 1000
    const val DEFAULT_AUDIO_DATA_CHANNEL = 1
    const val SESSION_START_FAIL = 200
    const val NET_ERROR_CODE = 400

    /**
     * 异常音量检测
     */
    const val SILENCE_WARING_TIME_SECOND = 30
    const val SILENCE_LIMIT_TIME_SECOND = 60 * 5
    const val SOUND_LOW_TIME_SECOND = 3


}

object WakeupConstants {
    const val PREF_KEY_WAKE_MODEL_PATH = "key_wake_model_path"
    const val WAKE_MODEL_PATH = "/mnt/sdcard/wake/model"
    var WAKE_CONFIG_FILE_ABS_PATH = WAKE_MODEL_PATH + File.separator + "hci_voicewake.ini"
}
package com.sinovoice.ai

object Constant {
    /**
     * SDK在Android设备上的工作空间目录。主要用于存放授权、日志、模型数据，其他运行中可存储的数据
     */
    const val SDK_WORKSPACE = "/mnt/sdcard/wake"

    const val TTS_DATA_RESOURCE = "/mnt/sdcard/tts"


    const val RECOGNIZE_THREAD = "recognize_thread"
    const val LOCAL_RECOGNIZE_THREAD = "local_recognize_thread"
    const val RECORD_THREAD = "record_thread"
    const val WAKEUP_THREAD = "wakeup_thread"
}
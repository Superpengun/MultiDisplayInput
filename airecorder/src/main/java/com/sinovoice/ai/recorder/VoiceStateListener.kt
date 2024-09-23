package com.sinovoice.ai.recorder

/**
 *Created by liqiang on 2020/3/18.
 **/
interface AudioRecordListener {
    fun onRecordEvent(asrRecorderEvent: AsrRecorderEvent)
    fun onRecordError(msg: String)
    fun onRecording(voiceData: ByteArray)
}
package com.sinovoice.ai.recorder

/**
 *Created by liqiang on 2020/3/18.
 **/
enum class AsrRecorderEvent {
    /**
     * 开始录音。
     */
    EVENT_RECORDER_BEGIN_RECORD,

    /**
     * 停止录音。
     */
    EVENT_RECORDER_STOP_RECORD,

    /**
     * 录音缓冲池满
     */
    EVENT_RECORDER_DATA_FULL
}
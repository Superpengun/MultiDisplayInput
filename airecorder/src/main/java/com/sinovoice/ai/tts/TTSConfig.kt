package com.sinovoice.ai.tts

import com.sinovoice.ai.tts.TTSConstants.TTS_ROU_MEI_QIAN

data class TTSConfig(
    val playType: String = TTS_ROU_MEI_QIAN,
    val speed: Int = 0,//-500~500
    val pitch: Int = 0,//-500~500
    val volume: Int = 50//0-100
)

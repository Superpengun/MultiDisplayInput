package com.sinovoice.ai.recognize

/**
 * Created by liqiang on 2020/3/19.
 */
class RecognizeResult {
    //{"start_time":36300,"end_time":40160,"result":{"text":"嗯，","confidence":0.316},"is_final":true}
    var start_time: Long = 0
    var end_time: Long = 0
    var result: RecognizeText? = null
    var is_final = false

    fun setIs_final(is_final: Boolean) {
        this.is_final = is_final
    }

    override fun toString(): String {
        return "VoiceData{" +
                "start_time=" + start_time +
                ", end_time=" + end_time +
                ", result=" + result +
                ", is_final=" + is_final +
                '}'
    }

    inner class RecognizeText {
        var text: String? = null
        var confidence = 0f
        var words: List<Word>? = null

    }

    class Word {
        var start_time: Long = 0
        var end_time: Long = 0
        var word: String? = null
        var confidence = 0f
        var type: String? = null
    }
}
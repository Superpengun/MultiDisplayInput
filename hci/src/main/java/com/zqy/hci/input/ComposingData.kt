package com.zqy.hci.input

/**
 * @author:zhenqiyuan
 * @data:2022/2/9
 * @描述：
 * @package:com.sinovoice.input.international.input
 */
class ComposingData {
    var composingText = StringBuilder()
    var composingState: ComposingState? = null

    init {
        composingState = ComposingState.FINSHCOMPOSING
    }

    enum class ComposingState() {
        INCOMPOSING,
        FINSHCOMPOSING
    }

    fun onComposingStart() {
        composingState = ComposingState.INCOMPOSING
    }

    fun onComposingStop() {
        composingState = ComposingState.FINSHCOMPOSING
    }
}
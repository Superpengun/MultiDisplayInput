package com.zqy.hci.listener

/**
 * @author:zhenqiyuan
 * @data:2022/2/9
 * @描述：
 * @package:com.sinovoice.input.international.listener
 */
interface LogicControlListener {

    /**
     * composing上屏文本更新
     */
    fun onUpdateComposingText(composingText: String)

    /**
     * candidate上屏文本更新
     */
    fun onUpdateCandidateWordsList(upDateCandidateWordsList: List<String>)

}
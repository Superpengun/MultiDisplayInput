package com.zqy.hci.listener

/**
 * @author:zhenqiyuan
 * @data:2022/2/10
 * @描述：
 * @package:com.sinovoice.input.international.listener
 */
interface InputMethodListener {

    /**
     * 从InputMethod中发出ComposingText
     */
    fun onSendComposingText(composingText: String)

    /**
     * 从InputMethod中发出CandidateWordsList
     */
    fun onSendCandidateWordsList(updateCandidateWordsList: List<String>)
}
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

    /**
     * 关闭可能出现的符号、更多候选界面
     */
    fun onHideView()
}
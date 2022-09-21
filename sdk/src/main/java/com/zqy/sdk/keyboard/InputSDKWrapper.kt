package com.zqy.sdk.keyboard

/**
 * @author:zhenqiyuan
 * @data:2022/1/14
 * @描述：
 * @package:keyboard
 */
interface InputSDKWrapper {
    fun init(): Boolean
    fun release(): Boolean
    fun query(query: String?): RecogResult
    fun getMore(): RecogResult?
    fun setResPreFix(lan: String?)
    fun submitUDB(content: String, syllable: String)
}
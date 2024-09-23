package com.zqy.sdk.keyboard

/**
 * @author:zhenqiyuan
 * @data:2022/1/14
 * @描述：
 * @package:keyboard
 */
abstract class InputSDKWrapper {
    abstract fun init(): Boolean
    abstract fun release(): Boolean
    abstract fun query(query: String?): RecogResult
    abstract fun setResPreFix(lan: String?)
    abstract fun submitUDB(content: String, syllable: String)
}
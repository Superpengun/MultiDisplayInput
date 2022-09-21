package com.zqy.sdk.handwrite

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.sdk.handwrite
 */
interface AssociateSDKWrapper {
    fun init(): Boolean
    fun release(): Boolean
    fun associate(word: String?): ArrayList<String?>?
    fun raisePriority(word: String?)
}
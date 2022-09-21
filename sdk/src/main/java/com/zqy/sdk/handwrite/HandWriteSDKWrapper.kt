package com.zqy.sdk.handwrite

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.sdk.handwrite
 */
interface HandWriteSDKWrapper {
    fun init(): Boolean
    fun release(): Boolean
    fun recog(points: ShortArray?): ArrayList<String?>
}
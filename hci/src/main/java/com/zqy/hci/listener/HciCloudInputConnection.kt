package com.zqy.hci.listener


/**
 * @author:zhenqiyuan
 * @data:2022/1/18
 * @描述：
 * @package:input
 */
interface HciCloudInputConnection {
    /**
     * 向系统发送按键消息
     */
    fun keyDownUp(keyEventCode: Int)

    /**
     * 向系统提交字符串
     */
    fun commitString(str: String?, cursorPos: Int)

    /**
     * 向系统提交预选状态字符串
     */
    fun commitComposingStr(str: String?)

    /**
     * 向系统提交预选状态字符串
     */
    fun commitComposing(str: String?)

    /**
     * 关闭系统预选词
     */
    fun finishComposingText()
}
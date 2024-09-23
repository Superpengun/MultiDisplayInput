package com.zqy.hci.listener

/**
 * @author:zhenqiyuan
 * @data:2022/10/18
 * @描述：
 * @package:com.zqy.hci.listener
 */
interface OnSymbolActionListener {

    /**
     * 符号界面删除操作
     */
    fun onSymbolDelete()

    /**
     * 点击了符号界面的符号
     * @param str 选择上屏的符号
     */
    fun onSymbolSelected(str: String?)

    fun onSymbolBack()

    /**
     * 清除
     */
    fun onSymbolReset()

    /**
     * 确定
     */
    fun onSymbolAction()
}
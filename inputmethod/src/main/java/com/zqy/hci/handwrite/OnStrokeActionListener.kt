package com.zqy.hci.handwrite

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.hci.handwrite
 */
interface OnStrokeActionListener {
    /**
     * 手写结束时调用，传递手写数据
     * @param stroke 笔迹数组
     */
    fun onWriteEnd(stroke: ShortArray)

    /**
     * 点操作收起键盘
     */
    fun onPointTouch()
}
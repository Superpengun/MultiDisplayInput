package com.zqy.hci.listener

interface OnPlateActionListener {

    fun onPlateSelected(index: Int, word: String)

    /**
     * 返回
     */
    fun onPlateBack()

    /**
     * 删除
     */
    fun onPlateDelete()

    /**
     * 清除
     */
    fun onPlateReset()

    /**
     * 确定
     */
    fun onPlateAction()
}
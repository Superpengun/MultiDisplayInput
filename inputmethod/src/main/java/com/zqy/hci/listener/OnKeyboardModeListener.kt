package com.zqy.hci.listener

/**
 * @Author:CWQ
 * @DATE:2022/1/25
 * @DESC:
 */
interface OnKeyboardModeListener {

    /**
     * 输入模式切换时候调用
     * @param mode 输入模式 [InputModeConst.INPUT_DEFAULT]
     */
    fun onInputModeChange(mode: Int)

    /**
     * shift按键切换时调用
     * @param shiftMode shift状态
     */
    fun onShiftModeChange(shiftMode: Int)
}
package com.zqy.hci.bean

/**
 * @Author:CWQ
 * @DATE:2022/1/22
 * @DESC:
 */
object InputModeConst {
    var INPUT_DEFAULT = 4

    //语言状态
    var INPUT_ENGLISH = 0   //英文
    var INPUT_CHINESE = 1 //汉语
    var INPUT_HWR_CN = 2 //汉语手写
    var INPUT_HWR_EN = 3 //英语手写



    //当前界面状态
    var LAYOUT_KB = 0
    var LAYOUT_SYMBOL = 3
    var LAYOUT_T9 = 4

    //当前键盘模式
    val KB_DEFAULT = 1
    val KB_CUSTOM = 2
}
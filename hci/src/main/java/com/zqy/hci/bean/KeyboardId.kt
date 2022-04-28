package com.zqy.hci.bean

/**
 * @Author:CWQ
 * @DATE:2022/1/22
 * @DESC:
 */
class KeyboardId {

    var kbName: String
    var numKbName:String
    var symbolKbName:String
    var inputMode = 0
    var keyboardMode = InputModeConst.KB_DEFAULT

    constructor(kbName: String,numKbName:String,symbolKbName:String, inputMode: Int) {
        this.kbName = kbName
        this.numKbName = numKbName
        this.symbolKbName = symbolKbName
        this.inputMode = inputMode
    }

    override fun toString(): String {
        return "KeyboardId{" +
                "xmlId=" + kbName +
                "numXmlId=" + numKbName +
                "symbolXmlId=" + symbolKbName +
                ", inputMode=" + inputMode +
                ", keyboardMode=" + keyboardMode +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as KeyboardId
        return inputMode == that.inputMode && keyboardMode == that.keyboardMode && kbName == that.kbName && numKbName == that.numKbName && symbolKbName == that.symbolKbName
    }

    override fun hashCode(): Int {
        return kbName.hashCode() + keyboardMode
    }
}
package com.zqy.hci.input

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.hci.input
 */
internal interface PinyinComposingInterface {
    fun getComposingToCommiting(): String?

    fun appendLetter(c: Char)
    fun appendSelectedItem(chosenWord: String, syllable: String)
    fun appendSelectedSyllable(syllable: String)
    fun del()
    fun getCompoingToQuery(): String?

    fun clear()
    fun getCurrentOutput(): String?
    fun isMatchComplete(): Boolean

    fun updateFirstCandidate(word: String?, syllable: String?)
    fun getLastCharOfCompoingToQuery(): Char
    fun getSelectComposing(): String?
}
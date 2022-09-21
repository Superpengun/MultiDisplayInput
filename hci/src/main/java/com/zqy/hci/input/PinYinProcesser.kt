package com.zqy.hci.input

import android.util.Log

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.hci.input
 */
class PinYinProcesser : PinyinComposingInterface {
    private val composingForQuery = StringBuilder()
    private val composingForCommit = StringBuilder()
    private val selectedWords: ArrayList<WordPinyinPair> = ArrayList<WordPinyinPair>()
    private val firstCandidateWord: WordPinyinPair = WordPinyinPair()
    private val unusedQuery = StringBuilder()
    private val NUMBER_EN_TABLE = charArrayOf('a', 'd', 'g', 'j', 'm', 'p', 't', 'w')
    override fun getComposingToCommiting(): String {
        return composingForCommit.toString()
    }

    override fun appendLetter(c: Char) {
        unusedQuery.append(c)
        constructComposing()
    }

    override fun appendSelectedItem(chosenWord: String, syllable: String) {
        if(unusedQuery.isEmpty()) return
        val code = unusedQuery.substring(0, syllable.length)
        selectedWords.add(
            WordPinyinPair(
                chosenWord,
                syllable,
                code
            )
        )
        Log.d(PinYinInputMethod.TAG, "chosenWord = [$chosenWord] syllable = [$syllable]")
        unusedQuery.delete(0, syllable.length)
        constructComposing()
    }

    override fun appendSelectedSyllable(syllable: String) {
        if (syllable.length <= unusedQuery.length) {
            unusedQuery.delete(0, syllable.length)
            unusedQuery.insert(0, syllable)
            constructComposing()
        }
    }

    override fun del() {
        if (selectedWords.size > 0) {
            val removedItem: WordPinyinPair =
                selectedWords.removeAt(selectedWords.size - 1)
            val code: String = removedItem.code.toString()
            unusedQuery.insert(0, code)
        } else if (unusedQuery.isNotEmpty()) {
            unusedQuery.deleteCharAt(unusedQuery.length - 1)
        } else if (firstCandidateWord.pinyin.isNotEmpty()) {
            firstCandidateWord.pinyin.deleteCharAt(firstCandidateWord.pinyin.length - 1)
        }
        constructComposing()
    }

    override fun getCompoingToQuery(): String? {
        return composingForQuery.toString()
    }
    override fun clear() {
        composingForQuery.setLength(0)
        composingForCommit.setLength(0)
        firstCandidateWord.word.setLength(0)
        firstCandidateWord.pinyin.setLength(0)
        unusedQuery.setLength(0)
        selectedWords.clear()
    }

    override fun getCurrentOutput(): String? {
        return getComposingToCommiting()
    }

    override fun isMatchComplete(): Boolean {
        return composingForQuery.isEmpty()
    }

    override fun updateFirstCandidate(word: String?, syllable: String?) {
        firstCandidateWord.word.setLength(0)
        firstCandidateWord.pinyin.setLength(0)
        firstCandidateWord.word.append(word)
        firstCandidateWord.pinyin.append(syllable)
        constructComposing()
    }

    override fun getLastCharOfCompoingToQuery(): Char {
        val c = '\u0000'
        return if (composingForQuery.isNotEmpty()) {
            composingForQuery[composingForQuery.length - 1]
        } else c
    }

    override fun getSelectComposing(): String? {
        return ""
    }

    private fun constructComposing() {
        composingForQuery.setLength(0)
        composingForCommit.setLength(0)

        // 用户已选择的候选词
        val selectedWords = StringBuilder()
        for (pair in this.selectedWords) {
            selectedWords.append(pair.word)
        }
        composingForCommit.append(selectedWords.append(composingForQuery.toString()))

        //
        if (unusedQuery.length > 0 && containsDigit(unusedQuery.toString())) {
            if (firstCandidateWord.pinyin.isNotEmpty()) {
                composingForCommit.append(firstCandidateWord.pinyin)
                Log.d(
                    PinYinInputMethod.TAG,
                    "firstCandidateWord.pinyin = " + firstCandidateWord.pinyin.toString()
                )
                Log.d(PinYinInputMethod.TAG, "unusedQuery = $unusedQuery")
                if (unusedQuery.length > firstCandidateWord.pinyin.length) {
                    composingForCommit.append(
                        numberToPinyin(
                            unusedQuery.subSequence(
                                firstCandidateWord.pinyin.length,
                                unusedQuery.length
                            ).toString()
                        )
                    )
                }
            } else {
                composingForCommit.append(numberToPinyin(unusedQuery.toString()))
            }
        } else {
            composingForCommit.append(unusedQuery.toString())
        }
        composingForQuery.append(unusedQuery.toString())
    }

    private fun containsDigit(str: String): Boolean {
        for (i in 0 until str.length) {
            if (Character.isDigit(str[i])) {
                return true
            }
        }
        return false
    }

    private fun numberToPinyin(nums: String): String {
        val sb = StringBuilder()
        for (i in 0 until nums.length) {
            val n = nums[i]
            if (Character.isDigit(n)) {
                sb.append(NUMBER_EN_TABLE.get(n.toInt() - 50))
            } else {
                sb.append(n)
            }
        }
        return sb.toString()
    }
}

private class WordPinyinPair {
    var word = java.lang.StringBuilder()
    var pinyin = java.lang.StringBuilder()
    var code = java.lang.StringBuilder()

    constructor(word: String?, pinyin: String?, code: String?) {
        this.word = java.lang.StringBuilder(word)
        this.pinyin = java.lang.StringBuilder(pinyin)
        this.code = java.lang.StringBuilder(code)
    }

    constructor() {}
}
package com.zqy.sdk.keyboard

/**
 * @author:zhenqiyuan
 * @data:2022/1/14
 * @描述：
 * @package:keyboard
 */
class ResultMatchItem {
    private var resultItem = ""
    private var symbolsItem = ""

    fun getResultItem(): String {
        return resultItem
    }

    fun setResultItem(resultItem: String) {
        this.resultItem = resultItem
    }

    fun getSymbolsItem(): String? {
        return symbolsItem
    }

    fun setSymbolsItem(symbolsItem: String) {
        this.symbolsItem = symbolsItem
    }
}
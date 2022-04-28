package com.zqy.sdk.keyboard

import java.util.ArrayList

/**
 * @author:zhenqiyuan
 * @data:2022/1/14
 * @描述：
 * @package:keyboard
 */
class RecogResultItem {
    private var result: String? = null
    private var symbols: String? = null
    private var matchItems: ArrayList<ResultMatchItem> = ArrayList<ResultMatchItem>()

    fun getResult(): String? {
        return result
    }

    fun setResult(result: String?) {
        this.result = result
    }

    fun getSymbols(): String? {
        return symbols
    }

    fun setSymbols(symbols: String?) {
        this.symbols = symbols
    }

    fun getMatchItems(): ArrayList<ResultMatchItem> {
        return matchItems
    }

    fun setMatchItems(matchItems: ArrayList<ResultMatchItem>) {
        this.matchItems = matchItems
    }
}
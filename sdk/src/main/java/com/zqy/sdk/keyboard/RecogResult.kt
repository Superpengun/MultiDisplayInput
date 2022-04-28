package com.zqy.sdk.keyboard

import java.util.ArrayList

/**
 * @author:zhenqiyuan
 * @data:2022/1/14
 * @描述：
 * @package:keyboard
 */
public class RecogResult {
    private var recogResultItems: ArrayList<RecogResultItem> = ArrayList<RecogResultItem>()
    private var hasMore = false
    private var syllables = ArrayList<String>()


    fun getRecogResultItems(): ArrayList<RecogResultItem>? {
        return recogResultItems
    }

    fun setRecogResultItems(recogResultItems: ArrayList<RecogResultItem>) {
        this.recogResultItems = recogResultItems
    }

    fun isHasMore(): Boolean {
        return hasMore
    }

    fun setHasMore(hasMore: Boolean) {
        this.hasMore = hasMore
    }

    fun getSyllables(): ArrayList<String>? {
        return syllables
    }

    fun setSyllables(syllables: ArrayList<String>) {
        this.syllables = syllables
    }
}
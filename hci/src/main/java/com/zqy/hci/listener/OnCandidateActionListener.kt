package com.zqy.hci.listener

/**
 * @Author:CWQ
 * @DATE:2022/1/15
 * @DESC:候选区view回调
 */
interface OnCandidateActionListener {

    /**
     * 候选词或联想词被选中
     * @param index Int
     * @param word String
     */
    fun onCandidateSelected(index: Int, word: String)

    /**
     * 加载更多数据
     */
    fun getMoreList()

    /**
     * 点击查看更多候选词
     */
    fun onMore()

    /**
     * 点击关闭键盘
     */
    fun onClose()

    /**
     * 点击清楚联想词
     */
    fun onClear()

    /**
     * 更多候选区返回
     */
    fun onBack()
}
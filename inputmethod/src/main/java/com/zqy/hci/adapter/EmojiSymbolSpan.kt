package com.zqy.hci.adapter

import android.text.TextPaint
import androidx.recyclerview.widget.GridLayoutManager

/**
 * @Author:CWQ
 * @DATE:2023/1/5
 * @DESC:
 */
class EmojiSymbolSpan(
    private val symbolList: List<String>,
    private val screenWidth: Int,
    private val fontSize: Float
) :
    GridLayoutManager.SpanSizeLookup() {

    private val spanCount = 4
    private val spanSize: IntArray
    private val textPaint: TextPaint = TextPaint()

    init {
        textPaint.textSize = fontSize
        spanSize = IntArray(symbolList.size)
        calculateSpanSize()
    }

    private fun calculateSpanSize() {
        if (spanSize.isEmpty()) {
            return
        }

        val oneSpanWidth = screenWidth / spanCount - fontSize
        for (i in symbolList.indices) {
            val str = symbolList[i]
            if (str.length <= 2){
                spanSize[i] = 1
                continue
            }

            val strWidth = textPaint.measureText(str)
            if (strWidth >= oneSpanWidth){
                if (strWidth > oneSpanWidth * 2){
                    spanSize[i] = 4
                }else{
                    spanSize[i] = 2
                }
            }else{
                spanSize[i] = 1
            }
        }
    }

    override fun getSpanSize(position: Int): Int {
        return spanSize[position]
    }
}
package com.zqy.hci.adapter

import android.content.Context
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zqy.hci.utils.LayoutUtils
import com.zqy.hci.R

/**
 * @Author:CWQ
 * @DATE:2022/1/18
 * @DESC:更多候选词列表适配器
 */
class MoreCandidateAdapter(
    private val context: Context,
    private val itemHeight: Int = LinearLayout.LayoutParams.WRAP_CONTENT,
    private val minWidth: Int = LinearLayout.LayoutParams.WRAP_CONTENT
) :
    RecyclerView.Adapter<MoreCandidateViewHolder>() {
    private val mItemPadding = 50
    private val mPaint = TextPaint()
    private val mData = arrayListOf<String>()
    private var mTypeNum = 0
    private val mPositionTypeMap = hashMapOf<Int, Int>()
    private var mListener: ((Int, String) -> Unit)? = null

    fun setData(data: ArrayList<String>, fontSize: Float = 0f) {
        mTypeNum = 0
        mData.clear()
        mPositionTypeMap.clear()
        mData.addAll(data)
        mPaint.textSize = if (fontSize == 0f) {
            context.resources.getDimension(R.dimen.baseMoreCandidateTextSize)
        } else {
            fontSize
        }
        handleDataViewType()
        notifyDataSetChanged()
    }

    fun setOnItemListener(listener: (Int, String) -> Unit) {
        mListener = listener
    }

    override fun getItemViewType(position: Int): Int {
        return mPositionTypeMap[position] ?: 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoreCandidateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_adapter_more_candidate, parent, false)
        view.layoutParams.height = itemHeight
        view.layoutParams.width = minWidth * viewType
        return MoreCandidateViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoreCandidateViewHolder, position: Int) {
        holder.tvItem.textSize = LayoutUtils.px2dip(context, mPaint.textSize)
        holder.tvItem.text = mData[position]
        holder.tvItem.setOnClickListener {
            mListener?.invoke(position, mData[position])
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    /**
     * 计算每个数据对应的viewType，保存
     */
    private fun handleDataViewType() {
        for (position in 0 until mData.size) {
            val type = getTypeByPosition(position)
            val preType:Int = if (position == 0) {
                0
            } else {
                mPositionTypeMap[position-1]!!
            }
            val nextType = if (position < mData.size - 1) {
                getTypeByPosition(position + 1)
            } else {
                0
            }
            val resultType = when (type) {
                1 -> {
                    if (nextType > 3 && mTypeNum % 4 == 0) {
                        4
                    } else if(nextType > 2 && mTypeNum % 4 == 1){
                        3
                    } else if (nextType > 1 && mTypeNum % 4 == 2){
                        2
                    }else {
                        1
                    }
                }
                2 -> {
                    if (nextType > 2 && (preType > 2 || mTypeNum % 4 == 0)) {
                        4
                    } else if (nextType > 1 && mTypeNum % 4 == 1){
                        3
                    }else {
                        2
                    }
                }
                3 -> {
                    if (nextType > 1 && (preType > 1 || mTypeNum % 4 == 0)) {
                        4
                    } else {
                        3
                    }
                }
                else -> 4
            }
//            Log.e("adapter", "position:$position,type:$type,preType:$preType,nextType:$nextType,typeNum:$mTypeNum")
            mTypeNum += resultType
            mPositionTypeMap[position] = resultType
        }
    }

    /**
     * 根据当前position的字符串长度获取应使用的网格格数
     * @param position Int
     * @return Int
     */
    private fun getTypeByPosition(position: Int): Int {
        val strLength = getStringLength(mData[position])
        return when {
            strLength > minWidth * 3 - mItemPadding -> {
                4
            }
            strLength > minWidth * 2 - mItemPadding -> {
                3
            }
            strLength > minWidth - mItemPadding -> {
                2
            }
            else -> {
                1
            }
        }
    }

    private fun getStringLength(string: String) = mPaint.measureText(string)
}


class MoreCandidateViewHolder(item: View) : RecyclerView.ViewHolder(item) {
    val tvItem: TextView = item.findViewById(R.id.more_candidate_item_txt)
}
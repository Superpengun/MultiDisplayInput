package com.zqy.hci.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.zqy.hci.R
import com.zqy.hci.service.Settings
import com.zqy.hci.theme.ThemeState
import com.zqy.hci.widget.SymbolView

/**
 * @author:zhenqiyuan
 * @data:2022/10/18
 * @描述：
 * @package:com.zqy.hci.adapter
 */
class SymbolTitleAdapter(context: Context) :
    RecyclerView.Adapter<SymbolTitleAdapter.SymboleTitleViewHolder?>() {
    private val mContext: Context
    private val mInflater: LayoutInflater
    private var mSymbolView: SymbolView? = null
    private var mSelect = 0
    private var mTitles: ArrayList<String>? = null

    /**
     * 初始化操作
     * @param titles 符号列表标题
     * @param parentView 符号界面抽象类
     */
    fun init(titles: ArrayList<String>?, parentView: SymbolView?) {
        mTitles = titles
        Log.d("SymbolTitleAdapter", "mTitles:$mTitles")
        mSymbolView = parentView
        mSelect = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymboleTitleViewHolder {
        return SymboleTitleViewHolder(mInflater.inflate(R.layout.symbol_title_item, parent, false))
    }

    override fun onBindViewHolder(myViewHolder: SymboleTitleViewHolder, position: Int) {
        if (mTitles != null) {
            val str = mTitles!![position]
            myViewHolder.tv.text = str
            myViewHolder.tv.isSelected = position == mSelect
            myViewHolder.tv.isSoundEffectsEnabled = Settings.instance.getCurrent()!!.isSoundOn()
            if (!ThemeState.isDefault) {
                myViewHolder.tv.background =
                    AppCompatResources.getDrawable(mContext,R.drawable.white_btn_symbol_bottom)
                myViewHolder.tv.setTextColor(mContext.getColor(R.color.white_baseCandidateText))
            }
            myViewHolder.tv.setOnClickListener {
                mSymbolView?.setSymbolsLabelIndex(position)
                mSelect = position
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return mTitles!!.size
    }


    /**
     * 清除选择引索
     */
    fun clear() {
        mSelect = 0
        notifyDataSetChanged()
    }

    fun setSelect(index: Int) {
        mSelect = index
        notifyDataSetChanged()
    }

    class SymboleTitleViewHolder(v: View) :
        RecyclerView.ViewHolder(v) {
        var tv: TextView

        init {
            tv = v.findViewById<View>(R.id.item_btn_symbol_title) as TextView
        }
    }

    init {
        mContext = context
        mInflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}
package com.zqy.hci.adapter

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.zqy.hci.R
import com.zqy.hci.service.Settings
import com.zqy.hci.theme.ThemeState
import com.zqy.hci.utils.LayoutUtils
import com.zqy.hci.widget.SymbolView
import com.zqy.input.common.utils.SharedPreferenceUtils
import java.util.*

/**
 * @author:zhenqiyuan
 * @data:2022/10/18
 * @描述：
 * @package:com.zqy.hci.adapter
 */
class SymbolAdapter(context: Context, view: SymbolView) :
    RecyclerView.Adapter<SymbolAdapter.SymboleViewHolder?>() {
    private val mInflater: LayoutInflater
    private val mContext: Context
    private val mSymbolView: SymbolView
    private val mSharedPreferenceUtils: SharedPreferenceUtils
    private var mSymbolsPacks: LinkedList<SymbolsPack>? = null
    private var mCurSymbols: Array<String?> = emptyArray()
    private var mPageIndex = 0
    private var mDefFontSize = 0
    private var mDefaultHeight = 0
    private var mCurLabelIndex = 0
    private var mCommonSymbolPack: SymbolsPack? = null
    private val mSymbolListDisplayWidth: Int

    /**
     * 初始化符号界面数据
     */
    fun init() {
        mSymbolsPacks = LinkedList()
        val symbolArray = mContext.resources.getStringArray(R.array.symbols_list)
        for (symbol in symbolArray) {
            mSymbolsPacks!!.add(
                SymbolsPack(
                    mContext.getString(
                        mContext.resources
                            .getIdentifier(symbol + "_label", "string", mContext.packageName)
                    ),
                    mContext.resources.getStringArray(
                        mContext.resources
                            .getIdentifier(symbol, "array", mContext.packageName)
                    )
                )
            )
        }
        initCommonSymPack()
    }

    private fun initCommonSymPack() {
        mCommonSymbolPack = mSymbolsPacks!!.first
        val commonStringSize: Int = mSharedPreferenceUtils.getInt(COMMON_STRING_SIZE)
        if (commonStringSize != -1) {
            mCommonSymbolPack?.symbols = arrayOfNulls(commonStringSize)
            for (i in 0 until commonStringSize) {
                mCommonSymbolPack!!.symbols[i] =
                    mSharedPreferenceUtils.getString(COMMON_STRING + i, "")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymboleViewHolder {
        //设置显示内容高度为总高度的1/5
        return SymboleViewHolder(mInflater.inflate(R.layout.symbols_item, parent, false))
    }

    override fun onBindViewHolder(holder: SymboleViewHolder, position: Int) {
        val str = mCurSymbols[position]
        holder.tv.text = str
        holder.tv.setTextColor(mContext.getColor(R.color.key_text_color))
        //计算文字大小
        var textSize = mDefFontSize
        if (str!!.length > 5) {
            textSize = LayoutUtils.adjustTextSize(
                holder.tv.paint,
                mDefFontSize,
                mSymbolListDisplayWidth,
                str
            )
        }
        holder.tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        holder.tv.setBackgroundColor(mContext.getColor(R.color.symbolListViewBg))
        if (!ThemeState.isDefault) {
            holder.tv.background = AppCompatResources.getDrawable(mContext,R.drawable.white_morecandidate_key)
            holder.tv.setTextColor(mContext.getColor(R.color.white_baseCandidateText))
        }
        holder.tv.isSoundEffectsEnabled = Settings.instance.getCurrent()!!.isSoundOn()
        holder.tv.setOnClickListener { mSymbolView.onSymbolSelected(str) }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return mCurSymbols.size
    }

    /**
     * 获取符号分类标题
     * @return 标题的数组
     */
    fun getLeftMenuLabels(): ArrayList<String> {
        val labels = ArrayList<String>()
        for (sp in mSymbolsPacks!!) {
            labels.add(sp.label)
        }
        return labels
    }

    /**
     * 更新符号包
     * @param symbolsLabelIndex 符号标题引索
     */
    fun setSymbolsLabelIndex(symbolsLabelIndex: Int) {
        val sp = mSymbolsPacks!![symbolsLabelIndex]
        mCurLabelIndex = symbolsLabelIndex
        mCurSymbols = sp.symbols
        // TODO: 2017/7/4
        //mServer.setLeftMenuHighlightIdx(symbolsLabelIndex);
        mPageIndex = 0
        notifyDataSetChanged()
    }


    fun getSymbolsRes() {
        mDefFontSize = mContext.resources.getDimensionPixelSize(R.dimen.key_text_size)
        mDefaultHeight = mContext.resources.getDimensionPixelSize(R.dimen.keyboard_key_height)
    }

    /**
     * 调整最近使用符号的频率
     * @param str 提交的符号
     */
    fun adjustTheFrequency(str: String?) {
        val commonStrings = LinkedList(listOf(*mCommonSymbolPack!!.symbols))
        //删除最后一个字符，控制常用符号长度
        if (!commonStrings.remove(str)) {
            commonStrings.removeLast()
        }
        commonStrings.addFirst(str)
        val commonStringArray = arrayOfNulls<String>(commonStrings.size)
        commonStrings.toArray(commonStringArray)
        mCommonSymbolPack!!.symbols = commonStringArray
        if (mCurLabelIndex == 0) {
            mCurSymbols = mCommonSymbolPack!!.symbols
        }
        saveFrequencyInfo()
    }

    /**
     * 将常用符号保存到SharedPreference里面
     */
    fun saveFrequencyInfo() {
        Log.d(
            "SymbolAdapter", "saveFrequencyInfo mCommonSymbols:" + Arrays.toString(
                mCommonSymbolPack!!.symbols
            )
        )
        mSharedPreferenceUtils.put(COMMON_STRING_SIZE, mCommonSymbolPack!!.symbols.size)
        for (i in mCommonSymbolPack!!.symbols.indices) {
            mCommonSymbolPack!!.symbols[i]?.let {
                mSharedPreferenceUtils.put(
                    COMMON_STRING + i,
                    it
                )
            }
        }
    }

    private class SymbolsPack(
        /**
         * 左边栏显示的字符串
         */
        var label: String,
        /**
         * 符号列表
         */
        var symbols: Array<String?> = emptyArray()
    ) {
        init {
        }
    }

    class SymboleViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        var tv: TextView
        var rootView: LinearLayout

        init {
            tv = view.findViewById<View>(R.id.tv_symbol_item) as TextView
            rootView = view.findViewById<View>(R.id.symbols_item_root) as LinearLayout
        }
    }

    companion object {
        private const val TEXT_SIZE = 16
        private const val COMMON_STRING_SIZE = "common_string_size"
        private const val COMMON_STRING = "common_string_"
    }

    init {
        mContext = context
        mInflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mSymbolView = view
        mSharedPreferenceUtils = SharedPreferenceUtils.getInstance(context)
        mSymbolListDisplayWidth = 1384
    }
}
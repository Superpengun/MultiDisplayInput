package com.zqy.hci.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.zqy.hci.R

/**
 * @Author:CWQ
 * @DATE:2023/1/5
 * @DESC:
 */
class EmojiSymbolItemAdapter(private val symbolList: List<String>, private val context: Context) :
    Adapter<EmojiSymbolItemAdapter.EmojiSymbolHolder>() {

    private var mListener: ((String) -> Unit)? = null

    fun setOnItemListener(listener: ((String) -> Unit)) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiSymbolHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_emoji_symbol, parent, false)
        return EmojiSymbolHolder(view)
    }

    override fun onBindViewHolder(holder: EmojiSymbolHolder, position: Int) {
        holder.tvSymbol.text = symbolList[position]
        holder.tvSymbol.height = context.resources.getDimensionPixelSize(R.dimen.keyboard_key_height)
        holder.itemView.setOnClickListener {
            mListener?.invoke(symbolList[position])
        }
    }

    override fun getItemCount(): Int {
        return symbolList.size
    }


    class EmojiSymbolHolder(item: View) : ViewHolder(item) {
        val tvSymbol: TextView = item.findViewById(R.id.tv_emoji_symbol)
    }
}
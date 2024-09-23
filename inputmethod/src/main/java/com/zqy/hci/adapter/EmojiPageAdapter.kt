package com.zqy.hci.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

/**
 * @Author:CWQ
 * @DATE:2022/12/29
 * @DESC:
 */
class EmojiPageAdapter(private val pagers: List<View>) : PagerAdapter() {

    override fun getCount(): Int {
        return pagers.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(pagers[position])
        return pagers[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(pagers[position])
    }
}
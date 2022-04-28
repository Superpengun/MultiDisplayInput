package com.zqy.hci.theme

import android.content.Context
import android.content.SharedPreferences
import com.zqy.common.SPUtils
import com.zqy.hci.R

class ThemeManager(context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val PREF_CURRENT_THEME_KEY = "current_theme"
    private val DEFAULT_THEME = "baseTheme"

    var mUIThemeFactory: UIThemeFactory = UIThemeFactory(context)
    var mContext: Context = context
    lateinit var mCurrentTheme: UITheme
    private val mThemeViews by lazy {
        ArrayList<ThemeAbleView>()
    }

    fun loadTheme() {
        loadThemeInternal()
        SPUtils.getInstance(mContext).registerChangeListener(this)
    }

    fun addThemeAbleView(view: ThemeAbleView) {
        mThemeViews.add(view)
    }

    fun clear() {
        mThemeViews.clear()
    }

    private fun onThemeChange(uiTheme: UITheme) {
        mThemeViews.forEach {
            it.setUITheme(uiTheme)
        }
    }


    private fun loadThemeInternal() {
        val currentThemeName =
            SPUtils.getInstance(mContext).getString(PREF_CURRENT_THEME_KEY, DEFAULT_THEME)
        mCurrentTheme = getTheme(currentThemeName)
        onThemeChange(mCurrentTheme)
    }


    private fun getTheme(currentThemeName: String): UITheme {
        return mUIThemeFactory.createThemes(mContext, R.xml.theme).find {
            it.themeName.contentEquals(currentThemeName)
        }!!
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // TODO: 2022/1/17 flow
        if (key.contentEquals(PREF_CURRENT_THEME_KEY)) {
            loadThemeInternal()
        }
    }

}
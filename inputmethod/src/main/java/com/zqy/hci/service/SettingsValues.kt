package com.zqy.hci.service

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources

/**
 * @Author:CWQ
 * @DATE:2022/1/21
 * @DESC:
 */
class SettingsValues(context: Context?, prefs: SharedPreferences, res: Resources?) {
    var mSoundOn: Boolean
    var mFussyOn: Boolean
    var mRectify = false
    private val mAutoCaps: Boolean

    // From preferences, in the same order as xml/prefs.xml:
    private val mPopOn: Boolean
    private val mAutoSpace: Boolean
    fun isFuzzyOn(): Boolean {
        return mFussyOn
    }

    fun isSoundOn(): Boolean {
        return mSoundOn
    }

    fun isAutoCaps(): Boolean {
        return mAutoCaps
    }

    fun isPopOn(): Boolean {
        return mPopOn
    }

    fun isAutoSpace(): Boolean {
        return mAutoSpace
    }

    override fun toString(): String {
        return "SettingsValues{" +
                "mSoundOn=" + mSoundOn +
                ", mFussyOn=" + mFussyOn +
                ", mRectify=" + mRectify +
                ", mAutoCaps=" + mAutoCaps +
                ", mPopOn=" + mPopOn +
                ", mAutoSpace=" + mAutoSpace +
                '}'
    }

    companion object {
        private val TAG = SettingsValues::class.java.simpleName
    }

    init {
        mSoundOn = prefs.getBoolean(Settings.PREF_SOUND_ON, true)
        mFussyOn = prefs.getBoolean(Settings.PREF_FUZZY_ON, false)
        mAutoCaps = prefs.getBoolean(Settings.PREF_AUTO_CAPS_ON, false)
        mPopOn = prefs.getBoolean(Settings.PREF_POPUP_ON, true)
        mAutoSpace = prefs.getBoolean(Settings.PREF_AUTO_SPACE_ON, false)
    }
}
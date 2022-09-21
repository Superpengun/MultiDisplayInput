package com.zqy.hci.service

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Resources
import android.preference.PreferenceManager
import android.util.Log
import java.util.concurrent.locks.ReentrantLock

/**
 * @Author:CWQ
 * @DATE:2022/1/21
 * @DESC:
 */
class Settings private constructor() : OnSharedPreferenceChangeListener {
    private var mContext: Context? = null
    private lateinit var mPrefs: SharedPreferences
    private var mSettingsValues: SettingsValues? = null
    private val mSettingsValuesLock = ReentrantLock()
    private var mRes: Resources? = null
    private fun onCreate(context: Context) {
        mContext = context
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    private fun onDestroy(){
        mContext = null
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String) {
        Log.d(TAG, "onSharedPreferenceChanged: $key")
        mSettingsValuesLock.lock()
        try {
            if (mSettingsValues == null) {
                return
            }
            loadSettings(mContext!!)
        } finally {
            mSettingsValuesLock.unlock()
        }
    }

    fun loadSettings(context: Context) {
        mSettingsValuesLock.lock()
        mContext = context
        mRes = context.resources
        mSettingsValues = SettingsValues(context, mPrefs, mRes)
        mSettingsValuesLock.unlock()
        Log.d(TAG, "loadSettings: currentsettings=" + mSettingsValues.toString())
    }

    // TODO: Remove this method and add proxy method to SettingsValues.
    fun getCurrent(): SettingsValues? {
        return mSettingsValues
    }

    companion object {
        private val TAG = Settings::class.java.simpleName

        // In the same order as xml/prefs.xml
        const val PREF_SOUND_ON = "sound_on"
        const val PREF_SPLIT_MODE = "split_mode"
        const val PREF_AUTO_CAPS_ON = "auto_caps"
        const val PREF_POPUP_ON = "popup_on"
        const val PREF_AUTO_SPACE_ON = "auto_space"
        const val PREF_FUZZY_ON = "fuzzy_on"
        const val PREF_VOICE_INPUT_ON = "voice_input_on"
        const val PREF_THEME_ON = "day_night_theme_on"
        const val SWIPE_SWITCH = "swipe_switch"
        const val PREF_DAYMODE_START_TIME = "setting_daymode_start_time"
        const val PREF_NIGHTMODE_START_TIME = "setting_nightmode_start_time"
        val instance = Settings()

        fun init(context: Context) {
            instance.onCreate(context)
        }

        fun destroy(){
            instance.onDestroy()
        }
    }
}
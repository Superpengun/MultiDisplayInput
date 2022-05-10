package com.zqy.hci.input

import android.content.Context
import android.util.Log
import com.zqy.hci.bean.InputModeConst
import com.zqy.hci.listener.HciCloudInputConnection
import com.zqy.hci.listener.InputMethodListener
import com.zqy.hci.listener.LogicControlListener
import com.zqy.hci.utils.LanguageUtil
import com.zqy.sdk.InputEngineInstance
import com.zqy.sdk.SysSDKManager
import com.zqy.sdk.tools.HciCloudUtils
import kotlinx.coroutines.*

/**
 * @author:zhenqiyuan
 * @data:2022/1/21
 * @描述：
 * @package:com.sinovoice.engine.controller
 */
class LogicControl private constructor()  {
    private lateinit var mContext: Context
    var mInitHciCloudSys = false

    companion object {
        private val TAG = LogicControl::class.java.canonicalName
        val instance by lazy { LogicControl() }
    }
    ///////////////////////////////////////////////系统相关////////////////////////////////////////////////////////
    fun init(
        context: Context
    ) {
        mContext = context
        mInitHciCloudSys = SysSDKManager.get().initHciCloudSys(context)
        if (!mInitHciCloudSys) return
        InputEngineInstance.get().init()
    }

    fun release() {
        InputEngineInstance.get().release()
        SysSDKManager.get().releaseHciCloud()
        mInitHciCloudSys = false
    }

    fun getVersionName(): String? {
        return HciCloudUtils.getLocalVersionName(mContext)
    }
}
package com.zqy.hci.input

import android.content.Context
import com.zqy.sdk.manager.HWInputEngineInstance
import com.zqy.sdk.manager.KBInputEngineInstance
import com.zqy.sdk.SysSDKManager
import com.zqy.sdk.tools.HciCloudUtils

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
        KBInputEngineInstance.get().init()
        HWInputEngineInstance.get().init()
    }

    fun release() {
        KBInputEngineInstance.get().release()
        HWInputEngineInstance.get().release()
        SysSDKManager.get().releaseHciCloud()
        mInitHciCloudSys = false
    }

    fun getVersionName(): String? {
        return HciCloudUtils.getLocalVersionName(mContext)
    }
}
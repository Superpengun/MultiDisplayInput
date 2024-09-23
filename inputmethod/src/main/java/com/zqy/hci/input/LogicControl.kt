package com.zqy.hci.input

import android.content.Context
import com.zqy.sdk.SysSDKManager
import com.zqy.sdk.manager.HWInputEngineInstance
import com.zqy.sdk.manager.KBInputEngineInstance
import com.zqy.sdk.network.Repository
import com.zqy.sdk.network.Repository.KEY_LOCAL_DICS_VERSION
import com.zqy.sdk.tools.HciCloudUtils
import com.zqy.input.common.utils.SharedPreferenceUtils
import com.superpenguin.foreigninputandoutput.inputEngine.KeyboardEngine

/**
 * @author:zhenqiyuan
 * @data:2022/1/21
 * @描述：
 * @package:com.sinovoice.engine.controller
 */
class LogicControl private constructor()  {
    private lateinit var mContext: Context
    var mInitHciCloudSys = false
    var currentLanguage = "en"

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
        KeyboardEngine.init(context.applicationContext)
    }

    fun release() {
        KBInputEngineInstance.get().release()
        HWInputEngineInstance.get().release()
        SysSDKManager.get().releaseHciCloud()
        KeyboardEngine.release()
        mInitHciCloudSys = false
    }

    fun getVersionName(): String? {
        return HciCloudUtils.getLocalVersionName(mContext)
    }

    fun updateCNDict(){
        val version = SharedPreferenceUtils.getInstance(mContext).getString(
            KEY_LOCAL_DICS_VERSION,
            "1.0.0"
        )
        Repository.queryCode(mContext,version)
    }
}
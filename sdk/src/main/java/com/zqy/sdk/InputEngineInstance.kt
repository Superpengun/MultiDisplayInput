package com.zqy.sdk

/**
 * @author:zhenqiyuan
 * @data:2022/1/14
 * @描述：KB引擎单例
 * @package:
 */

import com.sinovoice.hcicloudsdk.api.kb.HciCloudKb
import com.sinovoice.hcicloudsdk.common.kb.KbInitParam
import com.sinovoice.jtandroiddevutil.log.JTLog
import com.zqy.sdk.keyboard.InputSDKWrapper
import com.zqy.sdk.keyboard.MultiSDKWrapper
import com.zqy.sdk.keyboard.RecogResult
import com.zqy.sdk.tools.HciCloudUtils


class InputEngineInstance {
    private val TAG = InputEngineInstance::class.java.simpleName

    companion object {
        private var instance: InputEngineInstance? = null
            get() {
                if (field == null) {
                    field = InputEngineInstance()
                }
                return field
            }

        @Synchronized
        fun get(): InputEngineInstance {
            return instance!!
        }
    }

    private val mMainSDKWrapper: InputSDKWrapper = MultiSDKWrapper()
    private var mInitSession = false

    /**
     * 引擎能力初始化
     */
    fun init() {
        initKB()
    }

    /**
     * 切换语种
     */
    fun changeLanguage(lan: String?) {
        mMainSDKWrapper.setResPreFix(lan)
        if (mInitSession) mMainSDKWrapper.release()
        mInitSession = mMainSDKWrapper.init()
    }

    /**
     * 查询
     */
    fun multiQuery(query: String?): RecogResult {
        return mMainSDKWrapper.query(query)
    }

    /**
     * 获取更多结果
     */
    fun multiGetMore(): RecogResult? {
        return mMainSDKWrapper.getMore()
    }

    /**
     * 引擎能力反初始化
     */
    fun release() {
        mMainSDKWrapper.release()
        releaseKb()
    }

    /**
     * 初始化KB能力
     *
     * @return true : 初始化KB成功  <br></br>  false : 初始化KB失败
     */
    private fun initKB() {
        val initParam = KbInitParam()
//        val dataPath: String = HciCloudUtils.dataPath
        val dataPath: String = "/data/data/com.zqy.multidisplayinput/lib/x86/"
        //String dataPath ="/system/lib";
        initParam.addParam(KbInitParam.PARAM_KEY_DATA_PATH, dataPath)
        initParam.addParam(
            KbInitParam.PARAM_KEY_FILE_FLAG,
            Constants.KBConstant.FILE_FLAG
        )
        initParam.addParam(
            KbInitParam.PARAM_KEY_INIT_CAP_KEYS,
            Constants.KBConstant.KB_CAPKEY
        )
        val initResult: Int = HciCloudKb.hciKbInit(initParam.getStringConfig())
        JTLog.i(TAG, "Kb init result: $initResult")
    }


    /**
     * 释放KB
     *
     * @return true : 释放成功  <br></br>  false : 释放失败
     */
    private fun releaseKb() {
        val errCode: Int = HciCloudKb.hciKbRelease()
        JTLog.i(TAG, "hciKbrelease return: $errCode")
    }
}
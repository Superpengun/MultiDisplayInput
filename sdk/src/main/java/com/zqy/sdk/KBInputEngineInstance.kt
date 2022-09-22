package com.zqy.sdk

import android.util.Log
import com.sinovoice.hcicloudsdk.api.kb.HciCloudKb
import com.sinovoice.hcicloudsdk.common.kb.KbInitParam
import com.zqy.sdk.keyboard.*
import com.zqy.sdk.tools.HciCloudUtils

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.sdk
 */
class KBInputEngineInstance {
    private val TAG = KBInputEngineInstance::class.java.simpleName

    companion object {
        private var instanceKB: KBInputEngineInstance? = null
            get() {
                if (field == null) {
                    field = KBInputEngineInstance()
                }
                return field
            }

        @Synchronized
        fun get(): KBInputEngineInstance {
            return instanceKB!!
        }
    }

    private var mMainSDKWrapper: InputSDKWrapper = MultiSDKWrapper()
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
        if (lan != null) {
            if (lan == "_en_") {
                mMainSDKWrapper = EnglishSDKWrapper()
            } else if (lan == "_cn_") {
                mMainSDKWrapper = ChineseSDKWrapper()
            } else{
                mMainSDKWrapper = MultiSDKWrapper()
                mMainSDKWrapper.setResPreFix(lan)
            }
            if (mInitSession) mMainSDKWrapper.release()
            mInitSession = mMainSDKWrapper.init()
        }
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
     * 获取更多结果
     */
    fun submitUDB(content: String, syllable: String) {
        return mMainSDKWrapper.submitUDB(content, syllable)
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
        val dataPath: String = HciCloudUtils.dataPath
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
        Log.i(TAG, "Kb init result: $initResult")
    }


    /**
     * 释放KB
     *
     * @return true : 释放成功  <br></br>  false : 释放失败
     */
    private fun releaseKb() {
        val errCode: Int = HciCloudKb.hciKbRelease()
        Log.i(TAG, "hciKbrelease return: $errCode")
    }
}
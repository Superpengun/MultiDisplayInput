package com.zqy.sdk.manager

import android.util.Log
import com.sinovoice.hcicloudsdk.api.hwr.HciCloudHwr
import com.sinovoice.hcicloudsdk.common.hwr.HwrInitParam
import com.zqy.sdk.Constants
import com.zqy.sdk.tools.HciCloudUtils

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.sdk
 */
class HWInputEngineInstance {
    private val TAG = HWInputEngineInstance::class.java.simpleName

    companion object {
        private var instanceHW: HWInputEngineInstance? = null
            get() {
                if (field == null) {
                    field = HWInputEngineInstance()
                }
                return field
            }

        @Synchronized
        fun get(): HWInputEngineInstance {
            return instanceHW!!
        }
    }

    /**
     * 引擎能力初始化
     */
    fun init() {
        initHWR()
    }

    /**
     * 引擎能力反初始化
     */
    fun release() {
        releaseHWR()
    }

    /**
     * 初始化HWR能力
     *
     * @return true : 初始化HWR成功<br></br> false : 初始化HWR失败
     */
    private fun initHWR() {
        val dataPath: String = HciCloudUtils.dataPath
        val hwrParam = HwrInitParam()
        hwrParam.addParam(HwrInitParam.PARAM_KEY_DATA_PATH, dataPath)
        hwrParam.addParam(
            HwrInitParam.PARAM_KEY_INIT_CAP_KEYS,
            Constants.HWRConstant.HWR_CN_CAPKEY + ";" + Constants.HWRConstant.HWR_EN_CAPKEY
        )
        hwrParam.addParam(HwrInitParam.PARAM_KEY_FILE_FLAG, Constants.HWRConstant.FILE_FLAG)
        val config = hwrParam.stringConfig
        val result = HciCloudHwr.hciHwrInit(config)
        Log.i(TAG, "HWR init result: $result")
    }

    /**
     * 释放HWR
     *
     * @return true : 释放成功  <br></br>  false : 释放失败
     */
    private fun releaseHWR() {
        val errCode = HciCloudHwr.hciHwrRelease()
        Log.i(TAG, "hciKbrelease return: $errCode")
    }
}
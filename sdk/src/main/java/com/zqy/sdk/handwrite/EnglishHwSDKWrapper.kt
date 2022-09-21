package com.zqy.sdk.handwrite

import android.util.Log
import com.sinovoice.hcicloudsdk.api.hwr.HciCloudHwr
import com.sinovoice.hcicloudsdk.common.HciErrorCode
import com.sinovoice.hcicloudsdk.common.Session
import com.sinovoice.hcicloudsdk.common.hwr.HwrConfig
import com.sinovoice.hcicloudsdk.common.hwr.HwrRecogResult
import com.zqy.sdk.Constants

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.sdk.handwrite
 */
class EnglishHwSDKWrapper : HandWriteSDKWrapper{
    private val TAG: String = EnglishHwSDKWrapper::class.java.getSimpleName()

    /**
     * 手写识别Session
     */
    protected var mHwrRecogSession: Session? = null
    protected lateinit var mHwrConfig: HwrConfig
    override fun init(): Boolean {
        mHwrRecogSession = Session()
        mHwrConfig = HwrConfig()
        mHwrConfig.addParam(
            HwrConfig.SessionConfig.PARAM_KEY_CAP_KEY,
            Constants.HWRConstant.HWR_CN_CAPKEY
        )

        //启动实时识别
        mHwrConfig.addParam(
            HwrConfig.SessionConfig.PARAM_KEY_REALTIME,
            Constants.HWRConstant.IS_REALTIME
        )

        // 设置字典资源前缀
        mHwrConfig.addParam(
            HwrConfig.SessionConfig.PARAM_KEY_RES_PREFIX,
            Constants.HWRConstant.RES_PREFIX_EN
        )

        //设置手写模式：行写叠写
        mHwrConfig.addParam(
            HwrConfig.InputConfig.PARAM_KEY_SPLIT_MODE,
            Constants.HWRConstant.HWR_SPLIT_MODE_LINE
        )

        //设置单词模式
        mHwrConfig.addParam(
            HwrConfig.InputConfig.PARAM_KEY_WORD_MODE,
            Constants.HWRConstant.WORD_MODE
        )
        //设置候选词个数
        mHwrConfig.addParam(
            HwrConfig.ResultConfig.PARAM_KEY_CAND_NUM,
            Constants.HWRConstant.CAND_NUM
        )

        // 设置繁简转换
        mHwrConfig.addParam(
            HwrConfig.ResultConfig.PARAM_KEY_DISP_CODE,
            Constants.HWRConstant.DISP_CODE
        )

        //手写识别范围
        mHwrConfig.addParam(HwrConfig.ResultConfig.PARAM_KEY_RECOG_RANGE, "all")

        val errCode = HciCloudHwr.hciHwrSessionStart(mHwrConfig.stringConfig, mHwrRecogSession)
        Log.d(TAG, "init: $errCode")
        return errCode == HciErrorCode.HCI_ERR_NONE
    }

    override fun release(): Boolean {
        if (mHwrRecogSession != null) {
            val errCode = HciCloudHwr.hciHwrSessionStop(mHwrRecogSession)
            mHwrRecogSession = null
            Log.d(TAG, "hciHWRSessionStop return: $errCode")
            if (errCode == HciErrorCode.HCI_ERR_NONE) return true
        }
        return false
    }


    override fun recog(points: ShortArray?): ArrayList<String?> {
        val sb = StringBuilder()
        sb.append(mHwrConfig.stringConfig)
        mHwrConfig.parseStringConfig(sb.toString())
        val outRecogResult = HwrRecogResult()
        val recogResult = HciCloudHwr.hciHwrRecog(
            mHwrRecogSession, points,
            mHwrConfig.stringConfig, outRecogResult
        )
        Log.i(TAG, "hwrRecog() recogResult: $recogResult")
        val list = outRecogResult.resultItemList
        val results = ArrayList<String?>()
        if (outRecogResult.resultItemList != null && outRecogResult.resultItemList.size > 0) {
            for (item in list) {
                results.add(item.result)
            }
        }
        return results
    }
}
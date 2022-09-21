package com.zqy.sdk.handwrite

import android.util.Log
import com.sinovoice.hcicloudsdk.api.hwr.HciCloudHwr
import com.sinovoice.hcicloudsdk.common.HciErrorCode
import com.sinovoice.hcicloudsdk.common.Session
import com.sinovoice.hcicloudsdk.common.hwr.HwrAssociateWordsResult
import com.sinovoice.hcicloudsdk.common.hwr.HwrConfig
import com.zqy.sdk.Constants

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.sdk.handwrite
 */
class EnglishAssociateSDKWrapper :AssociateSDKWrapper{
    private val TAG: String = EnglishAssociateSDKWrapper::class.java.getSimpleName()
    private var mAssociateSession: Session? = null
    override fun init(): Boolean {
        mAssociateSession = Session()
        val mAssociateConfig = HwrConfig()
        mAssociateConfig.addParam(
            HwrConfig.SessionConfig.PARAM_KEY_CAP_KEY,
            Constants.HWRConstant.HWR_ASSOCIATE_CAPKEY
        )
        mAssociateConfig.addParam(
            HwrConfig.AssociateConfig.PARAM_KEY_ASSOCIATE_MODEL,
            Constants.HWRConstant.ASSOCIATE_MODEL
        )
        mAssociateConfig.addParam(
            HwrConfig.SessionConfig.PARAM_KEY_RES_PREFIX,
            Constants.HWRConstant.RES_PREFIX_EN
        )
        val errCode = HciCloudHwr.hciHwrSessionStart(
            mAssociateConfig.stringConfig,
            mAssociateSession
        )
        Log.i(TAG, "mAssociateSession start return $errCode")
        return errCode == HciErrorCode.HCI_ERR_NONE
    }

    override fun release(): Boolean {
        if (mAssociateSession != null) {
            val errCode = HciCloudHwr.hciHwrSessionStop(mAssociateSession)
            mAssociateSession = null
            Log.i(TAG, "stopAssociateSession return: $errCode")
            return errCode == HciErrorCode.HCI_ERR_NONE
        }
        return false
    }

    override fun associate(word: String?): ArrayList<String?>? {
        val hwrAssWordsResult = HwrAssociateWordsResult()
        val result =
            HciCloudHwr.hciHwrAssociateWords(mAssociateSession, null, word, hwrAssWordsResult)
        return hwrAssWordsResult.resultList
    }

    override fun raisePriority(word: String?) {
        Log.d(TAG, "raisePriority: 英文不支持")
    }
}
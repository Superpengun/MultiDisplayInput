package com.zqy.sdk.keyboard

import android.text.TextUtils
import android.util.Log
import com.sinovoice.hcicloudsdk.api.kb.HciCloudKb
import com.sinovoice.hcicloudsdk.common.HciErrorCode
import com.sinovoice.hcicloudsdk.common.Session
import com.sinovoice.hcicloudsdk.common.kb.*
import com.sinovoice.jtandroiddevutil.log.JTLog
import com.zqy.sdk.Constants
import com.zqy.sdk.tools.HciCloudUtils

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.sdk.keyboard
 */
class ChineseSDKWrapper : InputSDKWrapper {
    private val TAG = ChineseSDKWrapper::class.java.simpleName
    private lateinit var mKbConfig: KbConfig
    private var mKbSession: Session?=null
    private lateinit var mCurrentKbResult: KbRecogResult
    private var resPreFix = ""

    override fun init(): Boolean {
        mKbSession = Session()
        mKbConfig = KbConfig()
        mCurrentKbResult = KbRecogResult()
        // cap_key和资源前缀是开启一个session的必须配置
        mKbConfig.addParam(
            KbConfig.SessionConfig.PARAM_KEY_CAP_KEY,
            Constants.KBConstant.KB_CAPKEY
        )
        mKbConfig.addParam(Constants.KBConstant.RES_PROFIX, Constants.KBConstant.RES_PREFIX_CN)

        // 设置输出结果页大小
        mKbConfig.addParam(
            KbConfig.ResultConfig.PARAM_KEY_PAGE_COUNT, ""
                    + Constants.KBConstant.QUERY_COUNT
        )
        mKbConfig.addParam(
            KbConfig.InputConfig.PARAM_KEY_INPUT_MODE,
            Constants.KBConstant.INPUT_MODE_PINYIN
        )
        mKbConfig.addParam(KbConfig.InputConfig.PARAM_KEY_KEYBOARD, "t9")
        mKbConfig.addParam(
            KbConfig.InputConfig.PARAM_KEY_FAULT_TOLERANT_LEVEL,
            Constants.KBConstant.INPUT_TOLERANT_LEVEL_HIGH
        )
        val errCode = HciCloudKb.hciKbSessionStart(mKbConfig.stringConfig, mKbSession)
        JTLog.d(TAG, "multiSDKWrapper start session using [" + mKbConfig.stringConfig + "] return " + errCode
        )
        Log.d(TAG, "init: $errCode")
        return errCode == HciErrorCode.HCI_ERR_NONE
    }

    override fun setResPreFix(lan: String?) {
        if (lan != null) {
            resPreFix = lan
        }
    }

    override fun submitUDB(content: String, syllable: String) {
        val recogItem = KbRecogResultItem()
        recogItem.result = content
        if (!TextUtils.isEmpty(content) && content.length == 1) {
            recogItem.symbols = syllable
        } else {
            recogItem.symbols = ""
        }

        val udbItem = KbUdbItemInfo()
        udbItem.recogResultItem = recogItem

        val errorcode = HciCloudKb.hciKbUdbCommit(mKbSession, "inputMode=pinyin", udbItem)
    }

    override fun release(): Boolean {
        if (mKbSession != null) {
            val errCode = HciCloudKb.hciKbSessionStop(mKbSession)
            JTLog.i(TAG, "hciKbSessionStop return: $errCode")
            mKbSession = null
            if (errCode == HciErrorCode.HCI_ERR_NONE) return true
        }
        JTLog.e(TAG, "mKbSession is null hciKbSessionStop return -1")
        return false
    }

    override fun query(query: String?): RecogResult {
        // 每次查询均为重新查询，保持引擎的无状态性.
        resetKbRecogResult(mCurrentKbResult)

        // 构造查询信息
        val cnKbQueryInfo = KbQueryInfo()
        cnKbQueryInfo.query = query

        // 查询
        val errCode = HciCloudKb.hciKbRecog(
            mKbSession,
            mKbConfig.stringConfig,
            cnKbQueryInfo,
            mCurrentKbResult
        )

        return HciCloudUtils.transForm(mCurrentKbResult)
    }

    override fun getMore(): RecogResult {
        mCurrentKbResult = kbGetNextReg(mKbSession, mKbConfig, mCurrentKbResult)
        return HciCloudUtils.transForm(mCurrentKbResult)
    }

    /**
     * SDK KB识别 ：如果还有候选字，则往后获取一页
     */
    private fun kbGetNextReg(session: Session?, mConfig: KbConfig?, recogResult: KbRecogResult?): KbRecogResult {
        var errCode = HciErrorCode.HCI_ERR_NONE
        val kbRecogResultItems = ArrayList<KbRecogResultItem>()
        val kbSyllableResultItems = ArrayList<KbSyllableResultItem>()
        return if (recogResult != null) {
            // 保存原来的识别结果
            if (recogResult.recogResultItemList != null) {
                kbRecogResultItems.addAll(recogResult.recogResultItemList)
            }
            if (recogResult.syllableResultItemList != null) {
                kbSyllableResultItems.addAll(recogResult.syllableResultItemList)
            }
            val result = KbRecogResult()
            val isMore = recogResult.bmore
            if (isMore) {
                // recogInfo必须设置为null才能向后获取一页
                errCode = HciCloudKb.hciKbRecog(
                    session,
                    mConfig!!.stringConfig, null, recogResult
                )
                if (errCode == 0) {
                    if (recogResult.recogResultItemList != null) {
                        kbRecogResultItems.addAll(recogResult.recogResultItemList)
                    }
                    if (recogResult.syllableResultItemList != null) {
                        kbSyllableResultItems.addAll(recogResult.syllableResultItemList)
                    }
                    result.bmore = recogResult.bmore
                } else {
                    result.bmore = false
                }
            }
            result.recogResultItemList = kbRecogResultItems
            result.syllableResultItemList = kbSyllableResultItems
            return result
        } else {
            KbRecogResult()
        }
    }

    private fun resetKbRecogResult(recogResult: KbRecogResult?) {
        recogResult!!.bmore = false
        recogResult.recogResultItemList = ArrayList()
        recogResult.syllableResultItemList = ArrayList()
    }

}

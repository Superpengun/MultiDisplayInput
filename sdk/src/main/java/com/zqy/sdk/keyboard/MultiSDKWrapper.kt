package com.zqy.sdk.keyboard

import android.util.Log
import com.sinovoice.hcicloudsdk.api.HciCloudSys
import com.sinovoice.hcicloudsdk.api.kb.HciCloudKb
import com.sinovoice.hcicloudsdk.common.HciErrorCode
import com.sinovoice.hcicloudsdk.common.Session
import com.sinovoice.hcicloudsdk.common.kb.*
import com.zqy.sdk.Constants
import com.sinovoice.jtandroiddevutil.log.JTLog
import com.zqy.sdk.tools.HciCloudUtils
import java.util.ArrayList

/**
 * @author:zhenqiyuan
 * @data:2022/1/14
 * @描述：
 * @package:keyboard
 */
class MultiSDKWrapper : InputSDKWrapper {
    private val TAG = MultiSDKWrapper::class.java.simpleName
    private lateinit var mKbConfig: KbConfig
    private var mKbSession: Session ?=null
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
        mKbConfig.addParam(Constants.KBConstant.RES_PROFIX, resPreFix)

        // 设置输出结果页大小
        mKbConfig.addParam(
            KbConfig.ResultConfig.PARAM_KEY_PAGE_COUNT, ""
                    + Constants.KBConstant.QUERY_COUNT
        )
        mKbConfig.addParam(
            KbConfig.InputConfig.PARAM_KEY_INPUT_MODE,
            Constants.KBConstant.INPUT_MODE_LOWER
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
        val enKbQueryInfo = KbQueryInfo()
        enKbQueryInfo.query = query

        // 查询
        val errCode = HciCloudKb.hciKbRecog(
            mKbSession,
            mKbConfig.stringConfig,
            enKbQueryInfo,
            mCurrentKbResult
        )

        // 对识别结果后处理
        if (errCode == HciErrorCode.HCI_ERR_NONE) {
            extendsKBRecogResult(mCurrentKbResult, enKbQueryInfo.query)
        } else {
            JTLog.e(TAG, "query [" + query + "] error : code =[" + errCode + "] msg = [" + HciCloudSys.hciGetErrorInfo(errCode) + "]")
        }
        return HciCloudUtils.transForm(mCurrentKbResult)
    }

    override fun getMore(): RecogResult? {
        mCurrentKbResult = kbGetNextReg(mKbSession, mKbConfig, mCurrentKbResult)
        return HciCloudUtils.transForm(mCurrentKbResult)
    }

    // 英文模式下，按需要增加compoing string 为第一个候选词
    private fun extendsKBRecogResult(recogResult: KbRecogResult?, queryContent: String) {
        val item = KbRecogResultItem()
        item.result = queryContent
        val templist = ArrayList<KbRecogResultItem>()
        if (recogResult!!.recogResultItemList != null) {
            if (recogResult.recogResultItemList.size == 0 || !recogResult.recogResultItemList[0].result.equals(
                    item.result,
                    ignoreCase = true
                )
            ) {
                templist.add(item)
            }
            for (item1 in recogResult.recogResultItemList) {
                templist.add(item1)
            }
            recogResult.recogResultItemList = templist
        }
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

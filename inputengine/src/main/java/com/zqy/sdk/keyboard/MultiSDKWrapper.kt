package com.zqy.sdk.keyboard

import com.sinovoice.hcicloudsdk.api.kb.HciCloudKb
import com.sinovoice.hcicloudsdk.common.HciErrorCode
import com.sinovoice.hcicloudsdk.common.Session
import com.sinovoice.hcicloudsdk.common.kb.*
import com.superpenguin.foreigninputandoutput.inputEngine.KeyboardEngine
import java.util.ArrayList

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.sdk.keyboard
 */
class MultiSDKWrapper : InputSDKWrapper() {
    private val TAG = MultiSDKWrapper::class.java.simpleName
    private lateinit var mKbConfig: KbConfig
    private var mKbSession: Session?=null
    private var resPreFix = ""
    private lateinit var mRecogResult : RecogResult

    override fun init(): Boolean {
        mRecogResult = RecogResult()
        return  false
    }

    override fun setResPreFix(lan: String?) {
        resPreFix = if (lan == "fr" || lan == "de"){
            lan
        } else {
            "en"
        }
        KeyboardEngine.switchLanguage(resPreFix)
    }

    override fun submitUDB(content: String, syllable: String) {
    }

    override fun release(): Boolean {
        return true
    }

    override fun query(query: String?): RecogResult {
        if (query != null) {
            mRecogResult.setRecogResult(KeyboardEngine.queryWord(query))
        }
        return  mRecogResult
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

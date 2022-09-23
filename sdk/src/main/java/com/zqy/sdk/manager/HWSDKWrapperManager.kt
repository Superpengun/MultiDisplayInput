package com.zqy.sdk.manager

import com.zqy.sdk.handwrite.*

/**
 * @author:zhenqiyuan
 * @data:2022/9/22
 * @描述：
 * @package:com.zqy.sdk.manager
 */
class HWSDKWrapperManager {
    private val TAG = HWSDKWrapperManager::class.java.simpleName
    private var mMainHWSDKWrapper: HandWriteSDKWrapper = EnglishHwSDKWrapper()
    private var mMainAssociateSDKWrapper: AssociateSDKWrapper = EnglishAssociateSDKWrapper()
    private var mInitHWSession = false
    private var mInitAssociateSession = false

    /**
     * 切换语种
     */
    fun changeLanguage(lan: String?) {
        if (lan != null) {
            if (lan == "_en_") {
                mMainHWSDKWrapper = EnglishHwSDKWrapper()
                mMainAssociateSDKWrapper = EnglishAssociateSDKWrapper()
            } else if (lan == "_cn_") {
                mMainHWSDKWrapper = ChineseHwSDKWrapper()
                mMainAssociateSDKWrapper = ChineseAssociateSDKWrapper()
            }
        }
        if (mInitHWSession) {
            mMainHWSDKWrapper.release()
        }
        mInitHWSession = mMainHWSDKWrapper.init()
        if (mInitAssociateSession) {
            mMainAssociateSDKWrapper.release()
        }
        mInitAssociateSession = mMainAssociateSDKWrapper.init()
    }

    /**
     * 手写查询
     */
    fun recog(points: ShortArray?): ArrayList<String?> {
        return mMainHWSDKWrapper.recog(points)
    }

    /**
     * 联想查询
     */
    fun associateQuery(word: String?): ArrayList<String?>? {
        return mMainAssociateSDKWrapper.associate(word)
    }

    /**
     * 更新联想词频
     */
    fun raisePriority(word: String) {
        mMainAssociateSDKWrapper.raisePriority(word)
    }

    fun release(){
        mMainHWSDKWrapper.release()
        mMainAssociateSDKWrapper.release()
    }
}
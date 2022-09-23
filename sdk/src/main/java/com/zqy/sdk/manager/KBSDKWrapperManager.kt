package com.zqy.sdk.manager

import com.zqy.sdk.keyboard.*

/**
 * @author:zhenqiyuan
 * @data:2022/9/22
 * @描述：
 * @package:com.zqy.sdk.manager
 */
class KBSDKWrapperManager {
    private val TAG = KBSDKWrapperManager::class.java.simpleName
    private var mMainSDKWrapper: InputSDKWrapper = MultiSDKWrapper()
    private var mInitSession = false

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
    }
}
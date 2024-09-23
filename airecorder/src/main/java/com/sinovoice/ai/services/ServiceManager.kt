package com.sinovoice.ai.services

import com.orhanobut.logger.Logger
import com.sinovoice.ai.net.ApiResponse
import com.sinovoice.ai.net.NetConstant
import com.sinovoice.ai.net.RetrofitManager
import com.sinovoice.ai.utils.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class ServiceManager {

    companion object {
        private var instance: ServiceManager? = null
        private const val TAG = "ServiceManager"

        @Synchronized
        fun get(): ServiceManager {
            if (instance == null) {
                instance = ServiceManager()
            }
            return instance!!
        }
    }


    fun getAnswer(question: String, listener: ServiceListener) {
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                RetrofitManager.service.getAnswer(
                    ServiceRequestBean(
                        robotCode = NetConstant.ROBOT_CODE,
                        question = question,
                        sendTime = System.currentTimeMillis(),
                        sessionId = NetConstant.ROBOT_CODE + TimeUtil.longToString(
                            System.currentTimeMillis(),
                            "yyyyMMddHHmm"
                        )
                    ), NetConstant.SERVICE_APP_KEY
                )
            }.onSuccess { data: ApiResponse<KeFuResult> ->
                listener.onServiceResult(data.result?.answerResult)
            }.onFailure { e ->
                Logger.t(TAG).d("processNLU: getSessionResult Fail")
            }
        }
    }
}

interface ServiceListener {
    fun onServiceResult(result: AnswerResult?)
}

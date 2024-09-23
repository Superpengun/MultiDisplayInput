package com.sinovoice.ai.nlu

import com.orhanobut.logger.Logger
import com.sinovoice.ai.net.NetConstant
import kotlinx.coroutines.*

internal class NLUManager {

    companion object {
        private var instance: NLUManager? = null

        @Synchronized
        fun get(): NLUManager {
            if (instance == null) {
                instance = NLUManager()
            }
            return instance!!
        }
    }

    private var currentNLUJob: Job? = null
    private val TAG = NLUManager::class.java.simpleName
    private var token = ""
    private var currentSession: StartSessionResult? = null


    fun startNLUSession(): Boolean {
        val startSessionResBean = runBlocking {
            if (token.isEmpty()) {
                token = try {
                    NLURetrofit.service.getToken(appKey = NetConstant.NLU_APP_KEY).token
                } catch (e: Exception) {
                    Logger.t(TAG).e("getToken failed")
                    ""
                }
            }
            try {
                NLURetrofit.service.startSession(
                    NetConstant.NLU_APP_KEY,
                    StartSessionRequest(
                        Config(
                            multiIntent = true,
                            needContext = true
                        ), NetConstant.NLU_ROBOT_ID
                    ), token
                )
            } catch (e: Exception) {
                Logger.t(TAG).e("startSession failed:${e.message}")
                null
            }
        }
        currentSession = startSessionResBean?.result
        return startSessionResBean?.result != null
    }

    fun stopNLUSession() {
        if (currentSession != null && token.isNotEmpty()) {
            runBlocking {
                NLURetrofit.service.stopSession(
                    NetConstant.NLU_APP_KEY, StopSessionRequest(
                        currentSession!!.sessionId
                    ), token
                )
            }
            currentSession = null
        } else {
            Logger.t(TAG).w("stopSession failed session not exits")
        }
    }

    fun processNLU(question: String, listener: NLUEventListener) {
        if (currentSession == null || token.isEmpty()) {
            Logger.t(TAG).d("processNLU: getSessionResult Fail")
            return
        }
        Logger.t(TAG).d("processNLU: getSessionResult question:$question")
        currentNLUJob = GlobalScope.launch(Dispatchers.IO) {
            val result: DialogResBean? = try {
                NLURetrofit.service.dialog(
                    NetConstant.NLU_APP_KEY,
                    DialogRequest(
                        Config(
                            multiIntent = true,
                            needContext = true
                        ), currentSession!!.sessionId, question
                    ), token
                )
            } catch (e: Exception) {
                Logger.t(TAG).d("processNLU: getSessionResult Fail")
                listener.onNLUResult(
                    NluResult(
                        ""
                    )
                )
                return@launch
            }
            handleNLUResult(result, listener)
            currentNLUJob = null
        }
    }


    fun cancelNLU() {
        currentNLUJob?.cancel()
    }

    fun release() {

    }


    private fun handleNLUResult(
        result: DialogResBean?,
        listener: NLUEventListener
    ) {
        result!!.result?.let { dialogResult ->
            if (dialogResult.responses[0].cmd == null) {
                listener.onNLUResult(
                    NluResult(
                        dialogResult.responses[0].content,
                        "识别错误",
                        isCmdFinish(dialogResult.context)
                    )
                )
            } else {
                dialogResult.responses[0].cmd?.let { cmd ->
//                    val cmdBean = GsonUtils.fromJson(cmd, NluCmdBean::class.java)
                    listener.onNLUResult(
                        NluResult(
                            dialogResult.responses[0].content,
                            dialogResult.responses[0].cmd.toString(),
                            isCmdFinish(dialogResult.context)
                        )
                    )
                }
            }
        }
    }

    private fun isCmdFinish(context: IntentContext?): Boolean {
        return context?.intent?.state?.contentEquals("FINISH") ?: false
    }
}
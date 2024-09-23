package com.sinovoice.ai.nlu


import retrofit2.http.*

/**
 *    @author : liufenghui
 *    date   : 2020/4/7
 */
interface NLUApiService {
    /**
     * 获取nlu令牌
     */
    @GET("auth/get-access-token")
    suspend fun getToken(@Query("appkey") appKey: String): NLUTokenResBean

    /**
     * 开始会话
     */
    @POST(value = "nlu/resource/cn_common/start_session")
    suspend fun startSession(
        @Query("appkey") appKey: String,
        @Body request: StartSessionRequest,
        @Header(value = "X-Hci-Access-Token") token: String
    ): StartSessionResBean

    /**
     * 对话
     */
    @POST(value = "nlu/resource/cn_common/dialog")
    suspend fun dialog(
        @Query("appkey") appKey: String,
        @Body request: DialogRequest,
        @Header(value = "X-Hci-Access-Token") token: String
    ): DialogResBean

    /**
     * 关闭session
     */
    @POST(value = "nlu/resource/cn_common/end_session")
    suspend fun stopSession(
        @Query("appkey") appKey: String,
        @Body request: StopSessionRequest,
        @Header(value = "X-Hci-Access-Token") token: String
    ): StopSessionResBean

}
package com.sinovoice.ai.net

import com.sinovoice.ai.services.KeFuResult
import com.sinovoice.ai.services.ServiceRequestBean
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    //获取客服回答
    @POST(value = "service/qa")
    suspend fun getAnswer(
        @Body request: ServiceRequestBean,
        @Header(value = "appkey") appid: String
    ): ApiResponse<KeFuResult>


    companion object {
        const val MEDIA_TXT = "application/json;charset=utf-8"
    }
}
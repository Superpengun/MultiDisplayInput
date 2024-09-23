package com.sinovoice.ai.net

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * @author CWQ
 * @date 2022/3/4
 */
class HeaderInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val requestBuilder = request.newBuilder()

        val appkey = NetConstant.APP_KEY
        val auth: String = "$appkey:" + NetConstant.SERVICE_SECRET
        val encodeAuth = Base64.encodeToString(auth.toByteArray(), Base64.NO_WRAP)
        requestBuilder.addHeader("Authorization", "Basic $encodeAuth")
        return chain.proceed(requestBuilder.build())
    }
}
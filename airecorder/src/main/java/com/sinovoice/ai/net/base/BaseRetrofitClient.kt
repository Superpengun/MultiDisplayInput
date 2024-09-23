package com.sinovoice.ai.net.base

import com.sinovoice.ai.net.HeaderInterceptor
import com.sinovoice.ai.utils.HttpsUtils
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

open class BaseRetrofitClient {
    companion object {
        const val TIME_OUT = 5L
    }

    private val client: OkHttpClient by lazy {
        val sslParams = HttpsUtils.getSslSocketFactory(HttpsUtils.UnSafeTrustManager)
        val builder = OkHttpClient.Builder()
            .addInterceptor(getHeaderInterceptor())
            .addInterceptor(getHttpLoggingInterceptor())
            .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
        builder.build()
    }

    private fun getHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }

    open fun getHeaderInterceptor(): Interceptor {
        return HeaderInterceptor()
    }

    fun <Service> getService(serviceClass: Class<Service>, baseUrl: String): Service {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
            .create(serviceClass)
    }
}
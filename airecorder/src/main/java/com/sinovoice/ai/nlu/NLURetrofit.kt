package com.sinovoice.ai.nlu

import com.sinovoice.ai.net.NetConstant
import com.sinovoice.ai.net.base.BaseRetrofitClient
import okhttp3.Interceptor

/**
 * @Author:CWQ
 * @DATE:2022/8/9
 * @DESC:
 */
object NLURetrofit : BaseRetrofitClient() {

    val service by lazy {
        getService(NLUApiService::class.java, NetConstant.NLU_SERVER_URL)
    }

    override fun getHeaderInterceptor(): Interceptor {
        return NLUHeaderInterceptor()
    }
}
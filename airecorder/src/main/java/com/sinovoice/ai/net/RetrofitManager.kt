package com.sinovoice.ai.net

import com.sinovoice.ai.net.base.BaseRetrofitClient

object RetrofitManager : BaseRetrofitClient() {

    val service by lazy {
        getService(ApiService::class.java, NetConstant.SERVICE_URL)
    }
}
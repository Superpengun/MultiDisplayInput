package com.zqy.sdk.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * @author:zhenqiyuan
 * @data:2022/11/2
 * @描述：
 * @package:com.zqy.sdk.network
 */
interface QueryService {
    @GET
    fun queryStatus (@Url fileUrl: String?) : Call<QueryResponse>
}
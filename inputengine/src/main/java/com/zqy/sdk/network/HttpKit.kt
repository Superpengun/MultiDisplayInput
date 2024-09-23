package com.zqy.sdk.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

/**
 * @author:zhenqiyuan
 * @data:2022/11/2
 * @描述：
 * @package:com.zqy.sdk.network
 */

typealias download_error = suspend (Throwable) -> Unit
typealias download_process = suspend (downloadedSize: Long, length: Long, progress: Float) -> Unit
typealias download_success = suspend (uri: File) -> Unit

object HttpKit {

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://www.xxx.com")
        .validateEagerly(true) //在开始的时候直接开始检测所有的方法
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: DownLoadService = retrofit.create(DownLoadService::class.java)
    val queryService: QueryService = retrofit.create(QueryService::class.java)
}
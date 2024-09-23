package com.zqy.sdk.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownLoadService {
 	@Streaming
 	@GET
 	suspend fun downloadFile(@Url fileUrl: String?): ResponseBody
 }
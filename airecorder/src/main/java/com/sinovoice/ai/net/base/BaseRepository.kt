package com.sinovoice.ai.net.base

import com.sinovoice.ai.net.ApiErrorResponse
import com.sinovoice.ai.net.ApiFailedResponse
import com.sinovoice.ai.net.ApiResponse
import com.sinovoice.ai.net.ApiSuccessResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class BaseRepository {
    private val TAG = "BaseRepository"

    suspend fun <T> executeHttp(block: suspend () -> ApiResponse<T>): ApiResponse<T> {
        runCatching {
            withContext(Dispatchers.IO) {
                block.invoke()
            }
        }.onSuccess { data: ApiResponse<T> ->
            return handleHttpOk(data)
        }.onFailure { e ->
            return handleHttpError(e)
        }
        return ApiErrorResponse(Exception())
    }

    /**
     * 非后台返回错误
     */
    private fun <T> handleHttpError(e: Throwable): ApiResponse<T> {
        return ApiErrorResponse(e)
    }


    private fun <T> handleHttpOk(data: ApiResponse<T>): ApiResponse<T> {
        return if (data.isSuccess) {
            ApiSuccessResponse(data.result)
        } else {
            ApiFailedResponse(data.code, data.msg)
        }
    }
}
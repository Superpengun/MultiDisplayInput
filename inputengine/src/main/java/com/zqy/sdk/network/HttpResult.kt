package com.zqy.sdk.network

/**
 * @author:zhenqiyuan
 * @data:2022/11/2
 * @描述：
 * @package:com.zqy.sdk.network
 */
class HttpResult<out T> constructor(val value: Any?) {

    val isSuccess: Boolean get() = value !is Failure && value !is Progress

    val isFailure: Boolean get() = value is Failure

    val isLoading: Boolean get() = value is Progress

    fun exceptionOrNull(): Throwable? =
        when (value) {
            is Failure -> value.exception
            else -> null
        }

    companion object {
        fun <T> success(value: T): HttpResult<T> =
            HttpResult(value)

        fun <T> failure(exception: Throwable): HttpResult<T> =
            HttpResult(createFailure(exception))

        fun <T> progress(currentLength: Long, length: Long, process: Float): HttpResult<T> =
            HttpResult(createLoading(currentLength, length, process))
    }

    data class Failure(val exception: Throwable)

    data class Progress(val currentLength: Long, val length: Long, val process: Float)
}


private fun createFailure(exception: Throwable): HttpResult.Failure =
    HttpResult.Failure(exception)


private fun createLoading(currentLength: Long, length: Long, process: Float) =
    HttpResult.Progress(currentLength, length, process)


inline fun <R, T> HttpResult<T>.fold(
    onSuccess: (value: T) -> R,
    onLoading: (loading: HttpResult.Progress) -> R,
    onFailure: (exception: Throwable?) -> R
): R {
    return when {
        isFailure -> {
            onFailure(exceptionOrNull())
        }
        isLoading -> {
            onLoading(value as HttpResult.Progress)
        }
        else -> {
            onSuccess(value as T)
        }
    }
}

package com.sinovoice.ai.net

class ResultBuilder<T> {
    var onSuccess: (data: T?) -> Unit = {}

    var onFailed: (errorCode: String?, errorMsg: String?) -> Unit = { errorCode, errorMsg ->

    }

    var onError: (e: Throwable) -> Unit = {

    }

    var onComplete: () -> Unit = {}
}

fun <T> ApiResponse<T>.parseData(listenerBuilder: ResultBuilder<T>.() -> Unit) {
    val listener = ResultBuilder<T>().also(listenerBuilder)

    when (this) {
        is ApiSuccessResponse -> listener.onSuccess(this.response)
        is ApiFailedResponse -> listener.onFailed(this.code, this.msg)
        is ApiErrorResponse -> listener.onError(this.throwable)
    }
}
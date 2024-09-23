package com.sinovoice.ai.net

import java.io.Serializable

open class ApiResponse<T>(
    open val code: String? = null,
    open val msg: String? = null,
    open val error: Throwable? = null,
    open val result: T? = null
) : Serializable {
    val isSuccess: Boolean
        get() = (code == "10000")

    override fun toString(): String {
        return "ApiResponse(result=$result,status=$code,msg=$msg,error=$error)"
    }
}

data class ApiSuccessResponse<T>(val response: T?) : ApiResponse<T>(result = response)

data class ApiFailedResponse<T>(override val code: String?, override val msg: String?) :
    ApiResponse<T>(code = code, msg = msg)

data class ApiErrorResponse<T>(val throwable: Throwable) : ApiResponse<T>(error = throwable)
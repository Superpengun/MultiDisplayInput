package com.zqy.sdk.network

/**
 * @author:zhenqiyuan
 * @data:2022/11/2
 * @描述：
 * @package:com.zqy.sdk.network
 */
class QueryResponse(val code: Int, val data: Data) {
    class Data(val version: String, val download: String, val name: String, val filesize: Int)
}


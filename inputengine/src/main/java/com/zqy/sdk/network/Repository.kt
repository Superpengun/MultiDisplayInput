package com.zqy.sdk.network

import android.content.Context
import android.util.Log
import com.zqy.sdk.tools.FileUtils
import com.zqy.sdk.tools.HciCloudUtils
import com.zqy.input.common.utils.SharedPreferenceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * @author:zhenqiyuan
 * @data:2022/11/2
 * @描述：
 * @package:com.zqy.sdk.network
 */
object Repository {
    const val KEY_LOCAL_DICS_VERSION = "key_local_dics_version";
    private val TAG = Repository::class.java.canonicalName
    private const val UPDATE_DICS_SERVER =
        "http://www.aicloud.com/thesaurus/checkversion?app_key=485d5445&apk_version=1.0.0&thesaurus_symbol=Thesaurus_CN_Basis&thesaurus_version="

    fun queryCode(context: Context, version: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val queryResponse = HTTPDownLoader.queryStatus(UPDATE_DICS_SERVER + version)
                if (queryResponse.code == 0) {
                    download(context, queryResponse.data.download ,queryResponse.data.version)
                }else{
                    Log.d(TAG, "queryCode: " + queryResponse.code)
                }
            } catch (e: Exception) {
                Log.d(TAG, "exception")
            }
        }
    }

    private fun download(context: Context, url: String ,version : String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                HTTPDownLoader.download(
                    url,
                    "${HciCloudUtils.dataPath}/${"tmp.dict"}",
                    onProcess = { _, _, process ->
                        Log.i(TAG, "process =${(process * 100).toInt()}% \n")
                    },
                    onSuccess = {
                        Log.d(TAG, "download: success")
                        FileUtils.renameDic(HciCloudUtils.dataPath)
                        SharedPreferenceUtils.getInstance(context).put(KEY_LOCAL_DICS_VERSION,version)
                    },
                    onError = {
                        it.printStackTrace()
                    })
            } catch (e: Exception) {
                Log.d(TAG, "exception,connect failed")
            }
        }
    }
}
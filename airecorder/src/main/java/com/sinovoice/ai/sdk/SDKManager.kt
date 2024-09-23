package com.sinovoice.ai.sdk

import android.content.Context
import com.sinovoice.ai.Constant
import com.sinovoice.ai.R
import com.sinovoice.sdk.HciSdk
import com.sinovoice.sdk.HciSdkConfig
import com.sinovoice.sdk.LogLevel
import com.sinovoice.ai.recognize.SDKConstants
import com.sinovoice.ai.utils.CommonUtils
import java.io.File

internal class SDKManager private constructor() {
    private var sdk: HciSdk? = null

    companion object {
        private var instance: SDKManager? = null

        @Synchronized
        fun get(): SDKManager {
            if (instance == null) {
                instance = SDKManager()
            }
            return instance!!
        }
    }

    fun getSDK(): HciSdk? {
        return sdk
    }

    fun initSdk(context: Context, path: String = Constant.SDK_WORKSPACE): Boolean {
        if (sdk == null) {
            sdk = HciSdk()
        }
        checkPath(path)
        val cfg = getSdkConfig(context, path)
        sdk!!.setLogLevel(LogLevel.D)
        return (sdk!!.init(getSdkConfig(context, path), context) == 0)
    }

    private fun checkPath(path: String) {
        if (!File(path).exists()) {
            File(path).mkdirs()
        }
    }

    private fun getSdkConfig(
        context: Context,
        path: String
    ): HciSdkConfig {
        val cfg = HciSdkConfig()
        cfg.setAppkey(context.resources.getString(R.string.appKey))
        cfg.setSecret(context.resources.getString(R.string.secret))
        cfg.setSysUrl(context.resources.getString(R.string.sysUrl))
        cfg.setCapUrl(context.resources.getString(R.string.capUrl))
        cfg.setDataPath(path)
        if (CommonUtils.isReleaseVersion(context)) {
            CommonUtils.copyAssetsToFilesystem(
                context,
                SDKConstants.CA_FILE_NAME,
                path + SDKConstants.CA_FILE_NAME
            )
            cfg.setCaBundle(path + SDKConstants.CA_FILE_NAME)
            cfg.setVerifySSL(true)
        } else {
            cfg.setVerifySSL(false)
        }
        return cfg
    }

    fun releaseSdk() {
        val current = sdk
        current?.close()
        current?.waitForClosed()
        sdk = null
    }

}
package com.zqy.sdk

import android.content.Context
import com.sinovoice.hcicloudsdk.api.HciCloudSys
import com.sinovoice.hcicloudsdk.common.*
import com.sinovoice.jtandroiddevutil.log.JTLog
import com.zqy.sdk.tools.HciCloudUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * @author:zhenqiyuan
 * @data:2022/1/17
 * @描述：
 * @package:
 */
class SysSDKManager {
    private val TAG = SysSDKManager::class.java.canonicalName

    companion object {
        private var instance: SysSDKManager? = null
            get() {
                if (field == null) {
                    field = SysSDKManager()
                }
                return field
            }
        @Synchronized
        fun get(): SysSDKManager {
            return instance!!
        }
    }

    /**
     * 初始化灵云系统
     *
     * @return true : 初始化成功 <br></br>  false : 初始化失败
     */
    fun initHciCloudSys(context: Context): Boolean {
        HciCloudUtils.initDataPath(context)
        if (!copyBasicAuth(context)) {
            return false
        }
        val sysInitParamStr = getHciCloudSysInitParams(context).stringConfig
        JTLog.i(TAG, "initHciCloudSys sysInitParamStr: $sysInitParamStr")
        val errorCode = HciCloudSys.hciInit(sysInitParamStr, context)
        if (errorCode != HciErrorCode.HCI_ERR_NONE
            && errorCode != HciErrorCode.HCI_ERR_SYS_ALREADY_INIT
        ) {
            // 没有初始化成功
            JTLog.e(TAG, "initHciCloudSys failed return [" + errorCode + "]. msg = [" + HciCloudSys.hciGetErrorInfo(errorCode) + "]")
            return false
        }
        printAllCapkey()
        // 初始化成功
        JTLog.i(TAG, "initHciCloudSys success ")
        return true
    }


    /**
     * 释放灵云系统
     */
    fun releaseHciCloud(): Boolean {
        val errorCode = HciCloudSys.hciRelease()
        JTLog.i(TAG, "releaseHciCloud : return [" + errorCode + "]. msg = [" + HciCloudSys.hciGetErrorInfo(errorCode) + "]")
        return errorCode == HciErrorCode.HCI_ERR_NONE
    }

    private fun stringRes(resId: Int, context: Context): String? {
        return context.getString(resId)
    }

    /**
     * 获取灵云系统初始化配置参数
     *
     * @param context
     * @return 灵云系统初始化配置参数
     */
    private fun getHciCloudSysInitParams(context: Context): InitParam {
        var logDirPath =context.filesDir.absolutePath + context.resources.getString(R.string.log_path)
        // 创建日志路径
        if (!File(logDirPath).exists()) {
            File(logDirPath).mkdirs()
        }
        JTLog.i(TAG, "hcicloud log path: $logDirPath")
        val authDirPath = context.filesDir.absolutePath
        val initparam = InitParam()
        /****授权相关设置 */
        // 设置授权文件所在路径
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_AUTH_PATH, authDirPath)
        // 是否自动访问云授权，车在输入法一般设为NO
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_AUTO_CLOUD_AUTH, InitParam.VALUE_OF_NO)
        // 灵云云服务的接口地址，此项必填
        initparam.addParam(
            InitParam.AuthParam.PARAM_KEY_CLOUD_URL,
            stringRes(R.string.cloud_url, context)
        )
        //设置APP_KEY
        initparam.addParam(
            InitParam.AuthParam.PARAM_KEY_APP_KEY,
            stringRes(R.string.app_key, context)
        )
        //设置开发者密钥
        initparam.addParam(
            InitParam.AuthParam.PARAM_KEY_DEVELOPER_KEY,
            stringRes(R.string.developer_key, context)
        )
        /****日志相关设置 */
        // 日志数目，默认保留多少个日志文件，超过则覆盖最旧的日志
        initparam.addParam(
            InitParam.LogParam.PARAM_KEY_LOG_FILE_COUNT,
            stringRes(R.string.log_count, context)
        )
        // 日志的路径，可选，如果不传或者为空则不生成日志
        initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_PATH, logDirPath)
        // 日志大小，默认一个日志文件写多大，单位为K
        initparam.addParam(
            InitParam.LogParam.PARAM_KEY_LOG_FILE_SIZE,
            stringRes(R.string.log_file_size, context)
        )
        // 日志等级，0=无，1=错误，2=警告，3=信息，4=细节，5=调试，SDK将输出小于等于logLevel的日志信息
        initparam.addParam(
            InitParam.LogParam.PARAM_KEY_LOG_LEVEL,
            stringRes(R.string.log_level, context)
        )
        return initparam
    }

    /**
     * 将授权文件拷进app安装目录
     * 在灵云系统初始化之前调用
     *
     * @return true : 成功 <br></br>  false : 失败
     */
    private fun copyBasicAuth(context: Context): Boolean {
        try {
            val fileName = "HCI_BASIC_AUTH"
            val `in` = context.resources.openRawResource(
                R.raw.hci_basic_auth
            )
            val authSize = `in`.available()
            val file = File(context.filesDir, fileName)
            if (file.exists() && file.length() == authSize.toLong()) {
                `in`.close()
                return true
            }
            val fos = context.openFileOutput(
                fileName,
                Context.MODE_PRIVATE
            )
            val bt = ByteArray(1024)
            var count: Int
            while (`in`.read(bt).also { count = it } > 0) {
                fos.write(bt, 0, count)
            }
            `in`.close()
            fos.close()
            JTLog.i(TAG, "copyBasicAuth success")
            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        JTLog.i(TAG, "copyBasicAuth failed")
        return false
    }


    /**
     * 检查授权
     *
     * @return [Constants.HciCloudSysConstants.SYS_AUTH_SUCCESS]—检查授权成功<br></br>
     * [Constants.HciCloudSysConstants.SYS_AUTH_HINT]—检查授权成功，但即将到期<br></br>
     * [Constants.HciCloudSysConstants.SYS_AUTH_FAILED]—检查授权失败
     */
    private fun checkAuth(): Int {

        //检查授权文件是否到期
        val expireTime = AuthExpireTime()
        val errorCode = HciCloudSys.hciGetAuthExpireTime(expireTime)

        // 无授权文件，或者读取授权文件错误
        if (errorCode == HciErrorCode.HCI_ERR_SYS_AUTHFILE_INVALID) {
            JTLog.i(TAG, "checkAuth failed: $errorCode")
            return Constants.HciCloudSysConstants.SYS_AUTH_FAILED
        } else if (errorCode == HciErrorCode.HCI_ERR_NONE) {
            // 读取成功并判断过期时间
            val expireTimeValue = expireTime.expireTime
            val currTime = System.currentTimeMillis()
            val timeDifference = expireTimeValue * 1000 - currTime
            return if (timeDifference < Constants.HciCloudSysConstants.AUTH_HINT_TIME) {
                JTLog.i(TAG, "checkAuth success,but will finished")
                Constants.HciCloudSysConstants.SYS_AUTH_HINT
            } else {
                JTLog.i(TAG, "checkAuth success")
                // 检查都有哪些能力可用
                val capabilityResult = CapabilityResult()
                HciCloudSys.hciGetCapabilityList(null, capabilityResult)
                val capkeyList = capabilityResult
                    .capabilityList
                if (capkeyList != null && capkeyList.size > 0) {
                    for (capabilityItem in capkeyList) {
                        JTLog.i(TAG, "capability:" + capabilityItem.capKey)
                    }
                } else {
                    println("no capability found!")
                    JTLog.i(TAG, "no capability found!")
                }
                JTLog.i(TAG, "hciCloudCheckAuthExpireTimeAndCapKeys() End.")
                Constants.HciCloudSysConstants.SYS_AUTH_SUCCESS
            }
        }
        JTLog.e(TAG, "checkAuth failed: [" + errorCode + "]. msg = [" + HciCloudSys.hciGetErrorInfo(errorCode) + "]"
        )
        return Constants.HciCloudSysConstants.SYS_AUTH_FAILED
    }

    /**
     * 打印账号全部能力
     */
    private fun printAllCapkey() {
        JTLog.d(TAG, "printAllCapkey()")
        val capabilityResult = CapabilityResult()
        HciCloudSys.hciGetCapabilityList(null, capabilityResult)
        val capkeyList = capabilityResult.capabilityList
        if (capkeyList != null && capkeyList.size > 0) {
            for (capabilityItem in capkeyList) {
                JTLog.d(TAG, "capability:" + capabilityItem.capKey)
            }
        } else {
            JTLog.e(TAG, "no capability found!")
        }
    }
}
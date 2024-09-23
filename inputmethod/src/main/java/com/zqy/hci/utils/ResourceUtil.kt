package com.zqy.hci.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.DisplayMetrics
import java.io.File


/**
 * @author:zhenqiyuan
 * @data:2023/1/6
 * @描述：
 * @package:com.sinovoice.hci.utils
 */
object ResourceUtil {

    private var skinResources: Resources? = null
    private var appResources: Resources? = null
    private var currentResources: Resources? = null
    private var skinPackageName: String? = null
    private var appPackageName: String? = null
    private val basePath =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "skin-debug.apk"

    fun init(context: Context) {
        skinResources = getSkinResource(context, basePath)
        appResources = context.resources
        currentResources = appResources
        skinPackageName = getPackName(basePath, context)
        appPackageName = context.packageName
    }

    fun getColor(name: String): Int {
        return currentResources!!.getColor(getColorId(name), null)
    }

    private fun getColorId(name: String): Int {
        return getIdentifier(name, "color")
    }

    fun getDrawable(name: String): Drawable {
        return currentResources!!.getDrawable(getDrawableId(name), null)
    }

    private fun getDrawableId(name: String): Int {
        return getIdentifier(name, "drawable")
    }

    fun getString(name: String): CharSequence {
        return currentResources!!.getString(getStringId(name))
    }

    private fun getStringId(name: String): Int {
        return getIdentifier(name, "string")
    }

    private fun getSkinResource(context: Context, path: String): Resources? {
        try {
            val assetManager = AssetManager::class.java.newInstance()
            @SuppressLint("DiscouragedPrivateApi") val method =
                AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
            method.isAccessible = true
            /// 反射执行方法
            method.invoke(assetManager, path)
            return Resources(
                assetManager,
                createDisplayMetrics(context),
                createConfiguration(context)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getIdentifier(name: String, defType: String): Int {
        var id = 0
        if (skinResources != null) {
            currentResources = skinResources
            id = currentResources!!.getIdentifier(name, defType, skinPackageName)
        }
        if (id <= 0) {
            currentResources = appResources
            id = currentResources!!.getIdentifier(name, defType, appPackageName)
        }
        return id
    }

    private fun getPackName(path: String, context: Context): String? {
        val info: PackageInfo? =
            context.packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)
        if (info != null) {
            val appInfo = info.applicationInfo
            return appInfo.packageName
        }
        return null
    }

    private fun createDisplayMetrics(context: Context): DisplayMetrics? {
        return context.resources.displayMetrics
    }

    private fun createConfiguration(context: Context): Configuration? {
        return context.resources.configuration
    }

    init {

    }
}
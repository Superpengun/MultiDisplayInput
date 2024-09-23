package com.sinovoice.ai.utils

import android.content.Context
import android.content.res.AssetManager
import android.os.Environment
import java.io.*
import java.util.*

object FileUtils {

    /**
     * 拷贝assets/data目录下的文件到指定目录
     *
     * @param context
     * 上下文
     * @param dataPath
     * 指定目录
     * @return
     */
    fun copyDataAssetsFiles(
        context: Context,
        dataPath: String,
        overWrite: Boolean = false
    ) {
        // 没有文件夹，则创建
        if (!File(dataPath).exists()) {
            File(dataPath).mkdirs()
        }
        val assetMgr = context.resources.assets
        try {
            val filesList = assetMgr.list("data")
            loopFiles(
                filesList,
                assetMgr,
                "data" + File.separator,
                dataPath + File.separator, overWrite
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    /**
     * 拷贝assets/data目录下的文件到指定目录
     *
     * @param context
     * 上下文
     * @param dataPath
     * 指定目录
     * @return
     */
    fun copyTTSAssetsFiles(
        context: Context,
        dataPath: String
    ) {
        // 没有文件夹，则创建
        if (!File(dataPath).exists()) {
            File(dataPath).mkdirs()
        }
        val assetMgr = context.resources.assets
        try {
            val filesList = assetMgr.list("tts")
            loopFiles(
                filesList,
                assetMgr,
                "tts" + File.separator,
                dataPath + File.separator
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 拷贝assets/map目录下的文件到指定目录
     * @param context Context
     * @param dataPath String 指定目录
     */
    fun copyMapAssetsFiles(
        context: Context,
        dataPath: String
    ) {
        // 没有文件夹，则创建
        if (!File(dataPath).exists()) {
            File(dataPath).mkdirs()
        }
        val assetMgr = context.resources.assets
        try {
            val filesList = assetMgr.list("map")
            loopFiles(
                filesList,
                assetMgr,
                "map" + File.separator,
                dataPath + File.separator
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loopFiles(
        filesList: Array<String>?,
        assetMgr: AssetManager,
        srcPath: String,
        destPath: String,
        overWrite: Boolean = false
    ) {

        for (string in filesList!!) {
            try {
                val file = assetMgr.list(srcPath + string)
                if (file!!.isEmpty()) {
                    copyAssetFile(
                        assetMgr,
                        srcPath + string,
                        destPath + string
                    )
                } else {
                    if (!File(destPath + string).exists()) {
                        File(destPath + string).mkdirs()
                    }
                    loopFiles(
                        file, assetMgr,
                        srcPath + string + File.separator, destPath
                                + string + File.separator, overWrite
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun copyAssetFile(
        assetMgr: AssetManager?, src: String?,
        dst: String?, overWrite: Boolean = false
    ) {
        if (assetMgr == null) {
            throw NullPointerException("Method param assetMgr is null.")
        }
        if (src == null) {
            throw NullPointerException("Method param src is null.")
        }
        if (dst == null) {
            throw NullPointerException("Method param dst is null.")
        }
        var `is`: InputStream? = null
        var dis: DataInputStream? = null
        var fos: FileOutputStream? = null
        var dos: DataOutputStream? = null
        try {
            `is` = assetMgr.open(src, AssetManager.ACCESS_RANDOM)
            dis = DataInputStream(`is`)
            val file = File(dst)
            if (file.exists() && !overWrite) {
                return
                //return;
            }
            file.createNewFile()
            fos = FileOutputStream(file)
            dos = DataOutputStream(fos)
            val buffer = ByteArray(1024)
            var len = 0
            while (dis.read(buffer, 0, buffer.size).also { len = it } != -1) {
                dos.write(buffer, 0, len)
                dos.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (dis != null) {
                    dis.close()
                    dis = null
                }
                if (`is` != null) {
                    `is`.close()
                    `is` = null
                }
                if (dos != null) {
                    dos.close()
                    dos = null
                }
                if (fos != null) {
                    fos.close()
                    fos = null
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    fun splitBytes(bytes: ByteArray, size: Int): List<ByteArray>? {
        val splitLength = (size.toString() + "").toDouble()
        val arrayLength = Math.ceil(bytes.size / splitLength).toInt()
        val result: MutableList<ByteArray> = ArrayList()
        var from: Int
        var to: Int
        for (i in 0 until arrayLength) {
            from = (i * splitLength).toInt()
            to = (from + splitLength).toInt()
            if (to > bytes.size) to = bytes.size
            result.add(Arrays.copyOfRange(bytes, from, to))
        }
        return result
    }

    /**
     * 获取缓存路径
     * @param context Context
     * @return String
     */
    fun getDiskCacheDir(context: Context): String {
        return if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            context.externalCacheDir!!.path
        } else {
            context.cacheDir.path
        }
    }
}
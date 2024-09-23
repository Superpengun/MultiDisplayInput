package com.zqy.sdk.tools

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.zqy.input.common.utils.SharedPreferenceUtils
import com.zqy.sdk.network.Repository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * @author:zhenqiyuan
 * @data:2022/11/1
 * @描述：
 * @package:com.zqy.sdk.tools
 */
object FileUtils {
    fun copyDicData(context: Context, path: String) {
        val parent = File(path)
        if (!parent.exists()) {
            parent.mkdirs()
        }
        val testFile = File(path, "_en_kb.conf")
        if (testFile.exists()) {
            return
        }
        val listFile = getAssertsFiles(context)
        if (listFile != null) {
            for (str in listFile) {
                try {
                    if (str != null) {
                        copyFileFromAssets(context, str, path)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        SharedPreferenceUtils.getInstance(context).put(Repository.KEY_LOCAL_DICS_VERSION,"1.0.0")
    }

    fun renameDic(path : String){
        try {
            val oldFile = File(path+ File.separator+"tmp.dict")
            Log.d("Repository", "renameDic: ${oldFile.path}")
            val newFile = File(path+ File.separator+"_cn_kb.dic")
            val fis = FileInputStream(oldFile)
            val fos = FileOutputStream(newFile)
            val bt = ByteArray(1024)
            var count: Int
            while (fis.read(bt).also { count = it } > 0) {
                fos.write(bt, 0, count)
            }

            fis.close()
            fos.close()
        }catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 读取asserts目录下的文件
     * @param  "updatelog.txt"
     * @return 对应文件的内容
     *
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    private fun copyFileFromAssets(context: Context, srcFile: String, dataPath: String) {
        require(!(TextUtils.isEmpty(srcFile))) { "bad arguments!" }
        val assetManager = context.assets
        val input = assetManager.open(srcFile)
        val file = File(dataPath, srcFile)
        if (file.exists() && file.length() == input.available().toLong()) {
            input.close()
        }
        val fos = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var length = 0
        while (input.read(buffer).also { length = it } != -1) {
            fos.write(buffer, 0, length)
        }
        fos.close()
        input.close()
    }

    /**
     * 列出Asserts文件夹下的所有文件
     * @return asserts目录下的文件名列表
     *
     */
    @Throws(IllegalArgumentException::class)
    private fun getAssertsFiles(context: Context?): MutableList<String?>? {
        requireNotNull(context) { "bad arguments!" }
        val assetManager = context.assets
        var files: Array<String?>? = null
        try {
            files = assetManager.list("")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return if (null == files) null else Arrays.asList(*files)
    }
}

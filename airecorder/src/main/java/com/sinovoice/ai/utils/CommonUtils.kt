package com.sinovoice.ai.utils

import android.content.Context
import android.content.res.AssetManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.google.gson.Gson
import com.sinovoice.ai.R
import com.sinovoice.ai.recognize.RecognizeResult
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object CommonUtils {
    fun isReleaseVersion(context: Context): Boolean {
        return context.resources.getString(R.string.environment).equals("release")
    }

    fun copyAssetsToFilesystem(
        context: Context,
        assetsSrc: String,
        des: String
    ): Boolean {

        var istream: InputStream? = null
        var ostream: OutputStream? = null
        try {
            val am: AssetManager = context.getAssets()
            istream = am.open(assetsSrc)
            ostream = FileOutputStream(des)
            val buffer = ByteArray(1024)
            var length: Int
            while (istream.read(buffer).also { length = it } > 0) {
                ostream.write(buffer, 0, length)
            }
            istream.close()
            ostream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                istream?.close()
                ostream?.close()
            } catch (ee: Exception) {
                ee.printStackTrace()
            }
            return false
        }
        return true
    }

    fun isNetworkAvailable(context: Context?): Boolean {
        val connectivity =
            context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivity.activeNetworkInfo
        if (info != null && info.isConnected) {
            // 当前网络是连接的
            // 当前所连接的网络可用
            return info.state == NetworkInfo.State.CONNECTED
        }
        return false
    }


    fun parseJson(jsonString: String): RecognizeResult {
        return Gson().fromJson(jsonString, RecognizeResult::class.java)
    }
}


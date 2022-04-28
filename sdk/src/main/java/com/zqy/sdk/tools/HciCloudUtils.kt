package com.zqy.sdk.tools

import android.content.Context
import android.content.pm.PackageManager
import com.sinovoice.hcicloudsdk.common.kb.KbRecogResult
import com.zqy.sdk.keyboard.RecogResult
import com.zqy.sdk.keyboard.RecogResultItem
import com.zqy.sdk.keyboard.ResultMatchItem
import java.io.File
import java.util.ArrayList

/**
 * @author:zhenqiyuan
 * @data:2022/1/15
 * @描述：
 * @package:
 */
class HciCloudUtils {
    companion object {
        var dataPath: String= ""
        val SYSTEM_LIB = "/system/lib"
        fun initDataPath(context: Context) {
            dataPath = context.filesDir.absolutePath.replace("files", "lib")
            val dataSoPath = "$dataPath/libjtz.so"
            if (!File(dataSoPath).exists()) {
                dataPath = SYSTEM_LIB
            }
        }

        fun transForm(kbRecogResult: KbRecogResult?): RecogResult {
            val recogResult = RecogResult()
            if (kbRecogResult == null || kbRecogResult.recogResultItemList == null) return recogResult

            // get recogResultItems
            val kbRecogResultItems = kbRecogResult.recogResultItemList
            val recogResultItems: ArrayList<RecogResultItem> = ArrayList<RecogResultItem>()
            for (item in kbRecogResultItems) {
                val resultItem = RecogResultItem()
                resultItem.setResult(item.result)
                resultItem.setSymbols(item.symbols)
                val matchItems: ArrayList<ResultMatchItem> = ArrayList<ResultMatchItem>()
                val kbResultMatchItems = item.matchItemList
                if (kbResultMatchItems != null) {
                    for (kbMatchItem in kbResultMatchItems) {
                        val resultMatchItem = ResultMatchItem()
                        resultMatchItem.setResultItem(kbMatchItem.resultItem)
                        resultMatchItem.setSymbolsItem(kbMatchItem.symbolsItem)
                        matchItems.add(resultMatchItem)
                    }
                }
                resultItem.setMatchItems(matchItems)
                recogResultItems.add(resultItem)
            }

            // get recogSyllables
            val kbSyllableResultItems = kbRecogResult.syllableResultItemList
            val recogSyllables = ArrayList<String>()
            if (kbSyllableResultItems != null) {
                for (item in kbSyllableResultItems) {
                    recogSyllables.add(item.syllableResultItem)
                }
            }
            recogResult.setRecogResultItems(recogResultItems)
            recogResult.setSyllables(recogSyllables)
            recogResult.setHasMore(kbRecogResult.bmore)
            return recogResult
        }

        fun getLocalVersionName(ctx: Context): String? {
            var localVersion = ""
            try {
                val packageInfo = ctx.applicationContext
                    .packageManager.getPackageInfo(ctx.packageName, 0)
                localVersion = packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return "V$localVersion"
        }
    }

}
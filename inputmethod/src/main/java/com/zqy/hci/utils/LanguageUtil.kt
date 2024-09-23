package com.zqy.hci.utils

import com.zqy.hci.bean.InputModeConst

/**
 * @Author:CWQ
 * @DATE:2022/1/25
 * @DESC:
 */
object LanguageUtil {

    private const val MALAY_FIX = "_malay_"
    private const val BAHASA_FIX = "_bahasa_"
    private const val ENGLISH_FIX = "en"
    private const val DAI_FIX = "_dai_"

    private const val RUSSIAN_FIX = "_russian_"
    private const val SPANISH_FIX = "_es_"
    private const val PORTUGUESE_FIX = "_por_"
    private const val ARAB_FIX = "_arab_"
    private const val FARSI_FIX = "_farsi_"
    private const val VIETNAM_FIX = "_vi_"
    private const val GERMAN_FIX = "_ger_"
    private const val FRENCH_FIX = "_fr_"
    private const val ITALIAN_FIX = "_ita_"
    private const val DUTCH_FIX = "_du_"
    private const val SWEDISH_FIX = "_sw_"
    private const val CZECH_FIX = "_cz_"
    private const val NORWEGIAN_FIX = "_no_"

    object KbRes {
        var NUM_PREFIX = "num_"
        var SYMBOL_PREFIX = "symbol_"

        var LAST_KEYBOARD_ID = "last_keyboard_id"

        var NUM_QWERTY_CN = "num_qwerty_cn"

        var NUM_T9 = "num_t9"

        //英语
        var QWERTY_EN = "qwerty_en"

        //俄语
        var QWERTY_RUSSIAN = "qwerty_russian"

        //西班牙语
        var QWERTY_SPANISH = "qwerty_spanish"

        //葡萄牙语
        var QWERTY_PORTUGUESE = "qwerty_portuguese"

        //阿拉伯语
        var QWERTY_ARAB = "qwerty_arab"

        //波斯语
        var QWERTY_FARSI = "qwerty_farsi"

        //马来西亚
        var QWERTY_MALAY = "qwerty_malay"

        //印尼
        var QWERTY_BAHASA = "qwerty_malay"

        //泰语
        var QWERTY_DAI = "qwerty_dai"

        //越南语键盘
        var QWERTY_VIETNAM = "qwerty_vietnam"

        //德语
        var QWERTY_GERMAN = "qwerty_german"

        //法语
        var QWERTY_FRENCH = "qwerty_french"

        //意大利语
        var QWERTY_ITALY = "qwerty_italy"

        //荷兰语
        var QWERTY_DUTCH = "qwerty_dutch"

        //瑞典语
        var QWERTY_SWEDISH = "qwerty_swedish"

        //捷克语
        var QWERTY_CZECH = "qwerty_czech"

        //挪威语
        var QWERTY_NORWAY = "qwerty_norway"

        //中文全键
        var QWERTY_CHINESE = "qwerty_cn"

        //中文手写
        var QWERTY_HWR_CN = "qwerty_hwr_cn"

        //英文手写
        var QWERTY_HWR_EN = "qwerty_hwr_en"

        //密码键盘
        var QWERTY_PSW = "qwerty_en_psw"


        var KB_NAME = arrayOf(
            QWERTY_EN,
            QWERTY_CHINESE,
            QWERTY_HWR_CN,
            QWERTY_HWR_EN,
            QWERTY_PSW
        )


        var SPACE_TEXT = arrayOf(
            arrayOf("English", "Selected"),
            arrayOf("Chinese", "选择"),
            arrayOf("Chinese", "选择"),
            arrayOf("English", "Selected"),
        )
    }


    fun notNeedCheckUpper(res_pre_fix: String): Boolean {
        return res_pre_fix == ARAB_FIX || res_pre_fix == FARSI_FIX || res_pre_fix == DAI_FIX
    }

    fun needRemoveRepeat(res_pre_fix: String): Boolean {
        return res_pre_fix == DAI_FIX || res_pre_fix == FARSI_FIX
    }

    private val TAG = LanguageUtil::class.java.simpleName
    private const val LOCAL_LANGUAGE = "localLanguage"


    fun getRexPreFix(mainInputMethodMode: Int): String {
        var resPreFix = ""
        when (mainInputMethodMode) {
            InputModeConst.INPUT_ENGLISH -> resPreFix = ENGLISH_FIX
        }
        return resPreFix
    }

    private fun isCustomLanguage(language: String): Boolean {
        return (language.contains("ma")
                || language.contains("ba")
                || language.contains("da"))
    }
}
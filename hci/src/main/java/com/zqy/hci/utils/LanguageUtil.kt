package com.zqy.hci.utils

import com.zqy.hci.bean.InputModeConst

/**
 * @Author:CWQ
 * @DATE:2022/1/25
 * @DESC:
 */
object LanguageUtil {

    //切换语言的adb命令adb shell am broadcast -a com.sinovoice.hcicloudinputvehicle --es lan "ru"
    private const val MALAY_FIX = "_malay_"
    private const val BAHASA_FIX = "_bahasa_"
    private const val ENGLISH_FIX = "_en_"
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


        var KB_NAME = arrayOf(
            QWERTY_EN,
            QWERTY_RUSSIAN,
            QWERTY_SPANISH,
            QWERTY_PORTUGUESE,
            QWERTY_ARAB,
            QWERTY_FARSI,
            QWERTY_MALAY,
            QWERTY_BAHASA,
            QWERTY_DAI,
            QWERTY_VIETNAM,
            QWERTY_GERMAN,
            QWERTY_FRENCH,
            QWERTY_ITALY,
            QWERTY_DUTCH,
            QWERTY_SWEDISH,
            QWERTY_CZECH,
            QWERTY_NORWAY,
            QWERTY_CHINESE,
            QWERTY_HWR_CN,
            QWERTY_HWR_EN
        )


        var SPACE_TEXT = arrayOf(
            arrayOf("English", "Selected"),
            arrayOf("Русский язык", "Выбрать"),
            arrayOf("Español", "Seleccionado"),
            arrayOf("Português", "Escolher"),
            arrayOf("العربية ", "المحدد"),
            arrayOf("فارسی", "انتخاب شد."),
            arrayOf("Bahasa Melayu", "Dipilih"),
            arrayOf("Bahasa Indonesia", "Terpilih"),
            arrayOf("ภาษาไทย", "เลือก"),
            arrayOf("Tiếng Việt", "Chọn"),
            arrayOf("Deutsch", "Deutsch"),
            arrayOf("Français", "Français"),
            arrayOf("Italiano", "Italiano"),
            arrayOf("Nederlands", "Nederlands"),
            arrayOf("Svensk", "Svensk"),
            arrayOf("Čeština", "Čeština"),
            arrayOf("Norsk", "Norsk"),
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
            InputModeConst.INPUT_SPANISH -> resPreFix = SPANISH_FIX
            InputModeConst.INPUT_PORTUGUESE -> resPreFix = PORTUGUESE_FIX
            InputModeConst.INPUT_RUSSIAN -> resPreFix = RUSSIAN_FIX
            InputModeConst.INPUT_ARABIC -> resPreFix = ARAB_FIX
            InputModeConst.INPUT_FARSI -> resPreFix = FARSI_FIX
            InputModeConst.INPUT_MALAY -> resPreFix = MALAY_FIX
            InputModeConst.INPUT_BAHASA -> resPreFix = BAHASA_FIX
            InputModeConst.INPUT_DAI -> resPreFix = DAI_FIX
            InputModeConst.INPUT_VIETNAM -> resPreFix = VIETNAM_FIX
            InputModeConst.INPUT_GERMAN -> resPreFix = GERMAN_FIX
            InputModeConst.INPUT_FRENCH -> resPreFix = FRENCH_FIX
            InputModeConst.INPUT_ITALIAN -> resPreFix = ITALIAN_FIX
            InputModeConst.INPUT_DUTCH -> resPreFix = DUTCH_FIX
            InputModeConst.INPUT_SWEDISH -> resPreFix = SWEDISH_FIX
            InputModeConst.INPUT_CZECH -> resPreFix = CZECH_FIX
            InputModeConst.INPUT_NORWEGIAN -> resPreFix = NORWEGIAN_FIX
        }
        return resPreFix
    }

    private fun isCustomLanguage(language: String): Boolean {
        return (language.contains("ma")
                || language.contains("ba")
                || language.contains("da"))
    }
}
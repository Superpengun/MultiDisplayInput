package com.zqy.hci.utils

import android.util.Log
import okhttp3.internal.toHexString


/**
 * @Author:CWQ
 * @DATE:2023/1/5
 * @DESC:
 */
object EmojiUtils {
    private const val TAG = "EmojiUtils"

    /**
     * 黄脸表情
     */
    private val faceEmojiList = arrayListOf(
        "\ud83d\ude01",
        "\ud83d\ude02",
        "\ud83d\ude03",
        "\ud83d\ude04",
        "\ud83d\udc7f",
        "\ud83d\ude09",
        "\ud83d\ude0a",
        "\u263a\ufe0f",
        "\ud83d\ude0c",
        "\ud83d\ude0d",
        "\ud83d\ude0f",
        "\ud83d\ude12",
        "\ud83d\ude13",
        "\ud83d\ude14",
        "\ud83d\ude16",
        "\ud83d\ude18",
        "\ud83d\ude1a",
        "\ud83d\ude1c",
        "\ud83d\ude1d",
        "\ud83d\ude1e",
        "\ud83d\ude20",
        "\ud83d\ude21",
        "\ud83d\ude22",
        "\ud83d\ude23",
        "\ud83d\ude25",
        "\ud83d\ude28",
        "\ud83d\ude2a",
        "\ud83d\ude2d",
        "\ud83d\ude30",
        "\ud83d\ude31",
        "\ud83d\ude32",
        "\ud83d\ude33",
        "\ud83d\ude37",
        "\ud83d\ude43",
        "\ud83d\ude0b",
        "\ud83d\ude17",
        "\ud83d\ude1b",
        "\ud83e\udd11",
        "\ud83e\udd13",
        "\ud83d\ude0e",
        "\ud83e\udd17",
        "\ud83d\ude44",
        "\ud83e\udd14",
        "\ud83d\ude29",
        "\ud83d\ude24",
        "\ud83e\udd10",
        "\ud83e\udd12",
        "\ud83d\ude34",
        "\ud83d\ude00",
        "\ud83d\ude06",
        "\ud83d\ude05",
        "\ud83d\ude07",
        "\ud83d\ude42",
        "\ud83d\ude19",
        "\ud83d\ude1f",
        "\ud83d\ude15",
        "\ud83d\ude41",
        "\u2639\ufe0f",
        "\ud83d\ude2b",
        "\ud83d\ude36",
        "\ud83d\ude10",
        "\ud83d\ude11",
        "\ud83d\ude2f",
        "\ud83d\ude26",
        "\ud83d\ude27",
        "\ud83d\ude2e",
        "\ud83d\ude35",
        "\ud83d\ude2c",
        "\ud83e\udd15",
        "\ud83d\ude08",
        "\ud83d\udc7b",
        "\ud83e\udd7a",
        "\ud83e\udd74",
        "\ud83e\udd23",
        "\ud83e\udd70",
        "\ud83e\udd29",
        "\ud83e\udd24",
        "\ud83e\udd2b",
        "\ud83e\udd2a",
        "\ud83e\uddd0",
        "\ud83e\udd2c",
        "\ud83e\udd27",
        "\ud83e\udd2d",
        "\ud83e\udd20",
        "\ud83e\udd2f",
        "\ud83e\udd25",
        "\ud83e\udd73",
        "\ud83e\udd28",
        "\ud83e\udd22",
        "\ud83e\udd21",
        "\ud83e\udd2e",
        "\ud83e\udd75",
        "\ud83e\udd76",
        "\ud83d\udca9",
        "\u2620\ufe0f",
        "\ud83d\udc80",
        "\ud83d\udc7d",
        "\ud83d\udc7e",
        "\ud83d\udc7a",
        "\ud83d\udc79",
        "\ud83e\udd16"
    )

    /**
     * 颜文字
     */
    private val symbolList: ArrayList<String> = arrayListOf(
        "(눈_눈)",
        "⊙_⊙",
        "( ꒪ͧ⌓꒪ͧ)",
        "(๑•̌.•̑๑)ˀ",
        "-_-||",
        "┐（─__─）┌",
        "ヽ（・＿・；)ノ",
        "ヽ(≧Д≦)ノ",
        "⊙▽⊙",
        "(●°u°●)\u200B 」",
        "(>﹏<)",
        "(〜￣▽￣)〜",
        "(￣o￣) . z Z",
        "(ﾟoﾟ;",
        ",,Ծ^Ծ,,",
        "(✪▽✪)",
        "→_→",
        "٩(๑^o^๑)۶",
        "(≧∇≦)/",
        "(ﾟoﾟ;"
    )

    /**
     * 获取名称列表
     * @param type 0黄脸 1颜文字
     * @return ArrayList<String>
     */
    fun getEmojiList(type: Int): ArrayList<String> {
        return if (type == 1) {
            symbolList
        } else {
            faceEmojiList
        }
    }


    /**
     * 把中文，英文，数字字符串及emoji表情转换为十六进制Unicode编码字符串
     * @param s String
     * @return String
     */
    fun stringToUnicode1(s: String): String {
        var result = ""
        for (i in s.indices) {
            val ch: Int = s[i].code
            if (ch > 255) {
                result += "\\u" + ch.toHexString()
            } else {
                result += "\\" + ch.toHexString()
            }
        }
        return result
    }


    fun spliteStr() {
        val string =
            "ud83d\\ude01\\ud83d\\ude02\\ud83d\\ude03\\ud83d\\ude04\\ud83d\\udc7f\\ud83d\\ude09\\ud83d\\ude0a\\u263a\\ufe0f\\ud83d\\ude0c\\ud83d\\ude0d\\ud83d\\ude0f\\ud83d\\ude12\\ud83d\\ude13\\ud83d\\ude14\\ud83d\\ude16\\ud83d\\ude18\\ud83d\\ude1a\\ud83d\\ude1c\\ud83d\\ude1d\\ud83d\\ude1e\\ud83d\\ude20\\ud83d\\ude21\\ud83d\\ude22\\ud83d\\ude23\\ud83d\\ude25\\ud83d\\ude28\\ud83d\\ude2a\\ud83d\\ude2d\\ud83d\\ude30\\ud83d\\ude31\\ud83d\\ude32\\ud83d\\ude33\\ud83d\\ude37\\ud83d\\ude43\\ud83d\\ude0b\\ud83d\\ude17\\ud83d\\ude1b\\ud83e\\udd11\\ud83e\\udd13\\ud83d\\ude0e\\ud83e\\udd17\\ud83d\\ude44\\ud83e\\udd14\\ud83d\\ude29\\ud83d\\ude24\\ud83e\\udd10\\ud83e\\udd12\\ud83d\\ude34\\ud83d\\ude00\\ud83d\\ude06\\ud83d\\ude05\\ud83d\\ude07\\ud83d\\ude42\\ud83d\\ude19\\ud83d\\ude1f\\ud83d\\ude15\\ud83d\\ude41\\u2639\\ufe0f\\ud83d\\ude2b\\ud83d\\ude36\\ud83d\\ude10\\ud83d\\ude11\\ud83d\\ude2f\\ud83d\\ude26\\ud83d\\ude27\\ud83d\\ude2e\\ud83d\\ude35\\ud83d\\ude2c\\ud83e\\udd15\\ud83d\\ude08\\ud83d\\udc7b\\ud83e\\udd7a\\ud83e\\udd74\\ud83e\\udd23\\ud83e\\udd70\\ud83e\\udd29\\ud83e\\udd24\\ud83e\\udd2b\\ud83e\\udd2a\\ud83e\\uddd0\\ud83e\\udd2c\\ud83e\\udd27\\ud83e\\udd2d\\ud83e\\udd20\\ud83e\\udd2f\\ud83e\\udd25\\ud83e\\udd73\\ud83e\\udd28\\ud83e\\udd22\\ud83e\\udd21\\ud83e\\udd2e\\ud83e\\udd75\\ud83e\\udd76\\ud83d\\udca9\\u2620\\ufe0f\\ud83d\\udc80\\ud83d\\udc7d\\ud83d\\udc7e\\ud83d\\udc7a\\ud83d\\udc79\\ud83e\\udd16"
        val stringBuffer = StringBuffer()
        val split = string.split("\\")
        var count = 0
        var tempStr = ""
        split.forEach {
            count++
            if (count == 1) {
                tempStr = it
            } else {
                count = 0
                val one = "\\" + tempStr + "\\" + it
                stringBuffer.append("\"" + one + "\",")
            }
        }
        Log.d(TAG, "spliteStr: ${stringBuffer}")
    }
}
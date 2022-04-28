package com.zqy.sdk

/**
 * @author:zhenqiyuan
 * @data:2022/1/14
 * @描述：
 * @package:
 */
class Constants {
    val HCICLOUD_SYS_INIT_SUCCESS_AUTH_SUCCESS = 200

    /**
     * 灵云系统相关常量
     */
    object HciCloudSysConstants {
        //本地账号
        //        public static final String APP_KEY = "6d5d5457";
        //        public static final String DEVELOPER_KEY = "ea5b66761bd4017ae541c3112070944d";
        const val AUTH_HINT_TIME = 1 * 24 * 3600 * 1000L //授权即将到期提示时间，此处为1天
        //        * 0:检查授权成功
        //        * -1:检查授权成功，但即将到期
        //        * -2:检查授权失败
        /**
         * 授权成功
         */
        const val SYS_AUTH_SUCCESS = 0

        /**
         * 授权成功，但授权即将到期
         */
        const val SYS_AUTH_HINT = -1

        /**
         * 授权失败
         */
        const val SYS_AUTH_FAILED = -2

        /**
         * 灵云系统初始化成功并且授权成功
         */
        const val SYS_INIT_SUCCESS_AUTH_SUCCESS = 0

        /**
         * 灵云系统初始化成功，授权即将到期
         */
        const val SYS_INIT_SUCCESS_AUTH_HINT = -1

        /**
         * 灵云系统初始化成功但是授权失败
         */
        const val SYS_INIT_SUCCESS_AUTH_FAILED = -2

        /**
         * 灵云系统初始化失败
         */
        const val SYS_INIT_FAILED = -3

        /**
         * 拼音26键输入模式
         */
        const val INPUT_MODE_PINYIN = 0

        /**
         * 英文26键输入模式
         */
        const val INPUT_MODE_EN = 1

        /**
         * 手写输入模式
         */
        const val INPUT_MODE_HWR = 2
    }

    /**
     * KB能力相关常量
     */
    object KBConstant {
        /**
         * Kb候选字结果页大小
         */
        const val QUERY_COUNT = 20

        /**
         * Kb能力
         */
        const val KB_CAPKEY = "kb.local.recog"

        /**
         * Kb文件标示
         */
        const val FILE_FLAG = "android_so"

        /**
         * Kb资源前缀
         */
        const val RES_PROFIX = "resPrefix"

        /**
         * Kb中文资源前缀
         */
        const val RES_PREFIX_CH = "_cn_"

        /**
         * Kb英文资源前缀
         */
        const val RES_PREFIX_EN = "_en_"

        /**
         * 拼音（中文）输入模式
         */
        const val INPUT_MODE_PINYIN = "pinyin"

        /**
         * 外文（英语小写）输入模式
         */
        const val INPUT_MODE_LOWER = "lower"

        /**
         * 外文（默认）输入模式
         */
        const val INPUT_MODE_DEFAULT = "default"

        /**
         * 外文（英语小写）输入模式
         */
        const val INPUT_MODE_UPPER = "upper"

        /**
         * 外文（英语小写）输入模式
         */
        const val INPUT_MODE_FIRSTUPPER = "firstupper"

        /**
         * 拼音键盘容错水平——不容错
         */
        const val INPUT_TOLERANT_LEVEL_NONE = "none"

        /**
         * 拼音键盘容错水平——一般
         */
        const val INPUT_TOLERANT_LEVEL_LOW = "low"

        /**
         * 拼音键盘容错水平——高
         */
        const val INPUT_TOLERANT_LEVEL_HIGH = "high"

        /**
         * 中文输入模式: 拼音
         */
        const val KB_INPUT_MODE_PINYIN = 1

        /**
         * 外语输入模式: 小写
         */
        const val KB_INPUT_MODE_LOWER = 6

        /**
         * 候选数组的最大候选数量
         */
        const val KB_MAX_CAND_COUNT = 31

        /**
         * 按键值最小值
         */
        const val KB_KEYMAP_MIN_KEY_VALUE = 0xE000

        /**
         * KB能力模糊音打开
         */
        const val KB_FUZZY_ON = 1

        /**
         * KB能力模糊音关闭
         */
        const val KB_FUZZY_OFF = 0
    }
    object HandlerMsgConstants {
        /**
         * 灵云系统初始化成功并且授权成功
         */
        const val HCICLOUD_SYS_INIT_SUCCESS_AUTH_SUCCESS = 200

        /**
         * 灵云系统初始化成功，授权即将到期
         */
        const val HCICLOUD_SYS_INIT_SUCCESS_AUTH_HINT = 201

        /**
         * 灵云系统初始化成功但是授权失败
         */
        const val HCICLOUD_SYS_INIT_SUCCESS_AUTH_FAILED = 202

        /**
         * 灵云系统初始化失败
         */
        const val HCICLOUD_SYS_INIT_FAILED = 203

        /**
         * KB能力初始化成功
         */
        const val HCICLOUD_KB_INIT_SUCCESS = 100

        /**
         * KB能力初始化失败
         */
        const val HCICLOUD_KB_INIT_FAILED = 101

        /**
         * HWR能力初始化成功
         */
        const val HCICLOUD_HWR_INIT_SUCCESS = 400

        /**
         * HWR能力初始化失败
         */
        const val HCICLOUD_HWR_INIT_FAILED = 401
        const val HCICLOUD_SYS_INIT_RESULT = 12121

        /**
         * 获取候选词结束，需要更新候选区
         */
        const val UPDATE_CANDAIDATE = 301

        /**
         * 获取联想词结束，需要更新联想词
         */
        const val UPDATE_ASSOCIATE = 302

        /**
         * 需要更新ComposingView
         */
        const val UPDATE_COMPOSING = 303

        /**
         * 需要更新音节
         */
        const val UPDATE_SYLLABLE = 304

        /**
         * 显示toast通知
         */
        const val SHOW_TOAST = 456
    }
}
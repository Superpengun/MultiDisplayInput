package com.zqy.hci.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zqy.hci.R
import com.zqy.hci.bean.InputModeConst
import com.zqy.hci.listener.OnCandidateActionListener
import com.zqy.hci.listener.OnKeyboardActionListener
import com.zqy.hci.service.Settings
import com.zqy.hci.theme.ThemeManager
import com.zqy.hci.widget.CandidateView
import com.zqy.hci.widget.Keyboard
import com.zqy.hci.widget.KeyboardView
import com.zqy.hci.widget.MoreCandidateView

/**
 * @Author:CWQ
 * @DATE:2022/1/17
 * @DESC:
 */
class TestCandidateActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = TestCandidateActivity::class.java.simpleName

    private val mCandidateData =
        arrayListOf(
            "哈",
            "哈哈",
            "哈哈哈",
            "嘿",
            "嘿嘿嘿嘿嘿嘿",
            "嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿",
            "呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵",
            "呵呵",
            "呵呵呵",
            "嘻",
            "嘻嘻",
            "嘻嘻嘻",
            "嘎",
            "嘎嘎",
            "嘎嘎嘎",
            "哼",
            "哼哼",
            "哼哼哼",
            "哈",
            "哈哈",
            "哈哈哈",
            "嘿",
            "嘿嘿嘿嘿嘿嘿",
            "嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿",
            "呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵",
            "呵呵",
            "呵呵呵",
        )

    private val mAssData =
        arrayListOf(
            "ha",
            "haha",
            "hahaha",
            "hei",
            "heihei",
            "heiheihei",
            "he",
            "hehe",
            "hehehe",
            "xi",
            "xixi",
            "xixixi",
            "ga",
            "gaga",
            "gagaga",
            "heng",
            "hengheng",
            "henghengheng"
        )

    private lateinit var mCandidateView: CandidateView
    private lateinit var mMoreCandidateView: MoreCandidateView
    private lateinit var mKeyboardView: KeyboardView
    private var hasSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        mCandidateView = findViewById(R.id.candidate_view)
        mMoreCandidateView = findViewById(R.id.more_candidate_view)
        mKeyboardView = findViewById(R.id.keyboard_view)

        mCandidateView.setOnCandidateActionListener(object : OnCandidateActionListener {
            override fun onCandidateSelected(index: Int, word: String) {
                Log.e(TAG, "onSelected index:$index,word:$word")
                if (!hasSelected) {
                    mCandidateView.setAssociateData(mAssData)
                    hasSelected = true
                } else {
                    mCandidateView.setCandidateData(arrayListOf())
                    hasSelected = false
                }
            }

            override fun getMoreList() {
                Log.e(TAG, "getMoreList")
            }

            override fun onMore() {
                Log.e(TAG, "getMore")
            }

            override fun onClose() {
                Log.e(TAG, "onClose")
            }

            override fun onClear() {
                Log.e(TAG, "onClear")
                mCandidateView.setAssociateData(arrayListOf())
            }

            override fun onBack() {

            }
        })

        mMoreCandidateView.setOnCandidateActionListener(object : OnCandidateActionListener {
            override fun onCandidateSelected(index: Int, word: String) {
                Log.e(TAG, "more onCandidateSelected index:$index,word:$word")
            }

            override fun getMoreList() {
                Log.e(TAG, "more getMoreList")
            }

            override fun onMore() {
                Log.e(TAG, "more getMore")
            }

            override fun onClose() {
                Log.e(TAG, "more onClose")
            }

            override fun onClear() {
                Log.e(TAG, "more onClear")
            }

            override fun onBack() {

            }
        })



        findViewById<TextView>(R.id.tv_update).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_set_more_candidate).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_set_theme).setOnClickListener(this)

        val themeManager = ThemeManager(this)
        themeManager.addThemeAbleView(mCandidateView)
        themeManager.addThemeAbleView(mMoreCandidateView)
        themeManager.loadTheme()

        mCandidateView.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_update -> {
                mCandidateView.setCandidateData(mCandidateData)
            }
            R.id.tv_set_more_candidate -> {
                mMoreCandidateView.visibility = View.VISIBLE
                mMoreCandidateView.setCandidateData(mCandidateData)
            }
            R.id.tv_set_theme -> {
//                val themeManager = ThemeManager(this)
//                themeManager.addThemeAbleView(mCandidateView)
//                themeManager.addThemeAbleView(mMoreCandidateView)
//                themeManager.addThemeAbleView(mKeyboardView)
//                themeManager.loadTheme()


                Settings.init(this)
                Settings.instance.loadSettings(this)
                val themeManager = ThemeManager(this)
                themeManager.addThemeAbleView(mKeyboardView)
                themeManager.addThemeAbleView(mCandidateView)
                themeManager.addThemeAbleView(mMoreCandidateView)
                themeManager.loadTheme()

                mKeyboardView.setOnKeyboardActionListener(object : OnKeyboardActionListener {
                    override fun onPress(primaryCode: Int) {
                        Log.e(TAG, "onPress,$primaryCode")
                    }

                    override fun onRelease(primaryCode: Int) {
                        Log.e(TAG, "onRelease,$primaryCode")
                    }

                    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
                        Log.e(TAG, "onKey,primaryCode:$primaryCode,keyCodes:$keyCodes")
                    }

                    override fun onText(text: CharSequence?) {
                        Log.e(TAG, "onText,$text")
                    }

                    override fun swipeLeft() {
                        Log.e(TAG, "swipeLeft")
                    }

                    override fun swipeRight() {
                        Log.e(TAG, "swipeRight")
                    }

                    override fun swipeDown() {
                        Log.e(TAG, "swipeDown")
                    }

                    override fun swipeUp() {
                        Log.e(TAG, "swipeUp")
                    }

                    override fun onLongPress(text: CharSequence?): Boolean {
                        Log.e(TAG, "onLongPress,$text")
                        return true
                    }

                })

                val keyboard = Keyboard(this, R.xml.qwerty_en, InputModeConst.INPUT_ENGLISH)
                keyboard.setMkbName("qwerty_en")
                mKeyboardView.setKeyboard(keyboard)
            }
        }
    }
}
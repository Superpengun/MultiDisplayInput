package com.zqy.multidisplayinput.window

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import com.zqy.hci.R
import com.zqy.hci.bean.InputModeConst
import com.zqy.hci.bean.KeyboardId
import com.zqy.hci.listener.OnCandidateActionListener
import com.zqy.hci.listener.OnKeyboardActionListener
import com.zqy.hci.theme.ThemeManager
import com.zqy.hci.utils.LanguageUtil
import com.zqy.hci.widget.*
import com.zqy.multidisplayinput.editor.ImeEditor

/**
 * @author:zhenqiyuan
 * @data:2022/5/7
 * @描述：
 * @package:com.zqy.multidisplayinput.window
 */
class Switcher {
    private val TAG = "Switcher"
    private lateinit var mContext: Context
    lateinit var themeManager: ThemeManager
    private var currentInputView: ConstraintLayout? = null
    private var mCandidateView: CandidateView? = null
    private var mMoreCandidateView: MoreCandidateView? = null
    private var mKeyboardView: KeyboardView? = null
    private var mCurrentKbd: KeyboardId? = null

    private var mSelectDialog: Dialog? = null
    private var mCurrentSelectKB = 0
    private var mKbLayout = 0//键盘模式 0主键盘 1数字键盘 2字符键盘

    //输入模式 如文本、数字、url、密码等
    private var mTextMode = Switcher.MODE_TEXT
    private var showSpaceIcon = false
    lateinit var mImeEditor : ImeEditor

    companion object{
        const val MODE_TEXT = 1
        const val MODE_NUMBER = 3
        const val MODE_URL = 4
        const val MODE_EMAIL = 5
        const val MODE_WEB = 6
        const val MODE_PWD = 7

        private const val STATE_LANGUAGE = 0
        private const val STATE_SELECTED = 1
        private const val STATE_ICON = 2
    }

    fun init(context: Context) {
        mImeEditor = ImeEditor(context)
        mContext = context
        themeManager = ThemeManager(context)
    }

    fun createInputView(layoutInflater: LayoutInflater): View {
        if (currentInputView != null) {
            clear()
            currentInputView = null
        }
        currentInputView = initInputLayout(layoutInflater)
        return currentInputView!!
    }

    fun setUIListener(l: OnCandidateActionListener, listener: OnKeyboardActionListener) {
        mKeyboardView?.setOnKeyboardActionListener(listener)
        mCandidateView?.setOnCandidateActionListener(l)
    }

    /**
     * 加载键盘布局
     * @param layoutInflater LayoutInflater
     * @return ConstraintLayout?
     */
    private fun initInputLayout(layoutInflater: LayoutInflater): ConstraintLayout? {
        val inputContainer = layoutInflater.inflate(R.layout.layout_inputview, null)
        currentInputView = inputContainer as ConstraintLayout?
        mCandidateView = currentInputView?.findViewById(R.id.candidate_view)
        mKeyboardView = currentInputView?.findViewById(R.id.keyboard_view)
        mMoreCandidateView = currentInputView?.findViewById(R.id.more_candidate_view)
        themeManager.addThemeAbleView(mKeyboardView!!)
        themeManager.addThemeAbleView(mCandidateView!!)
        themeManager.addThemeAbleView(mMoreCandidateView!!)
        return inputContainer
    }


    fun loadTheme() {
        themeManager.loadTheme()
    }

    /**
     * 处理键盘拉起时系统输入模式
     * @param editorInfo EditorInfo?
     */
    fun handleEditorInfo(editorInfo: EditorInfo?) {
        var textMode = Switcher.MODE_TEXT
        if (editorInfo != null) {
            val type = editorInfo.inputType and EditorInfo.TYPE_MASK_CLASS
            if (type == EditorInfo.TYPE_CLASS_NUMBER || type == EditorInfo.TYPE_CLASS_DATETIME || type == EditorInfo.TYPE_CLASS_PHONE) {
                textMode = Switcher.MODE_NUMBER
            } else if (type == EditorInfo.TYPE_CLASS_TEXT) {
                textMode = when (editorInfo.inputType and EditorInfo.TYPE_MASK_VARIATION) {
                    EditorInfo.TYPE_TEXT_VARIATION_URI -> Switcher.MODE_URL
                    EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> Switcher.MODE_EMAIL
                    EditorInfo.TYPE_TEXT_VARIATION_PASSWORD, EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD -> Switcher.MODE_PWD
                    else -> Switcher.MODE_TEXT
                }
            }
        }
        mTextMode = textMode
        loadKeyboardId()
        switchKbByMode()
        mCandidateView?.setCandidateData(arrayListOf())
    }

    /**
     * 展示输入语言选择框
     * @param view 要绑定dialog的window中任意view
     */
    fun showChooseLanOption() {
        mSelectDialog = DialogHelper.getInputSelectDialog(
            mContext, mCandidateView!!.windowToken, mCurrentSelectKB
        ) { dialog, which ->
            dialog.dismiss()
            if (which != mCurrentSelectKB) {
                mCurrentSelectKB = which
                loadKeyboardId()
                switchKb()
            }
        }
        mSelectDialog?.show()
    }


    private fun switchKbByMode() {
        val kbName = if (mTextMode == Switcher.MODE_NUMBER) {
            mKbLayout = 1
            mCurrentKbd!!.numKbName
        } else {
            mKbLayout = 0
            mCurrentKbd!!.kbName
        }
        val keyboard: Keyboard = getKeyboard(kbName, mCurrentKbd!!.keyboardMode)
        setKeyboard(keyboard)
    }


    fun switchKb() {
        mKbLayout = 0
        val keyboard: Keyboard = getKeyboard(mCurrentKbd!!.kbName, mCurrentKbd!!.keyboardMode)
        setKeyboard(keyboard)
    }

    fun switchNumKb() {
        mKbLayout = 1
        val keyboard: Keyboard = getKeyboard(mCurrentKbd!!.numKbName, mCurrentKbd!!.keyboardMode)
        setKeyboard(keyboard)
    }

    fun switchSymbolKb() {
        mKbLayout = 2
        val keyboard: Keyboard = getKeyboard(mCurrentKbd!!.symbolKbName, mCurrentKbd!!.keyboardMode)
        setKeyboard(keyboard)
    }


    /**
     * 展示更多候选区
     * @param listener OnCandidateActionListener
     */
    fun showMoreCandidate(listener: OnCandidateActionListener) {
        mMoreCandidateView?.show(listener)
        mMoreCandidateView?.setCandidateData(
            mCandidateView!!.getData(),
            mCandidateView!!.isRTLMode()
        )
    }


    fun onFinishInputView() {
        checkAndHideDialog()
        if (mMoreCandidateView != null && mMoreCandidateView?.visibility == View.VISIBLE) {
            mMoreCandidateView?.hideAndScroll2Top()
        }
    }


    /**
     * 清理存在的数据，回收资源
     */
    fun clear() {

    }

    fun setShifted() {
        mKeyboardView?.setShifted(!mKeyboardView!!.isShifted())
    }

    fun lockShift() {
        mKeyboardView?.lockShift()
    }

    fun getShiftState(): Int {
        if (mKeyboardView != null) {
            return mKeyboardView!!.getShiftMode()
        } else {
            return 0
        }
    }

    fun isShifted() = mKeyboardView!!.isShifted()

    fun isLockShifted() = mKeyboardView!!.isLockShift()

    fun setCandidateData(data: ArrayList<String>) {
        Log.d(TAG,"setCandidateData,$data")
        mCandidateView?.setCandidateData(data)
        updateMoreCandidateViewData()
        if (!data.isNullOrEmpty()) {
            showSpaceIcon = true
        } else {
            updateSpace(Switcher.STATE_ICON)
        }
    }

    /**
     * 处理更多候选区返回动作
     */
    fun handleOnBack() {
        mMoreCandidateView?.hideAndScroll2Top()
    }


    private fun updateSpace(state: Int) {
        if (state == Switcher.STATE_ICON) {
            if (showSpaceIcon) {
                mKeyboardView!!.showSpaceIcon()
            }
            return
        }

        //密码键盘
        if (mCurrentKbd!!.inputMode == InputModeConst.INPUT_DEFAULT) {
            mKeyboardView!!.setSpaceKeyText("English")
            return
        }
        val spaceText = LanguageUtil.KbRes.SPACE_TEXT[mCurrentSelectKB]
        if (spaceText.isNotEmpty()) {
            mKeyboardView!!.setSpaceKeyText(spaceText[state])
        }
    }


    private fun loadKeyboardId() {
        mCurrentKbd = when (mTextMode) {
            Switcher.MODE_WEB, Switcher.MODE_URL, Switcher.MODE_PWD, Switcher.MODE_EMAIL -> {
                mImeEditor.inputMethodChange(InputModeConst.INPUT_DEFAULT)
                KeyboardId(
                    "qwerty_en_psw",
                    "num_qwerty_en_psw",
                    "symbol_qwerty_en_psw",
                    InputModeConst.INPUT_DEFAULT
                )
            }
            else -> {
                mImeEditor.inputMethodChange(mCurrentSelectKB)
                val kbName = LanguageUtil.KbRes.KB_NAME[mCurrentSelectKB]
                KeyboardId(
                    kbName,
                    LanguageUtil.KbRes.NUM_PREFIX + kbName,
                    LanguageUtil.KbRes.SYMBOL_PREFIX + kbName,
                    mCurrentSelectKB
                )
            }
        }
    }


    private fun setKeyboard(keyboard: Keyboard) {
        mKeyboardView!!.setKeyboard(keyboard)
        updateSpace(if (mKbLayout == 0) Switcher.STATE_LANGUAGE else Switcher.STATE_ICON)
        mKeyboardView!!.sendUpdateSpaceMsg()
        notifyCandidateLayoutModeChange(mCurrentKbd!!)

    }

    private fun notifyCandidateLayoutModeChange(id: KeyboardId) {
        if (id.kbName.contains("arab") || id.kbName.contains("farsi")) {
            mCandidateView!!.setRTLMode(true)
        } else {
            mCandidateView!!.setRTLMode(false)
        }
    }


    private fun checkAndHideDialog() {
        if (mSelectDialog != null && mSelectDialog!!.isShowing) {
            mSelectDialog?.dismiss()
        }
    }


    private fun getKeyboard(kbName: String, keyboardMode: Int): Keyboard {
        val keyboardRes: Int =
            mContext.resources.getIdentifier(kbName, "xml", mContext.packageName)
        val keyboard = Keyboard(mContext, keyboardRes, keyboardMode)
        keyboard.setMkbName(kbName)
        return keyboard
    }

    private fun updateMoreCandidateViewData(){
        mMoreCandidateView?.setCandidateData(
            mCandidateView!!.getData(),
            mCandidateView!!.isRTLMode()
        )
    }
}
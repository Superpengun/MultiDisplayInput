package com.zqy.multidisplayinput.window

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import com.zqy.hci.R
import com.zqy.hci.bean.KeyboardId
import com.zqy.hci.handwrite.OnStrokeActionListener
import com.zqy.hci.listener.*
import com.zqy.hci.theme.ThemeManager
import com.zqy.hci.theme.ThemeState
import com.zqy.hci.ui.EmojiPopupWindow
import com.zqy.hci.ui.NewPlatePop
import com.zqy.hci.ui.VoicePopupWindow
import com.zqy.hci.ui.VoiceViewManager
import com.zqy.hci.utils.LanguageUtil
import com.zqy.hci.utils.LanguageUtil.KbRes.QWERTY_EN
import com.zqy.hci.utils.LanguageUtil.KbRes.QWERTY_HWR_CN
import com.zqy.hci.utils.LanguageUtil.KbRes.QWERTY_HWR_EN
import com.zqy.hci.widget.*
import com.zqy.multidisplayinput.editor.ImeEditor
import kotlinx.coroutines.*

/**
 * @author:zhenqiyuan
 * @data:2022/5/7
 * @描述：
 * @package:com.zqy.multidisplayinput.window
 */
class Switcher {
    private val TAG = "Switcher"
    private lateinit var mContext: Context
    private lateinit var mDialog: Dialog
    lateinit var themeManager: ThemeManager
    private var currentInputView: ConstraintLayout? = null
    private var mCandidateView: CandidateView? = null
    private var mMoreCandidateView: MoreCandidateView? = null
    private var mKeyboardView: KeyboardView? = null
    private var mNum9KeyboardView: KeyboardView? = null
    private var mComposingView: ComposingView? = null
    private var mCurrentKbd: KeyboardId? = null
    private var mStrokeView: StrokeView? = null
    private var mSymbolView: SymbolView? = null
    private var mSuperPenguinEmojiContainer: SuperPenguinEmojiViewContainer? = null
    private var mSelectDialog: Dialog? = null
    private lateinit var mNewPlatePop: NewPlatePop
    private lateinit var mVoiceViewManager: VoiceViewManager
    private var mCurrentSelectKB = 1
    private var mLastKB = 1
    private var mLastHWR = 2
    private var mKbLayout = 0//键盘模式 0主键盘 1数字键盘 2字符键盘

    //输入模式 如文本、数字、url、密码等
    private var mTextMode = MODE_TEXT
    private var mActionMode = 1
    private var showSpaceIcon = false
    lateinit var mImeEditor: ImeEditor
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    companion object {
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

    fun init(context: Context, window: Dialog) {
        mImeEditor = ImeEditor()
        mContext = context
        themeManager = ThemeManager(context)
        mDialog = window
    }

    fun createInputView(layoutInflater: LayoutInflater): View {
        if (currentInputView != null) {
            clear()
            currentInputView = null
        }
        currentInputView = initInputLayout(layoutInflater)
        return currentInputView!!
    }

    fun setUIListener(
        l: OnCandidateActionListener,
        listener: OnKeyboardActionListener,
        ll: OnStrokeActionListener,
        lll: OnNumKeyboardActionListener,
        llll: OnSymbolActionListener,
        lllll: OnEmojiActionListener,
        llllll: OnNewPlateActionListener
    ) {
        mKeyboardView?.setOnKeyboardActionListener(listener)
        mCandidateView?.setOnCandidateActionListener(l)
        mStrokeView?.setOnStrokeActionListener(ll)
        mNum9KeyboardView?.setOnNumKeyboardActionListener(lll)
        mSymbolView?.setOnSymbolActionListener(llll)
        mSuperPenguinEmojiContainer?.setOnEmojiListener(lllll)
        mNewPlatePop.setOnNewPlateActionListener(llllll)
    }

    /**
     * 加载键盘布局
     * @param layoutInflater LayoutInflater
     * @return ConstraintLayout?
     */
    private fun initInputLayout(layoutInflater: LayoutInflater): ConstraintLayout? {
        val inputContainer = layoutInflater.inflate(R.layout.layout_inputview, null)
        Log.d(TAG, "current width is : ${mContext.resources.displayMetrics.widthPixels}")
        Log.d(TAG, "current height is : ${mContext.resources.displayMetrics.heightPixels}")
        currentInputView = inputContainer as ConstraintLayout?
        if (!ThemeState.isDefault) {
            currentInputView?.setBackgroundColor(mContext.getColor(R.color.white_bg))
        }
        mCandidateView = currentInputView?.findViewById(R.id.candidate_view)
        val candidateViewLayoutParams =
            mCandidateView?.layoutParams as ConstraintLayout.LayoutParams
        candidateViewLayoutParams.height =
            mContext.resources.getDimension(R.dimen.candidateViewLayoutParamsheight).toInt()
        candidateViewLayoutParams.width =
            mContext.resources.getDimension(R.dimen.candidateViewLayoutParamswidth).toInt()
        candidateViewLayoutParams.startToStart = -1
        mCandidateView?.layoutParams = candidateViewLayoutParams
        mKeyboardView = currentInputView?.findViewById(R.id.keyboard_view)
        val keyboardLayoutParams = mKeyboardView?.layoutParams as ConstraintLayout.LayoutParams
        keyboardLayoutParams.width =
            mContext.resources.getDimension(R.dimen.keyboardLayoutParamswidth).toInt()
        keyboardLayoutParams.leftMargin =
            mContext.resources.getDimension(R.dimen.keyboardLayoutParamsleftMargin).toInt()
        mKeyboardView?.layoutParams = keyboardLayoutParams
        mNum9KeyboardView = currentInputView?.findViewById(R.id.num9_view)
        val num9LayoutParams = mNum9KeyboardView?.layoutParams as ConstraintLayout.LayoutParams
        num9LayoutParams.width =
            mContext.resources.getDimension(R.dimen.num9LayoutParamswidth).toInt()
        mNum9KeyboardView?.layoutParams = num9LayoutParams
        mMoreCandidateView = currentInputView?.findViewById(R.id.more_candidate_view)
        val moreCandidateViewLayoutParams =
            mMoreCandidateView?.layoutParams as ConstraintLayout.LayoutParams
        moreCandidateViewLayoutParams.width =
            mContext.resources.getDimension(R.dimen.moreCandidateViewLayoutParamswidth).toInt()
        mMoreCandidateView?.layoutParams = moreCandidateViewLayoutParams

        mStrokeView = currentInputView?.findViewById(R.id.stroke_view)
        val strokeViewLayoutParams = mStrokeView?.layoutParams as ConstraintLayout.LayoutParams
        strokeViewLayoutParams.width =
            mContext.resources.getDimension(R.dimen.strokeViewLayoutParamswidth).toInt()
        strokeViewLayoutParams.height =
            mContext.resources.getDimension(R.dimen.strokeViewLayoutParamsheight).toInt()
        strokeViewLayoutParams.leftMargin =
            mContext.resources.getDimension(R.dimen.strokeViewLayoutParamsleftMargin).toInt()
        strokeViewLayoutParams.bottomMargin =
            mContext.resources.getDimension(R.dimen.strokeViewLayoutParamsbottomMargin).toInt()
        mStrokeView?.layoutParams = strokeViewLayoutParams
        mSymbolView = currentInputView?.findViewById(R.id.symbol_view)
        val symbolViewLayoutParams = mSymbolView?.layoutParams as ConstraintLayout.LayoutParams
        symbolViewLayoutParams.width =
            mContext.resources.getDimension(R.dimen.symbolViewLayoutParamswidth).toInt()
        mSymbolView?.layoutParams = symbolViewLayoutParams
        mSuperPenguinEmojiContainer = currentInputView?.findViewById(R.id.superpenguin_emojicontainer_layout)
        val superPenguinLEmojiContainerLayoutParams = mSuperPenguinEmojiContainer?.layoutParams as ConstraintLayout.LayoutParams
        superPenguinLEmojiContainerLayoutParams.width =
            mContext.resources.getDimension(R.dimen.superPenguinLEmojiContainerLayoutParamswidth).toInt()
        superPenguinLEmojiContainerLayoutParams.height =
            mContext.resources.getDimension(R.dimen.superPenguinLEmojiContainerLayoutParamsheight).toInt()
        mSuperPenguinEmojiContainer?.layoutParams = superPenguinLEmojiContainerLayoutParams
        mComposingView = ComposingView(mContext)
        themeManager.addThemeAbleView(mKeyboardView!!)
        themeManager.addThemeAbleView(mCandidateView!!)
        themeManager.addThemeAbleView(mMoreCandidateView!!)
        themeManager.addThemeAbleView(mStrokeView!!)
        themeManager.addThemeAbleView(mNum9KeyboardView!!)
        themeManager.addThemeAbleView(mSymbolView!!)
        themeManager.addThemeAbleView(mComposingView!!)
        mSuperPenguinEmojiContainer?.initView()
        mNewPlatePop = NewPlatePop()
        mNewPlatePop.initView(mCandidateView!!,mDialog)
        mVoiceViewManager = VoiceViewManager()
        mVoiceViewManager.init(currentInputView!!)
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
        var textMode = MODE_TEXT
        var actionMode = 0
        if (editorInfo != null) {
            val type = editorInfo.inputType and EditorInfo.TYPE_MASK_CLASS
            if (type == EditorInfo.TYPE_CLASS_NUMBER || type == EditorInfo.TYPE_CLASS_DATETIME || type == EditorInfo.TYPE_CLASS_PHONE) {
                textMode = MODE_PWD
            } else if (type == EditorInfo.TYPE_CLASS_TEXT) {
                textMode = when (editorInfo.inputType and EditorInfo.TYPE_MASK_VARIATION) {
                    EditorInfo.TYPE_TEXT_VARIATION_URI -> MODE_URL
                    EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> MODE_EMAIL
                    EditorInfo.TYPE_TEXT_VARIATION_PASSWORD, EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD -> MODE_PWD
                    else -> MODE_TEXT
                }
            }
            actionMode = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
            if (editorInfo.inputType and EditorInfo.TYPE_MASK_FLAGS == EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) {
                //多行文本
                actionMode = EditorInfo.IME_ACTION_UNSPECIFIED
            }
        }
        mActionMode = actionMode
        mTextMode = textMode
        loadKeyboardId()
        switchKbByMode()
        mKeyboardView?.setEnterActionMode(actionMode)
        mSymbolView?.setEnterActionMode(actionMode)
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

    fun switchKb(index: Int) {
        mCurrentSelectKB = index
        if (index == 0 || index == 1) {
            mLastKB = index
        } else {
            mLastHWR = index
        }
        loadKeyboardId()
        switchKb()
        updateEnterTextOutComposing()
    }

    fun switchKb() {
        hideExtraView(0)
        mKbLayout = 0
        val keyboard: Keyboard = getKeyboard(mCurrentKbd!!.kbName, mCurrentKbd!!.keyboardMode)
        showStrokeView()
        setKeyboard(keyboard)
    }

    fun switchToLastKB() {
        switchKb(mLastKB)
    }

    fun switchToLastHWR() {
        switchKb(mLastHWR)
    }

    /**
     * 展示更多候选区
     * @param listener OnCandidateActionListener
     */
    fun showMoreCandidate(listener: OnCandidateActionListener) {
        if (mMoreCandidateView != null && mMoreCandidateView?.visibility == View.VISIBLE) {
            mMoreCandidateView?.goBack()
            return
        }
        hideExtraView(0)
        val enstate = mCurrentKbd!!.kbName == QWERTY_HWR_EN || mCurrentKbd!!.kbName == QWERTY_EN
        val hasletter = hasLetter(enstate)
        mMoreCandidateView?.show(listener, enstate, hasletter)
        mMoreCandidateView?.setCandidateData(
            mCandidateView!!.getData(),
            mCandidateView!!.isRTLMode()
        )
    }

    fun onFinishInputView() {
        dismissVoiceView()
        reloadCandateMode()
        hideExtraView(0)
        checkAndHideDialog()
    }

    /**
     * 显示语音界面
     */
    fun showVoiceView() {
        mVoiceViewManager.show(
            object : VoicePopupWindow.VoicePopupListener {
                override fun onVoiceEnd(text: String) {
                    Log.d("", "onVoiceEnd: $text")
                    if (text.length > 1) {
                        mImeEditor.sendSentence(text)
                        scope.launch(Dispatchers.Main.immediate) {
                            delay(500)
                            dismissVoiceView()
                        }
                    } else {
                        dismissVoiceView()
                    }
                }

                override fun onError() {
                }

                override fun onLongTime() {
//                    dismissVoiceView()
                }

            }
        )
    }

    /**
     * 隐藏语音界面
     */
    fun dismissVoiceView() {
        mVoiceViewManager.dismiss()
    }


    /**
     * 清理存在的数据，回收资源
     */
    fun clear() {

    }

    fun setShifted() {
        mKeyboardView?.setShifted(!mKeyboardView!!.isShifted())
    }

    fun resetShifted() {
        mKeyboardView?.setShifted(false)
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
        mCandidateView?.setCandidateData(data)
        updateMoreCandidateViewData()
    }

    /**
     * 处理更多候选区返回动作
     */
    fun handleOnBack() {
        if (mMoreCandidateView != null && mMoreCandidateView?.visibility == View.VISIBLE) {
            mMoreCandidateView?.hideAndScroll2Top()
            showStrokeView()
        }
    }

    fun updateEnterTextInComposing() {
        mKeyboardView?.setEnterActionModeInComposing()
        mSymbolView?.setEnterActionModeInComposing()
        mMoreCandidateView?.setEnterActionModeInComposing()
    }

    fun updateEnterTextOutComposing() {
        mKeyboardView?.setEnterActionMode(mActionMode)
        mSymbolView?.setEnterActionMode(mActionMode)
        mMoreCandidateView?.setEnterActionMode(mActionMode)
    }

    /**
     * 展示手写板
     */
    fun showStrokeView() {
        if (mCurrentKbd?.kbName == QWERTY_HWR_CN || mCurrentKbd?.kbName == QWERTY_HWR_EN) {
            mStrokeView?.initView(mKeyboardView!!)
            mStrokeView?.visibility = View.VISIBLE
        }
    }

    /**
     * 展示手写板
     */
    fun numberActionBack() {
        if (mCurrentKbd!!.kbName == QWERTY_HWR_CN || mCurrentKbd!!.kbName == QWERTY_HWR_EN) {
            handleOnBack()
        } else {
            if (mMoreCandidateView != null && mMoreCandidateView?.visibility == View.VISIBLE) {
                mMoreCandidateView?.hideAndScroll2Top()
            }
        }
    }

    /**
     * 展示符号键盘
     */
    fun showSymbolView() {
        mSymbolView?.showView(mKeyboardView!!)
    }

    fun showEmojiView() {
        hideExtraView(1)
        if (mSuperPenguinEmojiContainer != null && mSuperPenguinEmojiContainer?.visibility == GONE) {
            mCandidateView?.setEmojiMode()
            mSuperPenguinEmojiContainer?.show()
        }
    }

    fun showComposing() {
        mComposingView?.showAsDropDown(currentInputView, 0, 0, Gravity.TOP or Gravity.START)
    }

    fun updateComposing(str: String) {
        scope.launch(Dispatchers.Main.immediate) {
            if (str.isEmpty()) {
                mComposingView?.dismiss()
            } else {
                if (mComposingView?.isShowing != true) {
                    showComposing()
                }
                mComposingView?.updateComposingText(str)
            }
        }
    }

    fun showParkingKeyboard(window: Dialog) {
        hideExtraView(2)
        if (!mNewPlatePop.isShowing) {
            mCandidateView?.setNumberplateMode()
            mNewPlatePop.show()
        }
    }

    fun hideParkingKeyboard(window: Dialog) {
        mNewPlatePop.dismiss(window)
        notifyCandidateLayoutModeChange(mCurrentKbd!!)
    }

    private fun switchKbByMode() {
        val kbName = if (mTextMode == MODE_NUMBER) {
            mKbLayout = 1
            mCurrentKbd!!.kbName
        } else {
            mKbLayout = 0
            mCurrentKbd!!.kbName
        }
        val keyboard: Keyboard = getKeyboard(kbName, mCurrentKbd!!.keyboardMode)
        showStrokeView()
        setKeyboard(keyboard)
        setNum9Keyboard()
    }

    private fun loadKeyboardId() {
        if (mTextMode == MODE_PWD || mTextMode == MODE_EMAIL) {
            mImeEditor.inputMethodChange(4)
            val kbName = LanguageUtil.KbRes.KB_NAME[4]
            mCurrentKbd =
                KeyboardId(
                    kbName,
                    LanguageUtil.KbRes.NUM_PREFIX + kbName,
                    LanguageUtil.KbRes.SYMBOL_PREFIX + kbName,
                    0
                )
        } else {
            mImeEditor.inputMethodChange(mCurrentSelectKB)
            val kbName = LanguageUtil.KbRes.KB_NAME[mCurrentSelectKB]
            mCurrentKbd =
                KeyboardId(
                    kbName,
                    LanguageUtil.KbRes.NUM_PREFIX + kbName,
                    LanguageUtil.KbRes.SYMBOL_PREFIX + kbName,
                    mCurrentSelectKB
                )
        }
    }


    private fun setKeyboard(keyboard: Keyboard) {
        mKeyboardView!!.setKeyboard(keyboard)
        mKeyboardView!!.sendUpdateSpaceMsg()
        notifyCandidateLayoutModeChange(mCurrentKbd!!)
    }

    private fun setNum9Keyboard() {
        val keyboard: Keyboard = getKeyboard("num_9", mCurrentKbd!!.keyboardMode)
        mNum9KeyboardView!!.setKeyboard(keyboard)
    }

    private fun notifyCandidateLayoutModeChange(id: KeyboardId) {
        showStrokeView()
        if (id.kbName.contains("arab") || id.kbName.contains("farsi")) {
            mCandidateView!!.setRTLMode(true, id.kbName)
        } else {
            mCandidateView!!.setRTLMode(false, id.kbName)
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

    private fun updateMoreCandidateViewData() {
        val enstate = mCurrentKbd!!.kbName == QWERTY_HWR_EN || mCurrentKbd!!.kbName == QWERTY_EN
        val hasletter = hasLetter(enstate)
        scope.launch(Dispatchers.Main.immediate) {
            mMoreCandidateView?.updateDisabledState(hasletter)
            mMoreCandidateView?.setCandidateData(
                mCandidateView!!.getData(),
                mCandidateView!!.isRTLMode()
            )
        }
    }

    private fun hasLetter(enstate: Boolean): Boolean {
        if (enstate) return false
        val newList: ArrayList<String?> = ArrayList<String?>()
        synchronized(this) {
            for (item in mCandidateView!!.getData()) {
                if (item.length < 2) {
                    newList.add(item)
                }
            }
        }
        return newList.size != 0
    }

    /**
     * 收起手写板和符号键盘和更多候选界面和Emoji界面和车牌界面
     * type 0隐藏所有 1不隐藏Emoji 2不隐藏车牌
     */
    private fun hideExtraView(type: Int) {
        mStrokeView?.destroyView()
        mStrokeView?.visibility = View.GONE
        mSymbolView?.hideView()
        if (mMoreCandidateView != null && mMoreCandidateView?.visibility == View.VISIBLE) {
            mMoreCandidateView?.hideAndScroll2Top()
        }
        if (mSuperPenguinEmojiContainer != null && mSuperPenguinEmojiContainer?.visibility == VISIBLE && type != 1) {
            mSuperPenguinEmojiContainer?.hide()
        }
        if (mNewPlatePop.isShowing && type != 2) {
            mNewPlatePop.dismiss(mDialog)
        }
    }

    private fun reloadCandateMode() {
        mCurrentKbd?.let { notifyCandidateLayoutModeChange(it) }
    }
}
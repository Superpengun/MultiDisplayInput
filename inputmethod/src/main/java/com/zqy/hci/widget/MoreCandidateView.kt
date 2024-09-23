package com.zqy.hci.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.zqy.hci.R
import com.zqy.hci.adapter.MoreCandidateAdapter
import com.zqy.hci.listener.OnCandidateActionListener
import com.zqy.hci.service.AudioAndHapticFeedbackManager
import com.zqy.hci.theme.ThemeAbleView
import com.zqy.hci.theme.ThemeState
import com.zqy.hci.theme.UITheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

/**
 * @Author:CWQ
 * @DATE:2022/1/18
 * @DESC:更多首选词View
 */
class MoreCandidateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener, ThemeAbleView {
    private val TAG = MoreCandidateView::class.java.simpleName

    private val mRootView: View
    private var mRvList: RecyclerView
    private var mIbUptoTop: ImageButton
    private var mIbPrePage: ImageButton
    private var mIbNextPage: ImageButton
    private var mIbBack: ImageButton
    private var mIbSwitch: ImageButton
    private var mIbClear: ImageButton
    private var mIbDelete: ImageButton
    private var mIbEnter: ImageButton
    private var mIbSpace: ImageButton

    private var mHeight = 0
    private var mWidth = 0
    private var mItemHeight = 0
    private var mMinItemWidth = 0
    private var mBottomHeight = 0

    private var mDefaultHeight = 0
    private var mFontSize = 0f
    private var mBgColor: Int = 0
    private var mPrePageDrawable: Drawable? = null
    private var mNextPageDrawable: Drawable? = null
    private var mBackDrawable: Drawable? = null
    private var mIbUptoTopDrawable: Drawable? = null
    private var mIbSwitchDrawable: Drawable? = null
    private var mIbClearDrawable: Drawable? = null
    private var mIbDeleteDrawable: Drawable? = null
    private var mIbEnterDrawable: Drawable? = null
    private var mIbSpaceDrawable: Drawable? = null
    private var mBtnDrawable: Drawable? = null
    private var mIbSwitchDisabledDrawable: Drawable? = null
    private var mBtnDisabledDrawable: Drawable? = null
    private var mUITheme: UITheme? = null

    private var mLayoutManager: FlexboxLayoutManager? = null
    private var mListener: OnCandidateActionListener? = null
    private var mAdapter: MoreCandidateAdapter? = null
    private val mData = arrayListOf<String>()
    private var mSwitch = false
    private var ICON_ENTERDONE: Int = 0
    private var ICON_ENTERGO: Int = 0
    private var ICON_ENTERSEARCH: Int = 0
    private var ICON_ENTERNEXT: Int = 0
    private var ICON_ENTERPREVIOUS: Int = 0
    private var ICON_ENTERCONFIRM: Int = 0

    private var mHasletter = true
    init {
        mRootView = LayoutInflater.from(context).inflate(R.layout.layout_more_candidate, this)
        mRvList = findViewById(R.id.more_candidate_rv)
        mIbUptoTop = findViewById(R.id.more_candidate_uptoTop)
        mIbPrePage = findViewById(R.id.more_candidate_up)
        mIbNextPage = findViewById(R.id.more_candidate_down)
        mIbBack = findViewById(R.id.more_candidate_back)
        mIbSwitch = findViewById(R.id.more_candidate_switch)
        mIbClear = findViewById(R.id.more_candidate_clear)
        mIbDelete = findViewById(R.id.more_candidate_delete)
        mIbEnter = findViewById(R.id.more_candidate_enter)
        mIbSpace = findViewById(R.id.more_candidate_space)
    }


    override fun setUITheme(uiTheme: UITheme) {
        mUITheme = uiTheme
        val typeArray = uiTheme.context.applicationContext.obtainStyledAttributes(
            uiTheme.themeResId,
            R.styleable.HciKeyboardViewTheme
        )
        val bgColor =
            typeArray.getColor(
                R.styleable.HciKeyboardViewTheme_candidateBgColor,
                resources.getColor(R.color.baseCandidateBg, null)
            )
        val fontSize = context.resources.getDimension(R.dimen.candidate_text_size)

        //键盘key高度
        val keyboardKeyHeight = typeArray.getDimensionPixelOffset(
            R.styleable.HciKeyboardViewTheme_keyboardKeyHeight,
            R.dimen.keyboard_key_height
        )
        //顶部候选区文字大小
        val candidateFontSize = typeArray.getDimension(
            R.styleable.HciKeyboardViewTheme_candidateFontSize,
            context.resources.getDimension(R.dimen.baseCandidateTextSize)
        )
        //计算键盘+顶部候选区高度
        mDefaultHeight = context.resources.getDimension(R.dimen.morecandidate_view_height).toInt()

        mHeight = mDefaultHeight
        mWidth = context.resources.getDimension(R.dimen.morecandidate_view_width).toInt()
        mMinItemWidth = mWidth / 4
        mItemHeight = mHeight / 5

        typeArray.recycle()

        val iconTypeArray = uiTheme.context.applicationContext.obtainStyledAttributes(
            uiTheme.iconThemeResId,
            R.styleable.HciKeyboardFunctionIconTheme
        )
        val preId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_moreCandidatePrePageIcon,
            R.drawable.symbol_pre_icon
        )
        val preDrawable =
            AppCompatResources.getDrawable(context,preId)
        val nextId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_moreCandidateNextPageIcon,
            R.drawable.symbol_next_icon
        )
        val nextDrawable =
            AppCompatResources.getDrawable(context,nextId)
        val backId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_moreCandidateBackIcon,
            R.drawable.symbol_back_icon
        )
        val backDrawable =
            AppCompatResources.getDrawable(context,backId)
        val utId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_moreCandidateUptoTopIcon,
            R.drawable.candidate_pt_icon
        )
        val uptoTopDrawable =
            AppCompatResources.getDrawable(context,utId)
        val switchId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_moreCandidateSwitchIcon,
            R.drawable.candidate_switch_icon
        )
        val switchDrawable =
            AppCompatResources.getDrawable(context,switchId)
        val disabledId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_disabledIcon,
            R.drawable.icon_switch_disabled
        )
        val disabledDrawable =
            AppCompatResources.getDrawable(context,disabledId)
        val btnDisabledId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_buttonDisabledIcon,
            R.drawable.keyboard_disabled_key
        )
        val btnDisabledDrawable =
            AppCompatResources.getDrawable(context,btnDisabledId)
        val clearId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_moreCandidateClearIcon,
            R.drawable.icon_clear
        )
        val clearDrawable =
            AppCompatResources.getDrawable(context,clearId)
        val deleteId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_moreCandidateDeleteIcon,
            R.drawable.icon_delete
        )
        val deleteDrawable =
            AppCompatResources.getDrawable(context,deleteId)
        val enterId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_moreCandidateEnterIcon,
            R.drawable.icon_enter
        )
        val enterDrawable =
            AppCompatResources.getDrawable(context,enterId)
        val spaceId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_moreCandidateSpaceIcon,
            R.drawable.icon_space
        )
        val spaceDrawable =
            AppCompatResources.getDrawable(context,spaceId)
        val btnId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_buttonIcon,
            R.drawable.btn_symbol_bottom
        )
        val btnDrawable =
            AppCompatResources.getDrawable(context,btnId)
        ICON_ENTERDONE = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_iconEnterDone,
            R.drawable.icon_enter_done
        )
        ICON_ENTERGO = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_iconEnterGo,
            R.drawable.icon_enter_go
        )
        ICON_ENTERSEARCH = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_iconEnterSearch,
            R.drawable.icon_enter_search
        )
        ICON_ENTERNEXT = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_iconEnterNext,
            R.drawable.icon_enter_next
        )
        ICON_ENTERPREVIOUS = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_iconEnterPrevious,
            R.drawable.icon_enter_previous
        )
        ICON_ENTERCONFIRM = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_iconEnterConfirm,
            R.drawable.icon_enter_confirm
        )
        iconTypeArray.recycle()

        mBgColor = bgColor
        mFontSize = fontSize
        if (preDrawable != null) {
            mPrePageDrawable = preDrawable
        }
        if (nextDrawable != null) {
            mNextPageDrawable = nextDrawable
        }
        if (backDrawable != null) {
            mBackDrawable = backDrawable
        }
        if (uptoTopDrawable != null) {
            mIbUptoTopDrawable = uptoTopDrawable
        }
        if (switchDrawable != null) {
            mIbSwitchDrawable = switchDrawable
        }
        if (clearDrawable != null) {
            mIbClearDrawable = clearDrawable
        }
        if (deleteDrawable != null) {
            mIbDeleteDrawable = deleteDrawable
        }
        if (enterDrawable != null) {
            mIbEnterDrawable = enterDrawable
        }
        if (spaceDrawable != null) {
            mIbSpaceDrawable = spaceDrawable
        }
        if (btnDrawable != null) {
            mBtnDrawable = btnDrawable
        }
        if (disabledDrawable != null) {
            mIbSwitchDisabledDrawable = disabledDrawable
        }
        if (btnDisabledDrawable != null) {
            mBtnDisabledDrawable = btnDisabledDrawable
        }
        if (!ThemeState.isDefault) {
            mIbPrePage.setBackgroundResource(btnId)
            mIbNextPage.setBackgroundResource(btnId)
            mIbBack.setBackgroundResource(btnId)
            mIbUptoTop.setBackgroundResource(btnId)
            mIbSwitch.setBackgroundResource(btnId)
            mIbClear.setBackgroundResource(btnId)
            mIbDelete.setBackgroundResource(btnId)
            mIbEnter.setBackgroundResource(btnId)
            mIbSpace.setBackgroundResource(btnId)
        }
        setThemeConfig()
        mAdapter?.setData(mData, mFontSize)
    }


    /**
     * 设置更多候选词数据
     * @param data ArrayList<String>
     * @param isRTLMode 是否是从右向左
     */
    fun setCandidateData(data: ArrayList<String>, isRTLMode: Boolean = false) {
        if (mUITheme == null) {
            Log.e(TAG, "请先设置主题！")
            return
        }
        mData.clear()
        mData.addAll(data)

        mAdapter = MoreCandidateAdapter(context, mItemHeight, mMinItemWidth)
        mAdapter!!.setOnItemListener { position, word ->
            mListener?.onCandidateSelected(position, word)
            mListener?.onBack()
        }
        mRvList.adapter = mAdapter

        if (mLayoutManager == null) {
            mLayoutManager = if (isRTLMode) {
                FlexboxLayoutManager(context, FlexDirection.ROW_REVERSE)
            } else {
                FlexboxLayoutManager(context, FlexDirection.ROW)
            }
        }
        mRvList.layoutManager = mLayoutManager
        mAdapter?.setData(mData, mFontSize)
        mRvList.post {
            updatePageBtnState()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setThemeConfig()
        mIbPrePage.setOnClickListener(this)
        mIbNextPage.setOnClickListener(this)
        mIbBack.setOnClickListener(this)
        mIbUptoTop.setOnClickListener(this)
        mIbSwitch.setOnClickListener(this)
        mIbClear.setOnClickListener(this)
        mIbDelete.setOnClickListener(this)
        mIbEnter.setOnClickListener(this)

        mLayoutManager = FlexboxLayoutManager(context)
        mRvList.layoutManager = mLayoutManager
        mRvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    return
                }
                val lastPosition = mLayoutManager!!.findLastVisibleItemPosition()
                updatePageBtnState()
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)

        val height = if (heightSpecMode == MeasureSpec.EXACTLY) {
            heightSpecSize
        } else {
            mDefaultHeight
        }
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.more_candidate_up -> {
                pagePre()
            }
            R.id.more_candidate_down -> {
                pageNext()
            }
            R.id.more_candidate_back -> {
                goBack()
            }
            R.id.more_candidate_uptoTop -> {
                mRvList.smoothScrollToPosition(0)
            }
            R.id.more_candidate_switch -> {
                if (mHasletter) return
                updateSwitchState()
                if (mSwitch) {
                    mListener?.onLetter()
                } else {
                    mListener?.onWord()
                }
            }
            R.id.more_candidate_space -> {
                refreshSwitchState()
                mListener?.onCandidateSpace()
                mData.clear()
                mListener?.onBack()
            }
            R.id.more_candidate_clear -> {
                refreshSwitchState()
                mListener?.onCandidateReset()
                mData.clear()
                mListener?.onBack()
            }
            R.id.more_candidate_delete -> {
                mListener?.onDelete()
                refreshSwitchState()
            }
            R.id.more_candidate_enter -> {
                refreshSwitchState()
                mListener?.onCandidateAction()
                mData.clear()
                mListener?.onBack()
            }
        }
        AudioAndHapticFeedbackManager.getInstance().performAudioFeedback(0)
    }

    fun hideAndScroll2Top() {
        visibility = GONE
        refreshSwitchState()
        Timer().schedule(object : TimerTask() {
            override fun run() {
                post {
                    Log.d(TAG, "run: hideCandidate!!!")
                    mRvList.scrollToPosition(0)
                    mAdapter = null
                }
            }
        }, 50)
    }

    fun show(listener: OnCandidateActionListener, enstate: Boolean, hasletter: Boolean) {
        if (enstate) {
            mIbSwitch.visibility = GONE
            mIbSpace.visibility = VISIBLE
            mIbSpace.setOnClickListener {
                refreshSwitchState()
                mListener?.onCandidateSpace()
                mData.clear()
                mListener?.onBack()
            }
        } else {
            mHasletter = hasletter
            mIbSwitch.visibility = VISIBLE
            mIbSpace.visibility = GONE
            if (hasletter){
                mIbSwitch.setOnClickListener {
                    updateSwitchState()
                    if (mSwitch) {
                        mListener?.onLetter()
                    } else {
                        mListener?.onWord()
                    }
                }
                mIbSpace.visibility = GONE
                mIbSwitch.background =mBtnDrawable
                mIbSwitch.setImageDrawable(mIbSwitchDrawable)
            }else {
                mIbSwitch.setOnClickListener{}
                mIbSwitch.background =mBtnDisabledDrawable
                mIbSwitch.setImageDrawable(mIbSwitchDisabledDrawable)
            }
        }
        visibility = View.VISIBLE
        mListener = listener
    }

    fun updateDisabledState(hasletter: Boolean){
        mHasletter = hasletter
        if (mHasletter){
            mIbSwitch.setOnClickListener {
                updateSwitchState()
                if (mSwitch) {
                    mListener?.onLetter()
                } else {
                    mListener?.onWord()
                }
            }
            mIbSpace.visibility = GONE
            mIbSwitch.background =mBtnDrawable
            mIbSwitch.setImageDrawable(mIbSwitchDrawable)
        }else {
            mIbSwitch.setOnClickListener{}
            mIbSwitch.background =mBtnDisabledDrawable
            mIbSwitch.setImageDrawable(mIbSwitchDisabledDrawable)

        }
    }
    fun setEnterActionMode(enterActionMode: Int) {
        val currentMode = if (enterActionMode > 0) enterActionMode else 0
        val ENTER_ICON = intArrayOf(
            ICON_ENTERDONE,
            ICON_ENTERGO,
            ICON_ENTERSEARCH,
            ICON_ENTERDONE,
            ICON_ENTERNEXT,
            ICON_ENTERDONE,
            ICON_ENTERPREVIOUS,
            ICON_ENTERDONE
        )
        Log.d(TAG, "setEnterActionMode: enterActionMode=$enterActionMode")
        if (currentMode <= 1 || currentMode >= 8) {
            mIbEnter.setImageDrawable(AppCompatResources.getDrawable(context,ENTER_ICON[0]))
        } else {
            mIbEnter.setImageDrawable(AppCompatResources.getDrawable(context,ENTER_ICON[currentMode - 1]))
        }
    }

    fun setEnterActionModeInComposing() {
        mIbEnter.setImageDrawable(AppCompatResources.getDrawable(context,ICON_ENTERCONFIRM))
    }

    fun goBack(){
        refreshSwitchState()
        mData.clear()
        mListener?.onWord()
        mListener?.onBack()
    }

    private fun setThemeConfig() {
        if (mPrePageDrawable != null && mNextPageDrawable != null) {
            mRootView.setBackgroundColor(mBgColor)
            mIbPrePage.setImageDrawable(mPrePageDrawable)
            mIbNextPage.setImageDrawable(mNextPageDrawable)
            mIbBack.setImageDrawable(mBackDrawable)
            mIbUptoTop.setImageDrawable(mIbUptoTopDrawable)
            mIbSwitch.setImageDrawable(mIbSwitchDrawable)
            mIbClear.setImageDrawable(mIbClearDrawable)
            mIbDelete.setImageDrawable(mIbDeleteDrawable)
            mIbEnter.setImageDrawable(mIbEnterDrawable)
            mIbSpace.setImageDrawable(mIbSpaceDrawable)
        }
    }


    private fun pagePre() {
        val firstPosition = mLayoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
        val itemView = mLayoutManager!!.findViewByPosition(firstPosition)
        val childTop = itemView!!.top
        mRvList.smoothScrollBy(0, childTop - mItemHeight * 4)
    }

    private fun pageNext() {
        val lastPosition = mLayoutManager?.findLastCompletelyVisibleItemPosition() ?: 0
        val itemView = mLayoutManager!!.findViewByPosition(lastPosition)
        val childBottom = itemView!!.bottom
        if (mAdapter!!.itemCount < lastPosition + 16) {
            mListener?.getMoreList()
            GlobalScope.launch(Dispatchers.Main) {
                mRvList.smoothScrollToPosition(lastPosition + 16)
            }
        } else {
            mRvList.smoothScrollBy(0, mItemHeight * 4)
        }
    }

    /**
     * 更新翻页按钮状态
     */
    private fun updatePageBtnState() {
        val firstPosition = mLayoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
        val lastPosition = mLayoutManager?.findLastCompletelyVisibleItemPosition() ?: 0

        mIbPrePage.isEnabled = firstPosition > 0
        mIbUptoTop.isEnabled = firstPosition > 0
        mIbNextPage.isEnabled = lastPosition < mData.size - 1
    }

    private fun updateSwitchState() {
        mSwitch = !mSwitch
        mIbSwitch.isActivated = mSwitch
    }

    private fun refreshSwitchState() {
        mSwitch = false
        mIbSwitch.isActivated = mSwitch
    }
}
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
import com.zqy.hci.listener.OnPlateActionListener
import com.zqy.hci.service.AudioAndHapticFeedbackManager
import com.zqy.hci.theme.ThemeAbleView
import com.zqy.hci.theme.ThemeState
import com.zqy.hci.theme.UITheme
import java.util.*
import kotlin.collections.ArrayList

class PlateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener, ThemeAbleView {
    private val TAG = PlateView::class.java.simpleName

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

    private var mHeight = 0
    private var mWidth = 0
    private var mItemHeight = 0
    private var mMinItemWidth = 0

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
    private var mBtnDrawable: Drawable? = null
    private var mUITheme: UITheme? = null

    private var mLayoutManager: FlexboxLayoutManager? = null
    private var mListener: OnPlateActionListener? = null
    private var mAdapter: MoreCandidateAdapter? = null
    private val mData = arrayListOf<String>()
    private var mSwitch = false
    private var ICON_ENTERDONE: Int = 0
    private var ICON_ENTERGO: Int = 0
    private var ICON_ENTERSEARCH: Int = 0
    private var ICON_ENTERNEXT: Int = 0
    private var ICON_ENTERPREVIOUS: Int = 0
    private var ICON_ENTERCONFIRM: Int = 0

    init {
        mRootView = LayoutInflater.from(context).inflate(R.layout.layout_plate, this)
        mRvList = findViewById(R.id.plate_rv)
        mIbUptoTop = findViewById(R.id.plate_uptoTop)
        mIbPrePage = findViewById(R.id.plate_up)
        mIbNextPage = findViewById(R.id.plate_down)
        mIbBack = findViewById(R.id.plate_back)
        mIbSwitch = findViewById(R.id.plate_switch)
        mIbClear = findViewById(R.id.plate_clear)
        mIbDelete = findViewById(R.id.plate_delete)
        mIbEnter = findViewById(R.id.plate_enter)
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
            R.styleable.HciKeyboardFunctionIconTheme_plateSwitchIcon,
            R.drawable.plate_switch_icon
        )
        val switchDrawable =
            AppCompatResources.getDrawable(context,switchId)
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
        if (btnDrawable != null) {
            mBtnDrawable = btnDrawable
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
        }
        setThemeConfig()
        mAdapter?.setData(mData, mFontSize)
    }


    /**
     * 设置更多候选词数据
     * @param data ArrayList<String>
     * @param isRTLMode 是否是从右向左
     */
    fun setPlateData(data: ArrayList<String>) {
        if (mUITheme == null) {
            Log.e(TAG, "请先设置主题！")
            return
        }
        mData.clear()
        mData.addAll(data)

        if (mAdapter == null) {
            mAdapter = MoreCandidateAdapter(context, mItemHeight, mMinItemWidth)
            mAdapter!!.setOnItemListener { position, word ->
                mListener?.onPlateSelected(position, word)
                if (mSwitch) {
                    mListener?.onPlateBack()
                } else {
                    updateSwitchState()
                    onCity()
                }
            }
            mRvList.adapter = mAdapter
        }
        if (mLayoutManager == null) {
            mLayoutManager
            FlexboxLayoutManager(context, FlexDirection.ROW)
            mRvList.layoutManager = mLayoutManager
        }
        mAdapter?.setData(mData, mFontSize)
        mIbPrePage.isEnabled = false
        mIbUptoTop.isEnabled = false
        updatePageBtnState()
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
            R.id.plate_up -> {
                pagePre()
            }
            R.id.plate_down -> {
                pageNext()
            }
            R.id.plate_back -> {
                mListener?.onPlateBack()
            }
            R.id.plate_uptoTop -> {
                mRvList.smoothScrollToPosition(0)
            }
            R.id.plate_switch -> {
                updateSwitchState()
                if (mSwitch) {
                    onCity()
                } else {
                    onProvince()
                }
            }
            R.id.plate_clear -> {
                refreshSwitchState()
                mListener?.onPlateReset()
                mData.clear()
                mListener?.onPlateBack()
            }
            R.id.plate_delete -> {
                mListener?.onPlateDelete()
                refreshSwitchState()
            }
            R.id.plate_enter -> {
                refreshSwitchState()
                mListener?.onPlateAction()
                mData.clear()
                mListener?.onPlateBack()
            }
        }
        AudioAndHapticFeedbackManager.getInstance().performAudioFeedback(0)
    }

    fun hideAndScroll2Top() {
        refreshSwitchState()
        post {
            Log.d(TAG, "run: hideCandidate!!!")
            mRvList.scrollToPosition(0)
            visibility = GONE
            mAdapter = null
        }
    }

    fun show(listener: OnPlateActionListener) {
        mIbSwitch.visibility = VISIBLE
        mIbSwitch.setOnClickListener {
            updateSwitchState()
            if (mSwitch) {
                onCity()
            } else {
                onProvince()
            }
        }
        visibility = View.VISIBLE
        mListener = listener
        updatePageBtnState()
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
        }
    }


    private fun pagePre() {
        val firstPosition = mLayoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
        val itemView = mLayoutManager?.findViewByPosition(firstPosition)
        val childTop = itemView?.top
        if (childTop != null) {
            mRvList.smoothScrollBy(0, childTop - mItemHeight * 4)
        }
    }

    private fun pageNext() {
        val lastPosition = mLayoutManager?.findLastCompletelyVisibleItemPosition() ?: 0
        if (mAdapter != null && mAdapter!!.itemCount < lastPosition + 16) {
            post {
                mRvList.smoothScrollToPosition(lastPosition + 16)
            }
        } else {
            post {
                mRvList.smoothScrollToPosition(lastPosition + 16)
            }
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

    private fun onProvince() {
        mRvList.smoothScrollToPosition(0)
        setPlateData("京,沪,津,渝,鲁,冀,晋,蒙,辽,吉,黑,苏,浙,皖,闽,赣,豫,湘,鄂,粤,桂,琼,川,贵,云,藏,陕,甘,青,宁,新,港,澳,台".split(',') as ArrayList<String>)
        invalidate()
    }

    private fun onCity() {
        mRvList.smoothScrollToPosition(0)
        setPlateData("A,B,C,D,E,F,G,H,J,K,L,M,N,P,Q,R,S,T,U,V,W,X,Y,Z".split(',') as ArrayList<String>)
        invalidate()
    }
}
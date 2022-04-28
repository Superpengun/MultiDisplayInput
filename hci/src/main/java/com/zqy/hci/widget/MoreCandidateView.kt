package com.zqy.hci.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.zqy.hci.R
import com.zqy.hci.adapter.MoreCandidateAdapter
import com.zqy.hci.listener.OnCandidateActionListener
import com.zqy.hci.theme.ThemeAbleView
import com.zqy.hci.theme.UITheme
import com.zqy.hci.utils.LayoutUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
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
    private var mLlBottom: LinearLayout
    private var mIbPrePage: ImageButton
    private var mIbNextPage: ImageButton
    private var mIbBack: ImageButton

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
    private var mUITheme: UITheme? = null

    private var mLayoutManager: FlexboxLayoutManager? = null
    private var mListener: OnCandidateActionListener? = null
    private var mAdapter: MoreCandidateAdapter? = null
    private val mData = arrayListOf<String>()

    init {
        mRootView = LayoutInflater.from(context).inflate(R.layout.layout_more_candidate, this)
        mRvList = findViewById(R.id.more_candidate_rv)
        mLlBottom = findViewById(R.id.more_candidate_bottom)
        mIbPrePage = findViewById(R.id.more_candidate_up)
        mIbNextPage = findViewById(R.id.more_candidate_down)
        mIbBack = findViewById(R.id.more_candidate_back)
    }


    override fun setUITheme(uiTheme: UITheme) {
        mUITheme = uiTheme
        val typeArray = uiTheme.context.applicationContext.obtainStyledAttributes(
            uiTheme.themeResId,
            R.styleable.HciKeyboardViewTheme
        )
        val bgColor =
            typeArray.getColor(R.styleable.HciKeyboardViewTheme_candidateBgColor, resources.getColor(R.color.baseCandidateBg, null))
        val fontSize = typeArray.getDimension(
            R.styleable.HciKeyboardViewTheme_moreCandidateFontSize,
            context.resources.getDimension(R.dimen.baseMoreCandidateTextSize)
        )

        //键盘key高度
        val keyboardKeyHeight = typeArray.getDimensionPixelOffset(R.styleable.HciKeyboardViewTheme_keyboardKeyHeight,R.dimen.keyboard_key_height)
        //顶部候选区文字大小
        val candidateFontSize = typeArray.getDimension(
            R.styleable.HciKeyboardViewTheme_candidateFontSize,
            context.resources.getDimension(R.dimen.baseCandidateTextSize)
        )
        //顶部候选区垂直边距
        val candidateVerticalTextPadding = typeArray.getDimension(
            R.styleable.HciKeyboardViewTheme_candidateVerticalTextPadding,
            context.resources.getDimension(R.dimen.baseCandidateVerticalPadding)
        )
        //keyboard配置的垂直方向单个padding值
        val keyboardPadding = context.resources.getDimension(R.dimen.keyboard_margin)
        //keyboard行间距
        val keyboardVerticalGap = context.resources.getDimension(R.dimen.keyboard_key_verticalGap)
        //计算键盘+顶部候选区高度
        mDefaultHeight = (keyboardKeyHeight * 4 + keyboardVerticalGap * 3 + candidateFontSize + candidateVerticalTextPadding * 2 + keyboardPadding * 2).toInt()

        mHeight = mDefaultHeight
        mWidth = LayoutUtils.getScreenWidth(context)
        mMinItemWidth = mWidth / 4
        mItemHeight = mHeight / 5
        mBottomHeight = mItemHeight
        mLlBottom.layoutParams.height = mBottomHeight

        typeArray.recycle()

        val iconTypeArray = uiTheme.context.applicationContext.obtainStyledAttributes(
            uiTheme.iconThemeResId,
            R.styleable.HciKeyboardFunctionIconTheme
        )
        val preDrawable =
            iconTypeArray.getDrawable(R.styleable.HciKeyboardFunctionIconTheme_moreCandidatePrePageIcon)
        val nextDrawable =
            iconTypeArray.getDrawable(R.styleable.HciKeyboardFunctionIconTheme_moreCandidateNextPageIcon)
        val backDrawable =
            iconTypeArray.getDrawable(R.styleable.HciKeyboardFunctionIconTheme_moreCandidateBackIcon)
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
        setThemeConfig()
        mAdapter?.setData(mData,mFontSize)
    }


    /**
     * 设置更多候选词数据
     * @param data ArrayList<String>
     * @param isRTLMode 是否是从右向左
     */
    fun setCandidateData(data: ArrayList<String>, isRTLMode: Boolean = false) {
        if (mUITheme == null){
            Log.e(TAG,"请先设置主题！")
            return
        }
        mData.clear()
        mData.addAll(data)

        if (mAdapter == null) {
            mAdapter = MoreCandidateAdapter(context, mItemHeight, mMinItemWidth)
            mAdapter!!.setOnItemListener { position, word ->
                mListener?.onCandidateSelected(position, word)
                hideAndScroll2Top()
            }
            mRvList.adapter = mAdapter
        }
        if(mLayoutManager == null){
            mLayoutManager = if (isRTLMode) {
                FlexboxLayoutManager(context, FlexDirection.ROW_REVERSE)
            } else {
                FlexboxLayoutManager(context, FlexDirection.ROW)
            }
        }
        mRvList.layoutManager = mLayoutManager
        mAdapter?.setData(mData,mFontSize)
        mIbPrePage.isEnabled = false
//        Log.d(TAG, "pageNext:2 "+ mAdapter!!.itemCount)
        updatePageBtnState()
    }


    fun setOnCandidateActionListener(listener: OnCandidateActionListener) {
        this.mListener = listener
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setThemeConfig()
        mIbPrePage.setOnClickListener(this)
        mIbNextPage.setOnClickListener(this)
        mIbBack.setOnClickListener(this)

        mLayoutManager = FlexboxLayoutManager(context)
        mRvList.layoutManager = mLayoutManager
        mRvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    return
                }
                val lastPosition = mLayoutManager!!.findLastVisibleItemPosition()
                if (lastPosition == mData.size - 1) {
                    Log.d(TAG, "onScrollStateChanged: 到底部了要更新")
                    postDelayed(Runnable {
                        mListener?.getMoreList()
                    },100)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.more_candidate_up -> {
                pagePre()
            }
            R.id.more_candidate_down -> {
                pageNext()
            }
            R.id.more_candidate_back -> {
                mData.clear()
                mListener?.onBack()
            }
        }
    }

    fun hideAndScroll2Top() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                post {
                    Log.d(TAG, "run: hideCandidate!!!")
                    mRvList.scrollToPosition(0)
                    visibility = GONE
                    mAdapter = null
                }
            }
        }, 50)
    }

    fun show(listener: OnCandidateActionListener){
        visibility = View.VISIBLE
        mListener = listener
        updatePageBtnState()
    }


    private fun setThemeConfig() {
        if (mPrePageDrawable != null && mNextPageDrawable != null){
            mRootView.setBackgroundColor(mBgColor)
            mIbPrePage.setImageDrawable(mPrePageDrawable)
            mIbNextPage.setImageDrawable(mNextPageDrawable)
            mIbBack.setImageDrawable(mBackDrawable)
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
        if (mAdapter!!.itemCount < lastPosition+16){
            mListener?.getMoreList()
            GlobalScope.launch(Dispatchers.Main){
                delay(200)
                mRvList.smoothScrollToPosition(lastPosition+16)
            }
        }else{
            mRvList.smoothScrollToPosition(lastPosition+16)
        }
    }

    /**
     * 更新翻页按钮状态
     */
    private fun updatePageBtnState() {
        val firstPosition = mLayoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
        val lastPosition = mLayoutManager?.findLastCompletelyVisibleItemPosition() ?: 0

        mIbPrePage.isEnabled = firstPosition > 0
        mIbNextPage.isEnabled = lastPosition < mData.size - 1
    }
}
package com.zqy.hci.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import androidx.appcompat.content.res.AppCompatResources
import com.zqy.hci.R
import com.zqy.hci.listener.OnCandidateActionListener
import com.zqy.hci.service.AudioAndHapticFeedbackManager
import com.zqy.hci.theme.ThemeAbleView
import com.zqy.hci.theme.UITheme
import com.zqy.hci.utils.LanguageUtil.KbRes.QWERTY_HWR_CN
import com.zqy.hci.utils.LanguageUtil.KbRes.QWERTY_HWR_EN
import kotlin.math.abs

/**
 * @Author:CWQ
 * @DATE:2022/1/15
 * @DESC:输入法顶部候选词View
 */
class CandidateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ThemeAbleView {
    private val TAG = CandidateView::class.java.simpleName

    /**
     * 展示候选词
     */
    private val DISPLAY_MODE_CAN = 1

    /**
     * 展示联想词
     */
    private val DISPLAY_MODE_ASS = 2

    /**
     * 未选择
     */
    private val NONE_SELECTED = -1

    /**
     * 是否是候选区文字靠左
     */
    private var mRTLMode = false
    private var mNeedShowMore = false

    /**
     * 展示模式 候选词 或 联想词
     */
    private var mDisplayMode = 0
    private var mSelectedIndex = NONE_SELECTED

    private val mPaint = Paint()
    private var mWidth = 0
    private var mHeight = 0
    private var mDataXGap = 0
    private var mBgColor = 0
    private var mCandidateTextColor = 0
    private var mCandidateTextSelectedColor = 0
    private var mFontSize = 0
    private var mVerticalTextPadding = 0
    private var mIconWidth = 0
    private var mCandidateOffX = 0
    private var mDivideWidth = 2
    private var mDividePadding = 15

    private lateinit var mMoreRightDrawable: Drawable
    private lateinit var mMoreLeftDrawable: Drawable
    private lateinit var mCloseDrawable: Drawable
    private lateinit var mClearDrawable: Drawable
    private lateinit var mHWNormalDrawable: Drawable
    private lateinit var mKeyboardNormalDrawable: Drawable
    private lateinit var mHWActiveDrawable: Drawable
    private lateinit var mKeyboardActiveDrawable: Drawable
    private lateinit var mNumberplateNormalDrawable: Drawable
    private lateinit var mNumberplateActiveDrawable: Drawable
    private lateinit var mEmojiNormalDrawable: Drawable
    private lateinit var mEmojiActiveDrawable: Drawable
    private lateinit var mHWDrawable: Drawable
    private lateinit var mKeyboardDrawable: Drawable
    private lateinit var mNumberplateDrawable: Drawable
    private lateinit var mEmojiDrawable: Drawable
    private lateinit var mDividerLeftDrawable: Drawable
    private lateinit var mDividerRightDrawable: Drawable

    private var mMoreIconArea: IconArea
    private var mClearIconArea: IconArea
    private var mCloseIconArea: IconArea
    private var mHWIconArea: IconArea
    private var mKeyboardIconArea: IconArea
    private var mNumberplateIconArea: IconArea
    private var mEmojiIconArea: IconArea
    private var mDividerLeftIconArea: IconArea
    private var mDividerRightIconArea: IconArea
    private var mRect = Rect()

    private val mDataPositions = arrayListOf<ResultInfo>()
    private val mData = arrayListOf<String>()
    private var mListener: OnCandidateActionListener? = null
    private val mVelocityTracker: VelocityTracker
    private var mTouchSlop = 0
    private var mMinFlingVelocity = 0
    private var mMaxFlingVelocity = 0
    private var mDownX = 0
    private var mDownY = 0
    private var mLastTouchX = 0
    private var mLastTouchY = 0
    private var mLastFlingX = 0
    private var mScroller: Scroller
    private val mKeyStateNormal = intArrayOf()
    private val mkeyStateActived = intArrayOf(
        android.R.attr.state_activated
    )

    /**
     * 更新候选词列表
     * @param data ArrayList<String>
     */
    fun setCandidateData(data: ArrayList<String>) {
        mDisplayMode = DISPLAY_MODE_CAN
        val isMoreData =
            !mData.isNullOrEmpty() && !data.isNullOrEmpty() && data.containsAll(mData) && data.size > mData.size
        if (!isMoreData) {
            mSelectedIndex = NONE_SELECTED
        }
        val upData = ArrayList(data)
        setCandidatePosition(upData, isMoreData)
        mData.clear()
        mData.addAll(upData)
        postInvalidate()
    }
    
    fun setOnCandidateActionListener(listener: OnCandidateActionListener) {
        mListener = listener
    }


    init {
        mMoreIconArea = IconArea()
        mCloseIconArea = IconArea()
        mClearIconArea = IconArea()
        mKeyboardIconArea = IconArea()
        mHWIconArea = IconArea()
        mNumberplateIconArea = IconArea()
        mEmojiIconArea = IconArea()
        mDividerLeftIconArea = IconArea()
        mDividerRightIconArea = IconArea()

        mPaint.apply {
            isAntiAlias = true
            textSize = mFontSize.toFloat()
            color = mCandidateTextColor
        }

        mScroller = Scroller(context)
        mVelocityTracker = VelocityTracker.obtain()
        val vc = ViewConfiguration.get(context)
        mTouchSlop = vc.scaledTouchSlop
        mMinFlingVelocity = vc.scaledMinimumFlingVelocity
        mMaxFlingVelocity = vc.scaledMaximumFlingVelocity
        val metrices: DisplayMetrics = resources.displayMetrics
        val dpi: Int = metrices.densityDpi
        val density: Float = metrices.density
        Log.d(TAG, "dpi is : $dpi  + density is : $density")

    }


    override fun setUITheme(uiTheme: UITheme) {
        val typeArray = uiTheme.context.obtainStyledAttributes(
            uiTheme.themeResId,
            R.styleable.HciKeyboardViewTheme
        )
        val bgId = typeArray.getResourceId(
            R.styleable.HciKeyboardViewTheme_candidateBgColor,
            R.color.baseCandidateBg
        )
        val bgColor = context.getColor(bgId)
        val textColor = typeArray.getColor(
            R.styleable.HciKeyboardViewTheme_candidateTextColor,
            context.getColor(R.color.baseCandidateText)
        )
        val selectedTextColor = typeArray.getColor(
            R.styleable.HciKeyboardViewTheme_candidateSelectedTextColor,
            context.getColor(R.color.baseCandidateSelectedText)
        )

        val fontSize = context.resources.getDimension(R.dimen.candidate_text_size)
        val dataXGap = context.resources.getDimension(R.dimen.baseCandidateDataXGap)

        val verticalTextPadding = typeArray.getDimension(
            R.styleable.HciKeyboardViewTheme_candidateVerticalTextPadding,
            context.resources.getDimension(R.dimen.baseCandidateVerticalPadding)
        )
        typeArray.recycle()

        val iconTypeArray = uiTheme.context.applicationContext.obtainStyledAttributes(
            uiTheme.iconThemeResId,
            R.styleable.HciKeyboardFunctionIconTheme
        )
        val moreId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_candidateMoreRightIcon,
            R.drawable.candidate_more
        )
        val moreRightDrawable = AppCompatResources.getDrawable(context,moreId)
        val moreLeftDrawable =
            iconTypeArray.getDrawable(R.styleable.HciKeyboardFunctionIconTheme_candidateMoreLeftIcon)
        val closeId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_candidateCloseIcon,
            R.drawable.candidate_close
        )
        val closeDrawable =
            AppCompatResources.getDrawable(context,closeId)
        val clearDrawable =
            iconTypeArray.getDrawable(R.styleable.HciKeyboardFunctionIconTheme_candidateClearIcon)
        val hwId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_candidateHWIcon,
            R.drawable.candidate_hw
        )
        val hwNormalDrawable =
            AppCompatResources.getDrawable(context,hwId)
        val hwActiveId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_candidateHWActiveIcon,
            R.drawable.candidate_hw_selected
        )
        val hwActiveDrawable =
            AppCompatResources.getDrawable(context,hwActiveId)
        val kbId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_candidateKeyboardIcon,
            R.drawable.candidate_keyboard
        )
        val keyboardNormalDrawable =
            AppCompatResources.getDrawable(context,kbId)
        val kbActiveId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_candidateKeyboardActiveIcon,
            R.drawable.candidate_keyboard_selected
        )
        val keyboardActiveDrawable =
            AppCompatResources.getDrawable(context,kbActiveId)
        val numberplateId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_candidateNumberplateIcon,
            R.drawable.candidate_numberplate
        )
        val numberplateDrawable =
            AppCompatResources.getDrawable(context,numberplateId)
        val numberplateActiveId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_candidateNumberplateActiveIcon,
            R.drawable.candidate_numberplate_selected
        )
        val numberplateActiveDrawable =
            AppCompatResources.getDrawable(context,numberplateActiveId)
        val emojiId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_candidateEmojiIcon,
            R.drawable.candidate_emoji
        )
        val emojiDrawable =
            AppCompatResources.getDrawable(context,emojiId)
        val emojiActiveId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_candidateEmojiActiveIcon,
            R.drawable.candidate_emoji_selected
        )
        val emojiActiveDrawable =
            AppCompatResources.getDrawable(context,emojiActiveId)
        val dlId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_candidateDividerLeftIcon,
            R.drawable.candidate_divider_left
        )
        val dividerLeftDrawable =
            AppCompatResources.getDrawable(context,dlId)
        val drId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_candidateDividerRightIcon,
            R.drawable.candidate_divider_right
        )
        val dividerRightDrawable =
            AppCompatResources.getDrawable(context,drId)
        iconTypeArray.recycle()

        mBgColor = bgColor
        mCandidateTextColor = textColor
        mCandidateTextSelectedColor = selectedTextColor

        mFontSize = fontSize.toInt()
        mDataXGap = dataXGap.toInt()
        mVerticalTextPadding = verticalTextPadding.toInt()

        if (moreRightDrawable != null) {
            mMoreRightDrawable = moreRightDrawable
        }
        if (moreLeftDrawable != null) {
            mMoreLeftDrawable = moreLeftDrawable
        }
        if (closeDrawable != null) {
            mCloseDrawable = closeDrawable
        }
        if (clearDrawable != null) {
            mClearDrawable = clearDrawable
        }
        if (hwNormalDrawable != null) {
            mHWNormalDrawable = hwNormalDrawable
            mHWDrawable = hwNormalDrawable
        }
        if (hwActiveDrawable != null) {
            mHWActiveDrawable = hwActiveDrawable
        }
        if (keyboardNormalDrawable != null) {
            mKeyboardNormalDrawable = keyboardNormalDrawable
            mKeyboardDrawable = keyboardNormalDrawable
        }
        if (keyboardActiveDrawable != null) {
            mKeyboardActiveDrawable = keyboardActiveDrawable
        }
        if (numberplateDrawable != null) {
            mNumberplateNormalDrawable = numberplateDrawable
            mNumberplateDrawable = mNumberplateNormalDrawable
        }
        if (numberplateActiveDrawable != null) {
            mNumberplateActiveDrawable = numberplateActiveDrawable
        }
        if (emojiDrawable != null) {
            mEmojiNormalDrawable = emojiDrawable
            mEmojiDrawable = mEmojiNormalDrawable
        }
        if (emojiActiveDrawable != null) {
            mEmojiActiveDrawable = emojiActiveDrawable
        }
        if (dividerLeftDrawable != null) {
            mDividerLeftDrawable = dividerLeftDrawable
        }
        if (dividerRightDrawable != null) {
            mDividerRightDrawable = dividerRightDrawable
        }

        mPaint.apply {
            isAntiAlias = true
            textSize = mFontSize.toFloat()
            color = mCandidateTextColor
        }
    }

    fun isRTLMode() = mRTLMode

    fun setRTLMode(mode: Boolean, name: String) {
        mRTLMode = mode
        if (mRTLMode) mDataXGap =
            context.resources.getDimension(R.dimen.rtlCandidateDataXGap).toInt()
        if (name == QWERTY_HWR_CN || name == QWERTY_HWR_EN) {
            mHWDrawable = mHWActiveDrawable
            mKeyboardDrawable = mKeyboardNormalDrawable
            mNumberplateDrawable = mNumberplateNormalDrawable
            mEmojiDrawable = mEmojiNormalDrawable
        } else {
            mHWDrawable = mHWNormalDrawable
            mKeyboardDrawable = mKeyboardActiveDrawable
            mNumberplateDrawable = mNumberplateNormalDrawable
            mEmojiDrawable = mEmojiNormalDrawable
        }
        setIconRect()
        postInvalidate()
    }

    fun setEmojiMode() {
        mHWDrawable = mHWNormalDrawable
        mKeyboardDrawable = mKeyboardNormalDrawable
        mNumberplateDrawable = mNumberplateNormalDrawable
        mEmojiDrawable = mEmojiActiveDrawable
        setIconRect()
        postInvalidate()
    }

    fun setNumberplateMode() {
        mHWDrawable = mHWNormalDrawable
        mKeyboardDrawable = mKeyboardNormalDrawable
        mNumberplateDrawable = mNumberplateActiveDrawable
        mEmojiDrawable = mEmojiNormalDrawable
        setIconRect()
        postInvalidate()
    }

    fun getData(): ArrayList<String> {
        return ArrayList(mData)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)

        val h = if (heightSpecMode == MeasureSpec.AT_MOST) {
            mFontSize + mVerticalTextPadding * 2
        } else {
            mVerticalTextPadding = (heightSpecSize - mFontSize) / 2
            heightSpecSize
        }
        setMeasuredDimension(widthSpecSize, h)
        mWidth = measuredWidth
        mHeight = measuredHeight

        mIconWidth = context.resources.getDimension(R.dimen.candidate_icon_width).toInt()
        setIconRect()
    }


    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(mBgColor)
        if (!mData.isNullOrEmpty()) {
            drawData(canvas)
            drawIcon(canvas)
        } else {
            if (mCloseIconArea.iconDrawable != null) {
                mCloseIconArea.draw(canvas)
            }
            if (mHWIconArea.iconDrawable != null) {
                mHWIconArea.draw(canvas)
            }
            if (mKeyboardIconArea.iconDrawable != null) {
                mKeyboardIconArea.draw(canvas)
            }
            if (mNumberplateIconArea.iconDrawable != null) {
                mNumberplateIconArea.draw(canvas)
            }
            if (mEmojiIconArea.iconDrawable != null) {
                mEmojiIconArea.draw(canvas)
            }
            if (mDividerLeftIconArea.iconDrawable != null) {
                mDividerLeftIconArea.draw(canvas)
            }
        }
    }


    var notifiedListener = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var eventAddedToVelocityTracker = false
        notifiedListener = false
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!mScroller.isFinished) {
                    mScroller.abortAnimation()
                }
                mDownX = (event.x + 0.5f).toInt()
                mDownY = (event.y + 0.5f).toInt()
                mLastTouchX = mDownX
                mLastTouchY = mDownY
                if (downInData(mDownX) && !mDataPositions.isNullOrEmpty()) {
                    for (index in 0 until mDataPositions.size) {
                        mDataPositions[index].setRect(mRect)
                        if (mRect.contains(mDownX, mDownY)) {
                            mSelectedIndex = index
                            postInvalidate()
                            break
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val curX = (event.x + 0.5f).toInt()
                val dx = curX - mLastTouchX
                if (downInData(mDownX)) {
                    mCandidateOffX += dx
                    handleOffXRange()
                    invalidate()
                }
                mLastTouchX = curX
            }
            MotionEvent.ACTION_UP -> {
                val curX = (event.x + 0.5f).toInt()
                val curY = (event.y + 0.5f).toInt()
                val dx = curX - mDownX
                if (abs(dx) <= mTouchSlop) {//点击
                    if (!mData.isNullOrEmpty() && !mDataPositions.isNullOrEmpty()) {
                        if (downInData(curX)) {//点击在数据区
                            for (index in 0 until mDataPositions.size) {
                                mDataPositions[index].setRect(mRect)
                                if (mRect.contains(curX, curY)) {
                                    mListener?.onCandidateSelected(
                                        index,
                                        mDataPositions[index].displayString
                                    )
                                    playKeySoundEffect()
                                    break
                                }
                            }
                        } else {//点击在按钮区
                            if (mDisplayMode == DISPLAY_MODE_CAN && mMoreIconArea.contains(
                                    curX,
                                    curY
                                )
                            ) {
                                if (mNeedShowMore) {
                                    mListener?.onMore()
                                    playKeySoundEffect()
                                }
                            } else if (mDisplayMode == DISPLAY_MODE_ASS && mClearIconArea.contains(
                                    curX,
                                    curY
                                )
                            ) {
                                mListener?.onClear()
                                playKeySoundEffect()
                            } else if (mCloseIconArea.contains(curX, curY)) {
                                mListener?.onClose()
                                playKeySoundEffect()
                            }
                        }
                    } else {
                        if (mCloseIconArea.contains(curX, curY)) {
                            mListener?.onClose()
                            playKeySoundEffect()
                        }
                        if (mHWIconArea.contains(curX, curY)) {
                            mListener?.onHW()
                            playKeySoundEffect()
                        }
                        if (mKeyboardIconArea.contains(curX, curY)) {
                            mListener?.onKeyboard()
                            playKeySoundEffect()
                        }
                        if (mNumberplateIconArea.contains(curX, curY)) {
                            mListener?.onNumberPlate()
                            playKeySoundEffect()
                        }
                        if (mEmojiIconArea.contains(curX, curY)) {
                            mListener?.onEmoji()
                            playKeySoundEffect()
                        }
                    }
                } else {
                    mVelocityTracker.addMovement(event)
                    eventAddedToVelocityTracker = true
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity.toFloat())
                    var xVelocity = mVelocityTracker.xVelocity
                    xVelocity = if (abs(xVelocity) < mMinFlingVelocity) {
                        0f
                    } else {
                        Math.max(
                            -mMaxFlingVelocity.toFloat(),
                            Math.min(xVelocity, mMaxFlingVelocity.toFloat())
                        )
                    }
                    if (xVelocity != 0f) {
                        mLastFlingX = 0
                        mScroller.fling(
                            0,
                            0,
                            xVelocity.toInt(),
                            0,
                            Int.MIN_VALUE,
                            Int.MAX_VALUE,
                            Int.MIN_VALUE,
                            Int.MAX_VALUE
                        )
                        postInvalidate()
                    }
                    mVelocityTracker.clear()
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                mVelocityTracker.clear()
            }
        }
        if (!eventAddedToVelocityTracker) {
            mVelocityTracker.addMovement(event)
        }
        return true
    }

    override fun computeScroll() {
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mVelocityTracker.recycle()
    }

    /**
     * 处理偏移量边界问题
     */
    private fun handleOffXRange() {
        try {
            if (mRTLMode) {
                if (mDataPositions.last().x - mDataPositions.last().width + mDataXGap / 2 + mCandidateOffX > mMoreIconArea.right + mDivideWidth) {
                    //左边界
                    mCandidateOffX =
                        mMoreIconArea.right + mDivideWidth - (mDataPositions.last().x - mDataPositions.last().width + mDataXGap / 2)
                    if (!notifiedListener) {
                        notifiedListener = true
                        mListener?.getMoreList()
                    }
                }
                if (mCandidateOffX < 0) {
                    //右边界
                    mCandidateOffX = 0
                }
            } else {
                if (mCandidateOffX > 0) {
                    //左边界
                    mCandidateOffX = 0
                } else if (mDataPositions.last().x + mDataPositions.last().width < mMoreIconArea.left - mDivideWidth) {
                    mCandidateOffX = 0
                } else {
                    if (mDataPositions.last().x + mDataPositions.last().width + mDataXGap / 2 + mCandidateOffX < mMoreIconArea.left - mDivideWidth) {
                        //右边界
//                        Log.d(TAG, "handleOffXRange:右边界")
                        mCandidateOffX =
                            mMoreIconArea.left - mDivideWidth - (mDataPositions.last().x + mDataPositions.last().width + mDataXGap / 2)
                    }
                    if (!notifiedListener) {
                        notifiedListener = true
                        mListener?.getMoreList()
                    }
//                    Log.d(
//                        TAG,
//                        "handleOffXRange: mDataPositions.last().x is ${mDataPositions.last().x}\n mDataPositions.last().width is ${mDataPositions.last().width} \n mDataXGap / 2 is ${mDataXGap / 2} \n mCandidateOffX is ${mCandidateOffX} \n mMoreIconArea.left - mDivideWidth is ${mMoreIconArea.left - mDivideWidth}"
//                    )
                }
            }
        } catch (e: NoSuchElementException) {
            Log.d(TAG, "onTouchEvent: no candidate item")
        }
    }


    /**
     * 画候选词、联想词
     * @param canvas Canvas
     */
    private fun drawData(canvas: Canvas) {
        canvas.save()

        if (mRTLMode) {
            canvas.clipRect(
                mMoreIconArea.right + mDivideWidth,
                0,
                mWidth - paddingRight,
                mHeight
            )
        } else {
            canvas.clipRect(
                paddingLeft,
                0,
                mDividerRightIconArea.right - ((mDividerRightIconArea.right - mDividerRightIconArea.left) * 0.6).toInt(),
                mHeight
            )
        }
        val drawData = ArrayList(mDataPositions)
        if (!drawData.isNullOrEmpty()) {
            for (index in 0 until drawData.size) {
                val resultInfo = drawData[index]
                //过滤掉显示区域外数据
                if (mRTLMode) {
                    if (((resultInfo.x - resultInfo.width + mDataXGap + mCandidateOffX) > (mWidth - paddingRight)) || resultInfo.x + mCandidateOffX < mMoreIconArea.left + mDivideWidth) {
                        continue
                    }
                } else {
                    if ((resultInfo.x + resultInfo.width - mCandidateOffX) < paddingLeft || resultInfo.x + mCandidateOffX > mMoreIconArea.left + mDivideWidth || resultInfo.x + mCandidateOffX < paddingLeft) {
                        continue
                    }
                }

                if (mSelectedIndex == NONE_SELECTED && index == 0) {
                    mPaint.color = mCandidateTextSelectedColor
                } else if (mSelectedIndex != NONE_SELECTED && mSelectedIndex == index) {
                    mPaint.color = mCandidateTextSelectedColor
                } else {
                    mPaint.color = mCandidateTextColor
                }
                if (mRTLMode) {
                    canvas.drawText(
                        resultInfo.displayString,
                        (resultInfo.x - resultInfo.width + mDataXGap + mCandidateOffX).toFloat(),
                        resultInfo.y.toFloat(), mPaint
                    )
                } else {
                    canvas.drawText(
                        resultInfo.displayString, (resultInfo.x + mCandidateOffX).toFloat(),
                        resultInfo.y.toFloat(), mPaint
                    )
                }
            }
        }
        canvas.restore()
    }

    private fun drawIcon(canvas: Canvas) {
        if (mNeedShowMore) {
            if (mDisplayMode == DISPLAY_MODE_CAN && mMoreIconArea.iconDrawable != null) {
                mMoreIconArea.draw(canvas)
                mDividerRightIconArea.draw(canvas)
            } else if (mDisplayMode == DISPLAY_MODE_ASS && mClearIconArea.iconDrawable != null) {
                mClearIconArea.draw(canvas)
            }
        }
    }

    /**
     * 设置词条位置数据
     * @param data ArrayList<String>
     */
    private fun setCandidatePosition(data: ArrayList<String>, isMoreData: Boolean = false) {
        mDataPositions.clear()
        if (data.isNullOrEmpty()) {
            mSelectedIndex = 0
            mCandidateOffX = 0
            return
        }
        if (!isMoreData) {
            mCandidateOffX = 0
        }
        var resultInfo: ResultInfo?
        var x = if (mRTLMode) {
            mWidth - paddingRight
        } else {
            paddingLeft
        }
        val y = ((mHeight - mPaint.textSize) / 2 - mPaint.ascent()).toInt()

        data.forEach { word ->
            val wordWidth = mPaint.measureText(word).toInt()
            resultInfo = ResultInfo().apply {
                this.displayString = word
                this.x = x
                this.y = y
                this.width = wordWidth + mDataXGap
            }
            mDataPositions.add(resultInfo!!)

            if (mRTLMode) {
                x -= resultInfo!!.width
            } else {
                x += resultInfo!!.width + mDataXGap
            }
        }

        //是否需要显示更多
        mNeedShowMore = if (mRTLMode) {
            mDataPositions.last().x - mDataPositions.last().width - mDataXGap / 2 < mMoreIconArea.right
        } else {
            mDataPositions.last().x + mDataPositions.last().width - mDataXGap / 2 >= mMoreIconArea.left
        }
    }

    /**
     * 设置图标数据
     */
    private fun setIconRect() {
        if (mRTLMode) {
            mMoreIconArea.reset(
                paddingLeft,
                0,
                paddingLeft + mIconWidth,
                mHeight,
                mMoreLeftDrawable
            )
            mCloseIconArea.reset(
                paddingLeft,
                0,
                paddingLeft + mIconWidth,
                mHeight,
                mCloseDrawable
            )
            mClearIconArea.reset(
                paddingLeft,
                0,
                paddingLeft + mIconWidth,
                mHeight,
                mClearDrawable
            )
        } else {
            mMoreIconArea.reset(
                (mWidth - paddingRight - mIconWidth * 1.5).toInt(),
                0,
                mWidth - paddingRight,
                mHeight,
                mMoreRightDrawable
            )
            mCloseIconArea.reset(
                paddingLeft,
                0,
                paddingLeft + mIconWidth,
                mHeight,
                mCloseDrawable
            )
            mClearIconArea.reset(
                mWidth - paddingRight - mIconWidth,
                0,
                mWidth - paddingRight,
                mHeight,
                mClearDrawable
            )
            mHWIconArea.reset(
                paddingLeft + mIconWidth * 3,
                0,
                paddingLeft + mIconWidth * 4,
                mHeight,
                mHWDrawable
            )
            mKeyboardIconArea.reset(
                paddingLeft + mIconWidth * 2,
                0,
                paddingLeft + mIconWidth * 3,
                mHeight,
                mKeyboardDrawable
            )
            mEmojiIconArea.reset(
                paddingLeft + mIconWidth * 4,
                0,
                paddingLeft + mIconWidth * 5,
                mHeight,
                mEmojiDrawable
            )
            mNumberplateIconArea.reset(
                paddingLeft + mIconWidth * 5,
                0,
                paddingLeft + mIconWidth * 6,
                mHeight,
                mNumberplateDrawable
            )
            mDividerLeftIconArea.reset(
                paddingLeft + mIconWidth,
                0,
                paddingLeft + mIconWidth * 2,
                mHeight,
                mDividerLeftDrawable
            )
            mDividerRightIconArea.reset(
                mWidth - paddingRight - mIconWidth * 2,
                0,
                (mWidth - paddingRight - mIconWidth * 1.5).toInt(),
                mHeight,
                mDividerRightDrawable
            )
        }
    }

    /**
     * 是否点击在数据区
     * @param x Int
     * @return Boolean
     */
    private fun downInData(x: Int) = if (mRTLMode) {
        x > mMoreIconArea.right + mDivideWidth
    } else {
        x < mDividerRightIconArea.left
    }

    /**
     * 播放点击音效
     */
    private fun playKeySoundEffect() {
        AudioAndHapticFeedbackManager.getInstance().performAudioFeedback(0)
    }


    inner class ResultInfo {
        var displayString: String = ""
        var x: Int = 0
        var y: Int = 0
        var width: Int = 0

        fun setRect(rect: Rect) {
            if (mRTLMode) {
                rect.set(
                    x - width + mDataXGap / 2 + mCandidateOffX,
                    1,
                    x + mDataXGap / 2 + mCandidateOffX,
                    mHeight - 1
                )
            } else {
                rect.set(
                    x - mDataXGap / 2 + mCandidateOffX,
                    1,
                    x + width - mDataXGap / 2 + mCandidateOffX,
                    mHeight - 1
                )
            }
        }
    }

    inner class IconArea {
        var left: Int = 0
        var top: Int = 0
        var right: Int = 0
        var bottom: Int = 0
        val iconRect = Rect()
        var iconDrawable: Drawable? = null

        fun reset(left: Int, top: Int, right: Int, bottom: Int, drawable: Drawable) {
            this.left = left
            this.top = top
            this.right = right
            this.bottom = bottom
            this.iconRect.set(left, top, right, bottom)
            this.iconDrawable = drawable
        }

        fun contains(x: Int, y: Int) = iconRect.contains(x, y)

        fun draw(canvas: Canvas) {
            iconDrawable?.run {
                val width = right - left
                val height = bottom - top

                val leftMargin = (width - this.intrinsicWidth) / 2
                val topMargin = (height - this.intrinsicHeight) / 2

                val iconRect = Rect(
                    left + leftMargin,
                    top + topMargin,
                    left + leftMargin + this.intrinsicWidth,
                    top + topMargin + this.intrinsicHeight
                )
                this.bounds = iconRect
                this.draw(canvas)
            }
        }
    }
}
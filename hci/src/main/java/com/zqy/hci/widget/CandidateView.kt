package com.zqy.hci.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import com.zqy.hci.R
import com.zqy.hci.listener.OnCandidateActionListener
import com.zqy.hci.service.AudioAndHapticFeedbackManager
import com.zqy.hci.theme.ThemeAbleView
import com.zqy.hci.theme.UITheme
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

    private var mMoreIconArea: IconArea
    private var mClearIconArea: IconArea
    private var mCloseIconArea: IconArea
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

    /**
     * 更新候选词列表
     * @param data ArrayList<String>
     */
    fun setCandidateData(data: ArrayList<String>) {
        mDisplayMode = DISPLAY_MODE_CAN
        val isMoreData =
            mData.isNotEmpty() && data.isNotEmpty() && data.containsAll(mData) && data.size > mData.size
        if (!isMoreData) {
            mSelectedIndex = NONE_SELECTED
        }
        setCandidatePosition(data, isMoreData)
        mData.clear()
        mData.addAll(data)
        invalidate()
    }

    /**
     * 更新联想词列表
     * @param data ArrayList<String>
     */
    fun setAssociateData(data: ArrayList<String>) {
        mDisplayMode = DISPLAY_MODE_ASS
        mSelectedIndex = NONE_SELECTED
        mData.clear()
        mData.addAll(data)
        setCandidatePosition(data)
        invalidate()
    }


    fun setOnCandidateActionListener(listener: OnCandidateActionListener) {
        mListener = listener
    }


    init {
        mMoreIconArea = IconArea()
        mCloseIconArea = IconArea()
        mClearIconArea = IconArea()

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
    }


    override fun setUITheme(uiTheme: UITheme) {
        val typeArray = uiTheme.context.applicationContext.obtainStyledAttributes(
            uiTheme.themeResId,
            R.styleable.HciKeyboardViewTheme
        )
        val bgColor =
            typeArray.getColor(
                R.styleable.HciKeyboardViewTheme_candidateBgColor,
                context.getColor(R.color.baseCandidateBg)
            )
        val textColor = typeArray.getColor(
            R.styleable.HciKeyboardViewTheme_candidateTextColor,
            context.getColor(R.color.baseCandidateText)
        )
        val selectedTextColor = typeArray.getColor(
            R.styleable.HciKeyboardViewTheme_candidateSelectedTextColor,
            context.getColor(R.color.baseCandidateSelectedText)
        )

        val fontSize = typeArray.getDimension(
            R.styleable.HciKeyboardViewTheme_candidateFontSize,
            context.resources.getDimension(R.dimen.baseCandidateTextSize)
        )
        val dataXGap = typeArray.getDimension(
            R.styleable.HciKeyboardViewTheme_candidateDataXGap,
            context.resources.getDimension(R.dimen.baseCandidateDataXGap)
        )
        val verticalTextPadding = typeArray.getDimension(
            R.styleable.HciKeyboardViewTheme_candidateVerticalTextPadding,
            context.resources.getDimension(R.dimen.baseCandidateVerticalPadding)
        )
        typeArray.recycle()

        val iconTypeArray = uiTheme.context.applicationContext.obtainStyledAttributes(
            uiTheme.iconThemeResId,
            R.styleable.HciKeyboardFunctionIconTheme
        )
        val moreRightDrawable =
            iconTypeArray.getDrawable(R.styleable.HciKeyboardFunctionIconTheme_candidateMoreRightIcon)
        val moreLeftDrawable =
            iconTypeArray.getDrawable(R.styleable.HciKeyboardFunctionIconTheme_candidateMoreLeftIcon)
        val closeDrawable =
            iconTypeArray.getDrawable(R.styleable.HciKeyboardFunctionIconTheme_candidateCloseIcon)
        val clearDrawable =
            iconTypeArray.getDrawable(R.styleable.HciKeyboardFunctionIconTheme_candidateClearIcon)
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

        mPaint.apply {
            isAntiAlias = true
            textSize = mFontSize.toFloat()
            color = mCandidateTextColor
        }

        invalidate()
    }

    fun isRTLMode() = mRTLMode

    fun setRTLMode(mode: Boolean) {
        mRTLMode = mode
        if(mRTLMode) mDataXGap = context.resources.getDimension(R.dimen.rtlCandidateDataXGap).toInt()
        setIconRect()
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

        mIconWidth = mWidth / 20
        setIconRect()
    }


    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(mBgColor)
        if (mData.isNotEmpty()) {
            drawData(canvas)
            drawIcon(canvas)
        } else {
            if (mCloseIconArea.iconDrawable != null) {
                mCloseIconArea.draw(canvas)
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
                if (downInData(mDownX) && mDataPositions.isNotEmpty()) {
                    for (index in 0 until mDataPositions.size) {
                        mDataPositions[index].setRect(mRect)
                        if (mRect.contains(mDownX, mDownY)) {
                            mSelectedIndex = index
                            invalidate()
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
                    if (mData.isNotEmpty()) {
                        if (downInData(curX)) {//点击在数据区
                            for (index in 0 until mDataPositions.size) {
                                mDataPositions[index].setRect(mRect)
                                if (mRect.contains(curX, curY)) {
                                    mListener?.onCandidateSelected(
                                        index,
                                        mDataPositions[index].displayString
                                    )
                                    break
                                }
                            }
                        } else {//点击在按钮区
                            if (mDisplayMode == DISPLAY_MODE_CAN && mMoreIconArea.contains(
                                    curX,
                                    curY
                                )
                            ) {
                                if (mNeedShowMore) mListener?.onMore()
                            } else if (mDisplayMode == DISPLAY_MODE_ASS && mClearIconArea.contains(
                                    curX,
                                    curY
                                )
                            ) {
                                mListener?.onClear()
                            } else if (mCloseIconArea.contains(curX, curY)) {
                                mListener?.onClose()
                            }
                        }
                    } else {
                        if (mCloseIconArea.contains(curX, curY)) {
                            mListener?.onClose()
                        }
                    }
                    playKeySoundEffect()
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
                        invalidate()
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
        if (mScroller.computeScrollOffset()) {
            val x = mScroller.currX
            val dx = x - mLastFlingX
            mLastFlingX = x
            mCandidateOffX += dx
            handleOffXRange()
            postInvalidate()
        }
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
                } else if (mDataPositions.last().x + mDataPositions.last().width - mDataXGap / 2 + mCandidateOffX < mMoreIconArea.left) {
                    mCandidateOffX = 0
                } else {
                    if (mDataPositions.last().x + mDataPositions.last().width - mDataXGap / 2 + mCandidateOffX < mMoreIconArea.left - mDivideWidth) {
                        //右边界
                        mCandidateOffX =
                            mMoreIconArea.left - mDivideWidth - (mDataPositions.last().x + mDataPositions.last().width - mDataXGap / 2)
                        if (!notifiedListener) {
                            notifiedListener = true
                            mListener?.getMoreList()
                        }
                    }
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
                mWidth - (mMoreIconArea.right - mMoreIconArea.left) - paddingRight,
                mHeight
            )
        }

        for (index in 0 until mDataPositions.size) {
            val resultInfo = mDataPositions[index]
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
            Log.d(TAG, "drawData: "+mDataXGap)
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

        canvas.restore()
    }

    private fun drawIcon(canvas: Canvas) {
        if (mNeedShowMore) {
            if (mDisplayMode == DISPLAY_MODE_CAN && mMoreIconArea.iconDrawable != null) {
                mMoreIconArea.draw(canvas)
            } else if (mDisplayMode == DISPLAY_MODE_ASS && mClearIconArea.iconDrawable != null) {
                mClearIconArea.draw(canvas)
            }
            //分割线
            mPaint.color = mCandidateTextColor
            if (mRTLMode) {
                mRect.set(
                    mMoreIconArea.right,
                    mDividePadding,
                    mMoreIconArea.right + mDivideWidth,
                    mHeight - mDividePadding
                )
            } else {
                mRect.set(
                    mMoreIconArea.left - mDivideWidth,
                    mDividePadding,
                    mMoreIconArea.left,
                    mHeight - mDividePadding
                )
            }
            canvas.drawRect(mRect, mPaint)
        }
    }

    /**
     * 设置词条位置数据
     * @param data ArrayList<String>
     */
    private fun setCandidatePosition(data: ArrayList<String>, isMoreData: Boolean = false) {
        mDataPositions.clear()
        if (data.isEmpty()) {
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
                mWidth - paddingRight - mIconWidth,
                0,
                mWidth - paddingRight,
                mHeight,
                mMoreRightDrawable
            )
            mCloseIconArea.reset(
                mWidth - paddingRight - mIconWidth,
                0,
                mWidth - paddingRight,
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
        x < mMoreIconArea.left - mDivideWidth
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
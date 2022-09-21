package com.zqy.hci.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zqy.hci.R
import com.zqy.hci.handwrite.OnStrokeActionListener
import com.zqy.hci.handwrite.StrokeData
import com.zqy.hci.theme.ThemeAbleView
import com.zqy.hci.theme.UITheme
import kotlin.math.abs

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.hci.widget
 */
class StrokeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ThemeAbleView {
    private inner class PathInfo(var path: Path)
    private lateinit var pathInfos: MutableList<PathInfo>
    private lateinit var mStrokePath: Path
    private var mStrokePaint: Paint? = null
    private var mStrokePointList: ArrayList<Point>? = null
    private var strokeViewWidth = 0
    private var strokeViewHeight = 0
    private var downX = 0
    private var downY = 0
    private var strokeWidth = 0f
    private var strokeColor = 0
    private var strokeViewBg = 0
    private val fadeColorAlpha = intArrayOf(
        0xCC,
        0xA6,
        0x80,
        0x66,
        0x59,
        0x4A,
        0x3D,
        0x33,
        0x29,
        0x21,
        0x1C,
        0x17,
        0x12,
        0x0D,
        0x08,
        0x03,
        0x00
    )
    private var isHandwriting = false
    private var mPrevX = 0f
    private var mPrevY = 0f
    private val mCurrentMode = 0
    private val drawPointIndex: MutableList<Int> = java.util.ArrayList()
    private lateinit var mListener: OnStrokeActionListener
    private lateinit var strokeColors: IntArray
    private lateinit var mStrokeData: StrokeData
    private var mInputArea: Rect? = null
    private var mInputAreaView: View? = null
    private var isInit = false

    fun initView(keyboard: KeyboardView) {
        mStrokePath = Path()
        pathInfos = ArrayList()
        mStrokePointList = ArrayList()
        strokePaintInit()
        mStrokeData = StrokeData()
        mInputAreaView = keyboard
        setBackgroundColor(context.getColor(R.color.strokeViewBg))
        isInit = true
    }

    fun setOnStrokeActionListener(listener: OnStrokeActionListener) {
        mListener = listener
    }

    fun destroyView() {
        if (isInit){
            clear()
        }
        mInputAreaView = null
        mInputArea = null
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //首次触摸确认当前键盘区域
        if (mInputArea == null) {
            mInputArea = Rect()
            mInputAreaView?.getLocalVisibleRect(mInputArea)
//            if (mCurrentMode == StrokeViewContainer.WINDOW_MODE) {
            val cutoff = mInputArea!!.width() * 10 / 100
            mInputArea = Rect(
                mInputArea!!.left + cutoff,
                mInputArea!!.top,
                mInputArea!!.right - cutoff,
                mInputArea!!.bottom
            )
//            }
        }

        //根据起笔区域是否为键盘区域，确定是否处理触摸事件
//        if (mCurrentMode == StrokeViewContainer.WINDOW_MODE) {
        if (!mInputArea!!.contains(event!!.x.toInt(), event.y.toInt()) && !isHandwriting) {
            return false
        }
//        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> handleTouchDown(event)
            MotionEvent.ACTION_MOVE -> handleTouchMove(event)
            MotionEvent.ACTION_UP -> handleTouchUp(event)
        }
        return true
    }

    override fun setUITheme(uiTheme: UITheme) {
        val typeArray = uiTheme.context.applicationContext.obtainStyledAttributes(
            uiTheme.themeResId,
            R.styleable.HciKeyboardViewTheme
        )
        strokeWidth = typeArray.getFloat(
            R.styleable.HciKeyboardViewTheme_paintWidth,
            context.resources.getInteger(R.integer.strokeWidth).toFloat()
        )
        strokeColor = typeArray.getColor(
            R.styleable.HciKeyboardViewTheme_strokeColor,
            context.getColor(R.color.strokeColor)
        )
        strokeViewBg = typeArray.getColor(
            R.styleable.HciKeyboardViewTheme_strokeViewBg,
            context.getColor(R.color.strokeViewBg)
        )
        typeArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawStroke(canvas)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (mInputAreaView == null) return
        mInputAreaView?.post { requestLayout() }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        if (mCurrentMode == StrokeViewContainer.WINDOW_MODE) {
        strokeViewWidth = mInputAreaView?.measuredWidth!!
        strokeViewHeight = mInputAreaView?.measuredHeight!!
        setMeasuredDimension(strokeViewWidth, strokeViewHeight)
//        } else if (mCurrentMode == FULLSCREEN_MODE) {
//            val defDis = display
//            strokeViewWidth = defDis.width
//            strokeViewHeight = defDis.height
//            setMeasuredDimension(mWidth, mHeight)
//        }
    }

    private fun handleTouchDown(event: MotionEvent) {
        isHandwriting = true
        stopUpTimer()
        val currentX = event.x
        val currentY = event.y
        mStrokeData.addStroke(currentX.toInt().toShort(), currentY.toInt().toShort())
        mPrevX = currentX
        mPrevY = currentY
        mStrokePath.moveTo(currentX, currentY)
        downX = currentX.toInt()
        downY = currentY.toInt()
        mStrokePointList!!.add(Point(downX, downY))
        invalidate()
    }

    private fun handleTouchMove(event: MotionEvent) {
        val currentX = event.x
        val currentY = event.y
        mStrokeData.addStroke(currentX.toInt().toShort(), currentY.toInt().toShort())
        val dx = abs(currentX - mPrevX)
        val dy = abs(currentY - mPrevY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mStrokePath.quadTo(mPrevX, mPrevY, (currentX + mPrevX) / 2, (currentY + mPrevY) / 2)
            mPrevX = currentX
            mPrevY = currentY
        }
        invalidate()
    }

    private fun handleTouchUp(event: MotionEvent) {
        mStrokeData.addTouchUpStroke()
        val currentX = event.x.toInt()
        val currentY = event.y.toInt()
        if (currentX == downX && currentY == downY) {
            drawPointIndex.add(mStrokePointList!!.size - 1)
        }
        pathInfos.add(PathInfo(mStrokePath))
        mStrokePath = Path()
        invalidate()
//        if (mStrokeData
//                .getStrokeLength() < MIN_POINT_STROKE && mCurrentMode == FULLSCREEN_MODE
//        ) {
//            mStrokeData.addEndStroke()
//            mListener.onPointTouch()
//            clear()
//        } else {
        startUpTimer()
//        }
    }

    private fun strokePaintInit() {
        mStrokePaint = Paint()
        mStrokePaint!!.isAntiAlias = true
        mStrokePaint!!.isDither = true
        mStrokePaint!!.style = Paint.Style.STROKE
        mStrokePaint!!.strokeCap = Paint.Cap.ROUND
        mStrokePaint!!.strokeJoin = Paint.Join.ROUND
        mStrokePaint!!.strokeWidth = strokeWidth
        mStrokePaint!!.color = strokeColor
        setColorAlpha(Color.WHITE)
    }

    private fun setColorAlpha(strokeColor: Int) {
        var strokeColor = strokeColor
        strokeColor = strokeColor and 0x00FFFFFF
        strokeColors = IntArray(fadeColorAlpha.size)
        for (i in fadeColorAlpha.indices) {
            strokeColors[i] = strokeColor or (fadeColorAlpha[i] shl 24)
        }
    }

    /**
     * 停止超时计时
     */
    private fun stopUpTimer() {
        strokeHandler.removeMessages(TIME_UP_MSG)
    }

    /**
     * 开始抬笔计时
     */
    private fun startUpTimer() {
        strokeHandler.removeMessages(TIME_UP_MSG)
        strokeHandler.sendMessageDelayed(
            strokeHandler.obtainMessage(TIME_UP_MSG),
            DELAY_MILLIS.toLong()
        )
    }

    private val strokeHandler = Handler(Looper.getMainLooper(), object : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                TIME_UP_MSG -> {
                    mStrokeData.addEndStroke()
                    run {
                        mListener.onWriteEnd(mStrokeData.getStroke())
//                        if (mCurrentMode == FULLSCREEN_MODE) {
//                            mContainer.clipFullWindowDefault()
//                        }
                    }
                    clear()
                }
            }
            return false
        }
    })

    private fun drawStroke(canvas: Canvas) {
        //画历史的路径
        var index = 0
        for (i in pathInfos.indices.reversed()) {
            mStrokePaint!!.color = strokeColors[index]
            canvas.drawPath(pathInfos[i].path, mStrokePaint!!)
            if (index < fadeColorAlpha.size - 1) {
                index++
            }
            //画点
            if (drawPointIndex.contains(i) && i < mStrokePointList!!.size) {
                val p = mStrokePointList!![i]
                canvas.drawPoint(p.x.toFloat(), p.y.toFloat(), mStrokePaint!!)
            }
        }
        //画最后的一笔
        mStrokePaint!!.color = strokeColor
        canvas.drawPath(mStrokePath, mStrokePaint!!)
    }

    /**
     * 清理笔迹，复位
     */
    private fun clear() {
        isHandwriting = false
        mStrokePointList?.clear()
        mStrokePath.reset()
        pathInfos.clear()
        mStrokeData.resetStroke()
        invalidate()
    }

    companion object {
        private const val TAG = "StrokeView"
        private const val TOUCH_TOLERANCE = 4

        //识别正常操作的最少点数，少于改值则识别为点操作
        private const val MIN_POINT_STROKE = 12

        // 抬笔超时FLAG
        private const val TIME_UP_MSG = 1

        // 抬笔超时毫秒数
        private const val DELAY_MILLIS = 500
    }
}
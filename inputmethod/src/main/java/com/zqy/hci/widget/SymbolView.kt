package com.zqy.hci.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.zqy.hci.R
import com.zqy.hci.adapter.SymbolAdapter
import com.zqy.hci.adapter.SymbolTitleAdapter
import com.zqy.hci.listener.OnSymbolActionListener
import com.zqy.hci.service.AudioAndHapticFeedbackManager
import com.zqy.hci.service.Settings
import com.zqy.hci.theme.ThemeAbleView
import com.zqy.hci.theme.ThemeState
import com.zqy.hci.theme.UITheme

/**
 * @author:zhenqiyuan
 * @data:2022/10/18
 * @描述：
 * @package:com.zqy.hci.widget
 */
class SymbolView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener, View.OnLongClickListener,
    ThemeAbleView {
    private val TAG = SymbolView::class.java.simpleName

    /**
     * 每页显示的符号数量
     */
    val PRE_PAGE = 16
    private val MSG_DELETE_REPEAT = 0
    private val REPEAT_INTERVAL = 80
    private val REPEAT_START_DELAY = 300
    val SYMBOLS_EN_LABEL_INDEX = 2
    private var mSymbolTitle: RecyclerView? = null
    private var mSymbolList: RecyclerView? = null
    private var mSymbolsAdapter: SymbolAdapter? = null


    private val DEFAULT_SYMBOL_INDEX = 0
    private val DEFAULT_SYMBOL_LINE = 6
    private var mSymbolTitleAdapter: SymbolTitleAdapter? = null
    private var mDefaultHeight = 0
    private var mSymbolView: SymbolView? = null
    private var mSymbolLock: ImageButton? = null
    private var mSymbolPre: ImageButton? = null
    private var mSymbolNext: ImageButton? = null
    private var mSymbolBack: ImageButton? = null
    private var mSymbolDelete: ImageButton? = null
    private var mSymbolClear: ImageButton? = null
    private var mSymbolAction: ImageButton? = null
    private var mInputView: View? = null
    private var mLock = false
    private var mListener: OnSymbolActionListener? = null
    private var mSymbolBottom: LinearLayout? = null
    private var isDeleteLongPress = false
    private var mHandler: Handler? = null
    private var ICON_ENTERDONE: Int = 0
    private var ICON_ENTERGO: Int = 0
    private var ICON_ENTERSEARCH: Int = 0
    private var ICON_ENTERNEXT: Int = 0
    private var ICON_ENTERPREVIOUS: Int = 0
    private var ICON_ENTERCONFIRM: Int = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (heightMode == MeasureSpec.EXACTLY) {
            heightSize = if (heightSize <= mDefaultHeight) heightSize else mDefaultHeight
        }
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = if (heightSize <= mDefaultHeight) heightSize else mDefaultHeight
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = if (heightSize <= mDefaultHeight) heightSize else mDefaultHeight
        }
        val maxHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
            heightSize,
            heightMode
        )
        super.onMeasure(widthMeasureSpec, maxHeightMeasureSpec)
    }

    private fun initView(context: Context) {
        mSymbolView =
            LayoutInflater.from(context).inflate(R.layout.symbol_view, this) as SymbolView?
        mSymbolTitle = mSymbolView!!.findViewById<View>(R.id.symbol_title) as RecyclerView
        mSymbolList = mSymbolView!!.findViewById<View>(R.id.symbols_list_recycle) as RecyclerView
        mSymbolBottom = mSymbolView!!.findViewById<View>(R.id.symbol_bottom) as LinearLayout
        mSymbolsAdapter = SymbolAdapter(context, this)
        mSymbolTitleAdapter = SymbolTitleAdapter(context)
        mSymbolLock = findViewById<View>(R.id.btn_symbol_lock) as ImageButton
        mSymbolPre = findViewById<View>(R.id.btn_symbol_pre_page) as ImageButton
        mSymbolNext = findViewById<View>(R.id.btn_symbol_next_page) as ImageButton
        mSymbolBack = findViewById<View>(R.id.btn_symbol_back) as ImageButton
        mSymbolDelete = findViewById<View>(R.id.btn_symbol_delete) as ImageButton
        mSymbolClear = findViewById<View>(R.id.btn_symbol_clear) as ImageButton
        mSymbolAction = findViewById<View>(R.id.btn_symbol_action) as ImageButton
        mSymbolLock!!.setOnClickListener(this)
        mSymbolPre!!.setOnClickListener(this)
        mSymbolNext!!.setOnClickListener(this)
        mSymbolBack!!.setOnClickListener(this)
        mSymbolDelete!!.setOnClickListener(this)
        mSymbolDelete!!.setOnLongClickListener(this)
        mSymbolClear!!.setOnClickListener(this)
        mSymbolAction!!.setOnClickListener(this)
    }

    /**
     * 显示符号界面，将该view 的Visible属性设置为VISIBLE
     * @param inputView
     */
    fun showView(inputView: View?) {
        mInputView = inputView
        mSymbolsAdapter?.getSymbolsRes()
        mSymbolsAdapter?.init()
        mSymbolList!!.adapter = mSymbolsAdapter
        mSymbolList!!.layoutManager =
            StaggeredGridLayoutManager(DEFAULT_SYMBOL_LINE, StaggeredGridLayoutManager.VERTICAL)
        mSymbolList!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                checkPageBtnState()
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        mSymbolTitleAdapter?.init(mSymbolsAdapter?.getLeftMenuLabels(), this)
        mSymbolTitle!!.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        mSymbolTitle!!.adapter = mSymbolTitleAdapter
        mSymbolTitle!!.background = AppCompatResources.getDrawable(context,R.drawable.bg_key_function_normal)
        mSymbolTitle!!.overScrollMode = OVER_SCROLL_NEVER
        setSymbolsLabelIndex(DEFAULT_SYMBOL_INDEX)
        visibility = VISIBLE
        refreshSoundSetting()
    }

    fun showENView(mSymbolView: SymbolView?) {
        mInputView = mSymbolView
        mSymbolsAdapter?.getSymbolsRes()
        mSymbolsAdapter?.init()
        mSymbolList!!.adapter = mSymbolsAdapter
        mSymbolList!!.layoutManager =
            StaggeredGridLayoutManager(DEFAULT_SYMBOL_LINE, StaggeredGridLayoutManager.VERTICAL)
        mSymbolList!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                checkPageBtnState()
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        mSymbolTitleAdapter?.init(mSymbolsAdapter?.getLeftMenuLabels(), this)
        mSymbolTitle!!.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        mSymbolTitle!!.adapter = mSymbolTitleAdapter
        mSymbolTitle!!.overScrollMode = OVER_SCROLL_NEVER
        setSymbolsLabelIndex(SYMBOLS_EN_LABEL_INDEX)
        mSymbolTitleAdapter?.setSelect(SYMBOLS_EN_LABEL_INDEX)
        visibility = VISIBLE
        refreshSoundSetting()
    }

    /**
     * 设置符号界面的点击监听
     * @param l
     */
    fun setOnSymbolActionListener(l: OnSymbolActionListener?) {
        mListener = l
    }

    /**
     * 隐藏符号界面
     */
    fun hideView() {
        visibility = GONE
    }

    /**
     * 设置符号标题的引索，更新符号类别
     * @param index
     */
    fun setSymbolsLabelIndex(index: Int) {
        mSymbolNext!!.isEnabled = true
        mSymbolPre!!.isEnabled = false
        mSymbolsAdapter?.setSymbolsLabelIndex(index)
        //        将符号界面滚动至第一行
        mSymbolList!!.scrollToPosition(0)
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
            mSymbolAction?.setImageDrawable(AppCompatResources.getDrawable(context,ENTER_ICON[0]))
        } else {
            mSymbolAction?.setImageDrawable(AppCompatResources.getDrawable(context,ENTER_ICON[currentMode - 1]))
        }
    }

    fun setEnterActionModeInComposing() {
        mSymbolAction?.setImageDrawable(AppCompatResources.getDrawable(context,ICON_ENTERCONFIRM))
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_symbol_lock -> symbolLock()
            R.id.btn_symbol_pre_page -> getPrePage()
            R.id.btn_symbol_next_page -> getNextPage()
            R.id.btn_symbol_back -> getBack()
            R.id.btn_symbol_delete -> {
                isDeleteLongPress = false
                delete()
            }
            R.id.btn_symbol_clear -> {
                mListener?.onSymbolReset()
            }
            R.id.btn_symbol_action -> {
                mListener?.onSymbolAction()
            }
        }
        AudioAndHapticFeedbackManager.getInstance().performAudioFeedback(0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mHandler = object : Handler(Looper.myLooper()!!) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_DELETE_REPEAT -> if (isDeleteLongPress) {
                        val repeat = Message.obtain(this, MSG_DELETE_REPEAT)
                        sendMessageDelayed(repeat, REPEAT_INTERVAL.toLong())
                        delete()
                    }
                }
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        isDeleteLongPress = true
        val msg = handler.obtainMessage(MSG_DELETE_REPEAT)
        mHandler!!.sendMessageDelayed(msg, REPEAT_START_DELAY.toLong())
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_OUTSIDE) {
            isDeleteLongPress = false
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun getBack() {
        mInputView?.visibility = VISIBLE
        hideView()
        mSymbolTitleAdapter?.clear()
        mListener?.onSymbolBack()
    }

    private fun getNextPage() {
        val next = getLastPositionShowOnSymbol() + PRE_PAGE
        mSymbolList!!.smoothScrollToPosition(next)
    }


    private fun getFirstPositionShowOnSymbol(): Int {
        val manager: StaggeredGridLayoutManager? =
            mSymbolList!!.layoutManager as StaggeredGridLayoutManager?
        val positions: IntArray = manager!!.findFirstCompletelyVisibleItemPositions(null)
        return positions[0]
    }

    private fun getLastPositionShowOnSymbol(): Int {
        val manager: StaggeredGridLayoutManager? =
            mSymbolList!!.layoutManager as StaggeredGridLayoutManager?
        val positions: IntArray = manager!!.findLastCompletelyVisibleItemPositions(null)
        var lastPosition = 0
        for (position in positions) {
            if (position >= lastPosition) {
                lastPosition = position
            }
        }
        return lastPosition
    }


    private fun getPrePage() {
        val pre = getFirstPositionShowOnSymbol() - PRE_PAGE
        mSymbolList!!.smoothScrollToPosition(pre)
    }

    private fun symbolLock() {
        mLock = !mLock
        mSymbolLock!!.isActivated = mLock
    }

    private fun checkPageBtnState() {
        mSymbolNext!!.isEnabled =
            getLastPositionShowOnSymbol() != mSymbolsAdapter!!.getItemCount() - 1
        mSymbolPre!!.isEnabled = getFirstPositionShowOnSymbol() != 0
    }

    /**
     * 符号界面的符号被点击，记录调整常用符号的频率，并将该符号到输入框
     * @param str
     */
    fun onSymbolSelected(str: String?) {
        if (mListener != null) {
            mListener?.onSymbolSelected(str)
        }
        mSymbolsAdapter?.adjustTheFrequency(str)
        if (!mLock) {
            getBack()
        }
//        else {
//            mSymbolsAdapter.notifyDataSetChanged();
//        }
    }

    private fun delete() {
        mListener?.onSymbolDelete()
    }

    private fun refreshSoundSetting() {
        val soundEnable: Boolean = Settings.instance.getCurrent()!!.isSoundOn()
        mSymbolLock!!.isSoundEffectsEnabled = soundEnable
        mSymbolPre!!.isSoundEffectsEnabled = soundEnable
        mSymbolNext!!.isSoundEffectsEnabled = soundEnable
        mSymbolBack!!.isSoundEffectsEnabled = soundEnable
        mSymbolDelete!!.isSoundEffectsEnabled = soundEnable
        mSymbolDelete!!.isSoundEffectsEnabled = soundEnable
    }

    override fun setUITheme(uiTheme: UITheme) {
        val typeArray = uiTheme.context.applicationContext.obtainStyledAttributes(
            uiTheme.themeResId,
            R.styleable.HciKeyboardViewTheme
        )
        mDefaultHeight = context.resources.getDimension(R.dimen.symbol_view_height).toInt()
        val bgId = typeArray.getResourceId(
            R.styleable.HciKeyboardViewTheme_symBolViewBgColor,
            R.color.keyboard_background_color
        )
        val bgColor =
            context.getColor(bgId)
        mSymbolView!!.setBackgroundColor(bgColor)
        typeArray.recycle()
        val iconTypeArray = uiTheme.context.applicationContext.obtainStyledAttributes(
            uiTheme.iconThemeResId,
            R.styleable.HciKeyboardFunctionIconTheme
        )
        val lockId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_iconSymbolLock,
            R.drawable.symbol_lock_icon
        )
        val lockIcon: Drawable? =
            AppCompatResources.getDrawable(context,lockId)
        val preId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_iconSymbolNext,
            R.drawable.symbol_pre_icon
        )
        val preIcon: Drawable? =
            AppCompatResources.getDrawable(context,preId)
        val nextId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_iconSymbolNext,
            R.drawable.symbol_next_icon
        )
        val nextIcon: Drawable? =
            AppCompatResources.getDrawable(context,nextId)
        val deleteId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_iconSymbolDelete,
            R.drawable.icon_delete
        )
        val deleteIcon: Drawable? =
            AppCompatResources.getDrawable(context,deleteId)
        val backId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_iconSymbolBack,
            R.drawable.icon_symbol
        )
        val backIcon: Drawable? =
            AppCompatResources.getDrawable(context,backId)
        val clearId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_iconSymbolClear,
            R.drawable.icon_clear
        )
        val clearIcon: Drawable? =
            AppCompatResources.getDrawable(context,clearId)
        val actionId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_iconSymbolAction,
            R.drawable.icon_enter
        )
        val actionIcon: Drawable? =
            AppCompatResources.getDrawable(context,actionId)
        val btnId = iconTypeArray.getResourceId(
            R.styleable.HciKeyboardFunctionIconTheme_buttonIcon,
            R.drawable.btn_symbol_bottom
        )
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
        if (!ThemeState.isDefault) {
            mSymbolBack?.setBackgroundResource(btnId)
            mSymbolLock?.setBackgroundResource(btnId)
            mSymbolClear?.setBackgroundResource(btnId)
            mSymbolDelete?.setBackgroundResource(btnId)
            mSymbolAction?.setBackgroundResource(btnId)
        }
        mSymbolLock!!.setImageDrawable(lockIcon)
        mSymbolPre!!.setImageDrawable(preIcon)
        mSymbolNext!!.setImageDrawable(nextIcon)
        mSymbolDelete!!.setImageDrawable(deleteIcon)
        mSymbolBack!!.setImageDrawable(backIcon)
        mSymbolClear!!.setImageDrawable(clearIcon)
        mSymbolAction!!.setImageDrawable(actionIcon)
        mSymbolPre!!.isEnabled = false
        mSymbolNext!!.isEnabled = true
        iconTypeArray.recycle()
    }

    init {
        initView(context)
    }
}
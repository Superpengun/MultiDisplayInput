package com.zqy.hci.widget

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Align
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.accessibility.AccessibilityManager
import android.widget.PopupWindow
import android.widget.TextView
import com.zqy.hci.R
import com.zqy.hci.bean.FunctionKeyCode
import com.zqy.hci.listener.OnKeyboardActionListener
import com.zqy.hci.theme.ThemeAbleView
import com.zqy.hci.theme.UITheme
import com.zqy.hci.widget.Keyboard.Key
import java.util.*

/**
 * @Author:CWQ
 * @DATE:2022/1/20
 * @DESC:
 */
class KeyboardView : View, View.OnClickListener, ThemeAbleView {

    companion object {
        const val TAG = "KeyboardView"
        private const val DEBUG = false
        private const val NOT_A_KEY = -1
        private val LONG_PRESSABLE_STATE_SET = intArrayOf(R.attr.state_long_pressable)
        private const val LOG = "KeyboardView"
        private const val REPEAT_INTERVAL = 50 // ~20 keys per second
        private const val REPEAT_START_DELAY = 400

        //    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
        private const val LONGPRESS_TIMEOUT = 500
    }

    private var mKeyboard: Keyboard? = null
    private var mCurrentKeyIndex: Int = NOT_A_KEY
    private var mLabelTextSize = 0
    private var mKeyTextSize = 0
    private var mKeyTextColor = 0
    private var mFucKeyTextColor = 0
    private var mShadowRadius = 0f
    private var mShadowColor = 0
    private var mBackgroundDimAmount = 0f

    private lateinit var mPreviewText: TextView
    private var mPreviewPopup: PopupWindow? = null
    private var mPreviewTextSizeLarge = 0
    private var mPreviewOffset = 0
    private var mPreviewHeight = 0

    // Working variable
    private val mCoordinates = IntArray(2)

    private var mPopupKeyboard: PopupWindow? = null
    private var mMiniKeyboardContainer: View? = null
    private var mMiniKeyboard: MiniKeyboardView? = null
    private var mMiniKeyboardOnScreen = false
    private var mPopupParent: View? = null
    private var mMiniKeyboardOffsetX = 0
    private var mMiniKeyboardOffsetY = 0
    private var mMiniKeyboardCache: HashMap<CharSequence, View>? = null
    private var mKeys: List<Keyboard.Key>? = null

    /**
     * Listener for [OnKeyboardActionListener].
     */
    private var mKeyboardActionListener: OnKeyboardActionListener? = null

    private val MSG_SHOW_PREVIEW = 1
    private val MSG_UPDATE_SPACE = 56
    private val MSG_REMOVE_PREVIEW = 2
    private val MSG_REPEAT = 3
    private val MSG_LONGPRESS = 4

    private val DELAY_BEFORE_PREVIEW = 0
    private val DELAY_AFTER_PREVIEW = 40
    private val DEBOUNCE_TIME = 70

    private var mVerticalCorrection = 0
    private var mProximityThreshold = 0

    private val mPreviewCentered = false
    private var mShowPreview = true
    private val mShowTouchPoints = true
    private var mPopupPreviewX = 0
    private var mPopupPreviewY = 0

    private var mLastX = 0
    private var mLastY = 0
    private var mStartX = 0
    private var mStartY = 0

    private var mProximityCorrectOn = false

    private var mPaint: Paint = Paint()
    private var mPadding: Rect? = null

    private var mDownTime: Long = 0
    private var mLastMoveTime: Long = 0
    private var mLastKey = 0
    private var mLastCodeX = 0
    private var mLastCodeY = 0
    private var mCurrentKey: Int = NOT_A_KEY
    private var mDownKey: Int = NOT_A_KEY
    private var mLastKeyTime: Long = 0
    private var mCurrentKeyTime: Long = 0
    private val mKeyIndices = IntArray(12)
    private var mGestureDetector: GestureDetector? = null
    private var mPopupX = 0
    private var mPopupY = 0
    private var mRepeatKeyIndex: Int = NOT_A_KEY
    private var mPopupLayout = 0
    private var mAbortKey = false
    private var mInvalidatedKey: Key? = null
    private val mClipRegion = Rect(0, 0, 0, 0)
    private var mPossiblePoly = false
    private val mSwipeTracker: SwipeTracker = SwipeTracker()
    private var mSwipeThreshold = 0
    private var mDisambiguateSwipe = false

    // Variables for dealing with multiple pointers
    private var mOldPointerCount = 1
    private var mOldPointerX = 0f
    private var mOldPointerY = 0f

    private var mKeyBackground: Drawable? = null
    private var mFunctionalKeyBackground: Drawable? = null

    private val MAX_NEARBY_KEYS = 12
    private val mDistances = IntArray(MAX_NEARBY_KEYS)

    // For multi-tap
    private var mLastSentIndex = 0
    private var mTapCount = 0
    private var mLastTapTime: Long = 0
    private var mInMultiTap = false
    private val MULTITAP_INTERVAL = 200 // milliseconds

    private val mPreviewLabel = StringBuilder(1)

    /**
     * Whether the keyboard bitmap needs to be redrawn before it's blitted.
     */
    private var mDrawPending = false

    /**
     * The dirty region in the keyboard bitmap
     */
    private val mDirtyRect = Rect()

    /**
     * The keyboard bitmap for faster updates
     */
    private var mBuffer: Bitmap? = null

    /**
     * Notes if the keyboard just changed, so that we could possibly reallocate the mBuffer.
     */
    private var mKeyboardChanged = false

    /**
     * The canvas for the above mutable keyboard bitmap
     */
    private var mCanvas: Canvas? = null

    /**
     * The accessibility manager for accessibility support
     */
    private var mAccessibilityManager: AccessibilityManager? = null

    /**
     * The audio manager for accessibility support
     */
    private var mAudioManager: AudioManager? = null

    /**
     * Whether the requirement of a headset to hear passwords if accessibility is enabled is announced.
     */
    private var mHeadsetRequiredToHearPasswordsAnnounced = false

    var mHandler: Handler? = null
    private val mSmallLabelColor = 0
    private val mSmallLabelSize = 0
    private var mCurrentEnterMode = 0

    /**
     * showSpace label once.
     */
    private var mShowSpaceKeyText = false
    private val mTheme: UITheme? = null
    private val backgroundColor = 0
    private var mUITheme: UITheme? = null


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        R.attr.keyboardViewStyle
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    override fun setUITheme(uiTheme: UITheme) {
        mUITheme = uiTheme
        val keyTextSize = 0
        val typeAttr = uiTheme.context.applicationContext.obtainStyledAttributes(
            uiTheme.themeResId,
            R.styleable.HciKeyboardViewTheme
        )
        dismissPopupKeyboard()
        mPreviewPopup?.dismiss()
        removeMessages()
        mFunctionalKeyBackground =
            typeAttr.getDrawable(R.styleable.HciKeyboardViewTheme_functionalKeyBackground)
        mKeyBackground = typeAttr.getDrawable(R.styleable.HciKeyboardViewTheme_keyBackground)
        if (mFunctionalKeyBackground == null) {
            mFunctionalKeyBackground = mKeyBackground
        }
        mVerticalCorrection =
            typeAttr.getDimensionPixelOffset(R.styleable.HciKeyboardViewTheme_verticalCorrection, 0)
        var previewLayout = typeAttr.getResourceId(R.styleable.HciKeyboardViewTheme_keyPreviewLayout, 0)
        mPreviewOffset =
            typeAttr.getDimensionPixelOffset(R.styleable.HciKeyboardViewTheme_keyPreviewOffset, 0)
        mPreviewHeight =
            typeAttr.getDimensionPixelOffset(R.styleable.HciKeyboardViewTheme_keyPreviewHeight, 20)
        mKeyTextSize =
            typeAttr.getDimensionPixelSize(R.styleable.HciKeyboardViewTheme_keyTextSize, 18)
        mKeyTextColor = typeAttr.getColor(R.styleable.HciKeyboardViewTheme_keyTextColor, -0x1000000)
        mFucKeyTextColor = typeAttr.getColor(R.styleable.HciKeyboardViewTheme_funKeyTextColor,-0x1000000)
        mLabelTextSize =
            typeAttr.getDimensionPixelSize(R.styleable.HciKeyboardViewTheme_labelTextSize, 14)
        mPopupLayout = typeAttr.getResourceId(R.styleable.HciKeyboardViewTheme_popupLayout, 0)
        mShadowColor = typeAttr.getColor(R.styleable.HciKeyboardViewTheme_shadowColor, 0)
        mShadowRadius = typeAttr.getFloat(R.styleable.HciKeyboardViewTheme_shadowRadius, 0f)
        val keyPreviewTextColor =
            typeAttr.getColor(R.styleable.HciKeyboardViewTheme_key_preview_text_color, Color.WHITE)
        val keyPreviewBg = typeAttr.getDrawable(R.styleable.HciKeyboardViewTheme_key_preview_bg)
        typeAttr.recycle()

        val inflate = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mPreviewPopup = PopupWindow(context)
        if (previewLayout != 0) {
            mPreviewText = inflate.inflate(previewLayout, null) as TextView
            mPreviewTextSizeLarge = mPreviewText.getTextSize().toInt()
            mPreviewPopup!!.setContentView(mPreviewText)
            mPreviewPopup!!.setBackgroundDrawable(null)
            mPreviewPopup!!.setClippingEnabled(false)
        } else {
            mShowPreview = false
        }

        mPreviewPopup!!.setTouchable(false)

        mPopupKeyboard = PopupWindow(context)
        mPopupKeyboard!!.setBackgroundDrawable(null)


        mPopupParent = this

        mPaint.setAntiAlias(true)
        mPaint.setTextSize(keyTextSize.toFloat())
        mPaint.setTextAlign(Align.CENTER)
        mPaint.setAlpha(255)

        mPadding = Rect(0, 0, 0, 0)
        mMiniKeyboardCache = HashMap()
        mKeyBackground!!.getPadding(mPadding!!)

        mSwipeThreshold = (500 * resources.displayMetrics.density).toInt()
        mDisambiguateSwipe = true
        mAccessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mKeyboardActionListener = OnKeyboardActionListener.EMPTY_LISTENER
        resetMultiTap()
        mPreviewText.setTextColor(keyPreviewTextColor)
        mPreviewText.background = keyPreviewBg
        if(mKeyboard != null){
            invalidateAllKeys()
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initGestureDetector()
        if (mHandler == null) {
            mHandler = object : Handler(Looper.myLooper()!!) {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        MSG_SHOW_PREVIEW ->     //showKey(msg.arg1);
                            mPreviewText.visibility = VISIBLE
                        MSG_REMOVE_PREVIEW -> mPreviewText.visibility =
                            INVISIBLE
                        MSG_REPEAT -> if (repeatKey()) {
                            val repeat: Message = Message.obtain(
                                this,
                                MSG_REPEAT
                            )
                            sendMessageDelayed(
                                repeat,
                                REPEAT_INTERVAL.toLong()
                            )
                        }
                        MSG_LONGPRESS -> openPopupIfRequired(
                            msg.obj as MotionEvent
                        )
                        MSG_UPDATE_SPACE -> {
                            if (mKeyboard != null && mKeyboard!!.getSpaceKey() != null){
                                mShowSpaceKeyText = false
                                invalidateKey(mKeyboard!!.getSpaceKey()!!)
                            }
                        }
                    }
                }
            }
        }
        sendUpdateSpaceMsg()
    }

    private fun initGestureDetector() {
        if (mGestureDetector == null) {
            mGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
                override fun onFling(
                    me1: MotionEvent, me2: MotionEvent,
                    velocityX: Float, velocityY: Float
                ): Boolean {
                    if (mMiniKeyboardOnScreen) return false
                    val absX = Math.abs(velocityX)
                    val absY = Math.abs(velocityY)
                    val deltaX = me2.x - me1.x
                    val deltaY = me2.y - me1.y
                    Log.d(TAG, "onFling: velocitY :" + velocityY + "deltaY " + deltaY)
                    val travelX = width / 2 // Half the keyboard width
                    val travelY = height / 2 // Half the keyboard height
                    mSwipeTracker.computeCurrentVelocity(1000)
                    val endingVelocityX: Float = mSwipeTracker.xVelocity
                    var sendDownKey = false
                    if (me1.y > me2.y && deltaY < -travelY / 5) {
                        return detectSwipeUpKey(mDownKey, mStartX, mStartY, me1.eventTime)
                    }
                    if (velocityX > mSwipeThreshold && absY < absX && deltaX > travelX) {
                        sendDownKey = if (mDisambiguateSwipe && endingVelocityX < velocityX / 4) {
                            true
                        } else {
                            swipeRight()
                            return true
                        }
                    } else if (velocityX < -mSwipeThreshold && absY < absX && deltaX < -travelX) {
                        sendDownKey = if (mDisambiguateSwipe && endingVelocityX > velocityX / 4) {
                            true
                        } else {
                            swipeLeft()
                            return true
                        }
                    }
                    if (sendDownKey) {
                        detectAndSendKey(mDownKey, mStartX, mStartY, me1.eventTime)
                    }
                    return false
                }

                override fun onScroll(
                    me1: MotionEvent,
                    me2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    val deltaX = me2.x - me1.x
                    if (mMiniKeyboardOnScreen && mMiniKeyboard != null) {
                        val travelX = mMiniKeyboard!!.width / 9 * 5 / mMiniKeyboard!!.childCount
                        val delta = deltaX / travelX
                        val index = Math.round(delta)
                        mMiniKeyboard!!.swipeTo(index)
                        return true
                    }
                    return false
                }
            })
            mGestureDetector!!.setIsLongpressEnabled(false)
        }
    }

    fun setOnKeyboardActionListener(listener: OnKeyboardActionListener) {
        mKeyboardActionListener = listener
    }

    /**
     * Returns the [OnKeyboardActionListener] object.
     *
     * @return the listener attached to this keyboard
     */
    protected fun getOnKeyboardActionListener(): OnKeyboardActionListener? {
        return mKeyboardActionListener
    }

    /**
     * Attaches a keyboard to this view. The keyboard can be switched at any time and the
     * view will re-layout itself to accommodate the keyboard.
     *
     * @param keyboard the keyboard to display in this view
     * @see Keyboard
     *
     * @see .getKeyboard
     */
    fun setKeyboard(keyboard: Keyboard) {
        if (mKeyboard != null) {
            showPreview(NOT_A_KEY)
        }
        // Remove any pending messages
        mKeyboard = keyboard
        if (mKeyboard == null) {
            return
        }
        mKeys = keyboard.getKeys()
        requestLayout()
        // Hint to reallocate the buffer if the size changed
        mKeyboardChanged = true
        invalidateAllKeys()
        computeProximityThreshold(keyboard)
        mMiniKeyboardCache?.clear() // Not really necessary to do every time, but will free up views
        // Switching to a different keyboard should abort any pending keys so that the key up
        // doesn't getBoolean delivered to the old or new keyboard
        mAbortKey = true // Until the next ACTION_DOWN
    }

    /**
     * Returns the current keyboard being displayed by this view.
     *
     * @return the currently attached keyboard
     * @see .setKeyboard
     */
    fun getKeyboard(): Keyboard? {
        return mKeyboard
    }


    /**
     * Sets the state of the shift key of the keyboard, if any.
     *
     * @param shifted whether or not to enable the state of the shift key
     * @return true if the shift key state changed, false if there was no change
     * @see KeyboardView.isShifted
     */
    fun setShifted(shifted: Boolean): Boolean {
        if (mKeyboard != null) {
            if (mKeyboard!!.setShifted(shifted)) {
                // The whole keyboard probably needs to be redrawn
                invalidateAllKeys()
                return true
            }
        }
        return false
    }

    /**
     * Sets the state of the shift key of the keyboard, if any.
     */
    fun setAltKey(altState: Boolean) {
        if (mKeyboard != null) {
            mKeyboard!!.setAltKey(altState)
            invalidateAllKeys()
        }
    }

    /**
     * Returns the state of the shift key of the keyboard, if any.
     *
     * @return true if the shift is in a pressed state, false otherwise. If there is
     * no shift key on the keyboard or there is no keyboard attached, it returns false.
     * @see KeyboardView.setShifted
     */
    fun isShifted(): Boolean {
        return if (mKeyboard != null) {
            mKeyboard!!.isShifted()
        } else false
    }

    fun getShiftMode(): Int {
        return if (mKeyboard != null) {
            mKeyboard!!.getShiftMode()
        } else 0
    }

    /**
     * Enables or disables the key feedback popup. This is a popup that shows a magnified
     * version of the depressed key. By default the preview is enabled.
     *
     * @param previewEnabled whether or not to enable the key feedback popup
     * @see .isPreviewEnabled
     */
    fun setPreviewEnabled(previewEnabled: Boolean) {
        mShowPreview = previewEnabled
    }

    /**
     * Returns the enabled state of the key feedback popup.
     *
     * @return whether or not the key feedback popup is enabled
     * @see .setPreviewEnabled
     */
    fun isPreviewEnabled(): Boolean {
        return mShowPreview
    }

    fun setVerticalCorrection(verticalOffset: Int) {}

    fun setPopupParent(v: View) {
        mPopupParent = v
    }

    fun setPopupOffset(x: Int, y: Int) {
        mMiniKeyboardOffsetX = x
        mMiniKeyboardOffsetY = y
        if (mPreviewPopup!!.isShowing) {
            mPreviewPopup!!.dismiss()
        }
    }

    /**
     * When enabled, calls to [OnKeyboardActionListener.onKey] will include key
     * codes for adjacent keys.  When disabled, only the primary key code will be
     * reported.
     *
     * @param enabled whether or not the proximity correction is enabled
     */
    fun setProximityCorrectionEnabled(enabled: Boolean) {
        mProximityCorrectOn = enabled
    }

    /**
     * Returns true if proximity correction is enabled.
     */
    fun isProximityCorrectionEnabled(): Boolean {
        return mProximityCorrectOn
    }

    override fun onClick(v: View?) {
        dismissPopupKeyboard()
    }

    private fun adjustCase(key: Key): CharSequence? {
        return if (mKeyboard!!.isShifted() && mKeyboard!!.isDaiKeyboard()) {
            if (key.label2 == null) key.label else key.label2
        } else adjustCase(key.label)
    }

    private fun adjustCase(label: CharSequence?): CharSequence? {
        var label = label
        if (mKeyboard!!.isShifted() && label != null && Character.isLowerCase(label[0])) {
            label = label.toString().toUpperCase()
        }
        return label
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Round up a little

        // Round up a little
        if (mKeyboard == null) {
            setMeasuredDimension(paddingLeft + paddingRight, paddingTop + paddingBottom)
        } else {
            var width = mKeyboard!!.getMinWidth() + paddingLeft + paddingRight
            if (MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
                width = MeasureSpec.getSize(widthMeasureSpec)
            }
            setMeasuredDimension(width, mKeyboard!!.getHeight() + paddingTop + paddingBottom)
        }
    }

    /**
     * Compute the average distance between adjacent keys (horizontally and vertically)
     * and square it to getBoolean the proximity threshold. We use a square here and in computing
     * the touch distance from a key's center to avoid taking a square root.
     *
     * @param keyboard
     */
    private fun computeProximityThreshold(keyboard: Keyboard?) {
        if (keyboard == null) return
        val keys = mKeys ?: return
        val length = keys.size
        var dimensionSum = 0
        for (key in keys) {
            dimensionSum += Math.min(key.width, key.height) + key.gap
        }
        if (dimensionSum < 0 || length == 0) return
        mProximityThreshold = (dimensionSum * 1.4f / length).toInt()
        mProximityThreshold *= mProximityThreshold // Square it
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBuffer = null
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mDrawPending || mBuffer == null || mKeyboardChanged) {
            onBufferDraw()
        }
        canvas!!.drawBitmap(mBuffer!!, 0f, 0f, null)
    }

    private fun onBufferDraw() {
        if (mBuffer == null || mKeyboardChanged) {
            if (mBuffer == null || mBuffer!!.width != width || mBuffer!!.height != height) {
                // Make sure our bitmap is at least 1x1
                val width = Math.max(1, width)
                val height = Math.max(1, height)
                mBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                mCanvas = Canvas(mBuffer!!)
            }
            invalidateAllKeys()
            mKeyboardChanged = false
        }
        val canvas = mCanvas!!
        if (Build.VERSION.SDK_INT >= 26){
            canvas.save()
            canvas.clipRect(mDirtyRect)
            canvas.restore()
        }else{
            canvas.clipRect(mDirtyRect, Region.Op.REPLACE)
        }
        if (mKeyboard == null) return
        val paint = mPaint
        val clipRegion = mClipRegion
        val padding = mPadding!!
        val kbdPaddingLeft = paddingLeft
        val kbdPaddingTop = paddingTop
        val keys = mKeys!!
        val invalidKey = mInvalidatedKey
        var keyBackground = mKeyBackground
        var keyTextColor = mKeyTextColor
        var drawSingleKey = false
        if (invalidKey != null && canvas.getClipBounds(clipRegion)) {
            // Is clipRegion completely contained within the invalidated key?
            if (invalidKey.x + kbdPaddingLeft - 1 <= clipRegion.left && invalidKey.y + kbdPaddingTop - 1 <= clipRegion.top && invalidKey.x + invalidKey.width + kbdPaddingLeft + 1 >= clipRegion.right && invalidKey.y + invalidKey.height + kbdPaddingTop + 1 >= clipRegion.bottom) {
                drawSingleKey = true
            }
        }
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR)
        for (key: Key in keys) {
            if (drawSingleKey && invalidKey !== key) {
                continue
            }
            val drawableState = key.getCurrentDrawableState()
            keyBackground = key.chooseBackground(mKeyBackground, mFunctionalKeyBackground)
            keyBackground!!.state = drawableState
            keyTextColor = key.chooseLabelColor(mKeyTextColor, mFucKeyTextColor)
            paint.color = keyTextColor
            // Switch the character to uppercase if shift is pressed
            val label = if (key.label == null) null else adjustCase(key).toString()
            val bounds = keyBackground.bounds
            if (key.width != bounds.right || key.height != bounds.bottom) {
                keyBackground.setBounds(0, 0, key.width, key.height)
            }
            canvas.translate((key.x + kbdPaddingLeft).toFloat(), (key.y + kbdPaddingTop).toFloat())
            keyBackground.draw(canvas)
            var forceDrawIcon = false
            if (key.isSpace()) {
                if (!mShowSpaceKeyText) {
                    //空格键 是否制画图标
                    forceDrawIcon = true
                }
            }
            if (label != null && !forceDrawIcon) {
                // For characters, use large font. For labels like "Done", use small font.
                if (label.length > 1 && key.codes!!.size < 2) {
                    paint.textSize = mLabelTextSize.toFloat()
                    paint.typeface = Typeface.DEFAULT
                } else {
                    paint.textSize = mKeyTextSize.toFloat()
                    paint.typeface = Typeface.DEFAULT
                }
                if (key.isEnter()) {
                    var size = mKeyTextSize
                    var keyWidth = paint.measureText(label)
                    while (keyWidth > key.width - 10) {
                        size -= 2
                        paint.textSize = size.toFloat()
                        keyWidth = paint.measureText(label)
                    }
                }

                // Draw a drop shadow for the text
                paint.setShadowLayer(mShadowRadius, 0f, 0f, mShadowColor)
                // Draw the text
                canvas.drawText(
                    label, ((key.width - padding.left - padding.right) / 2
                            + padding.left).toFloat(),
                    ((key.height - padding.top - padding.bottom) / 2
                            ) + ((paint.textSize - paint.descent()) / 2) + padding.top,
                    paint
                )
                // Turn off drop shadow
                paint.setShadowLayer(0f, 0f, 0f, 0)
                paint.textSize = mSmallLabelSize.toFloat()
                if (key.smallLabel != null) {
                    paint.color = mSmallLabelColor
                    canvas.drawText(
                        key.smallLabel.toString(), (
                                ((key.width - padding.left - padding.right) / 2
                                        + padding.left).toFloat()),
                        ((key.height - padding.top - padding.bottom) / 7
                                ) + ((paint.textSize - paint.descent()) / 7) + padding.top,
                        paint
                    )
                }
            } else if (key.icon != null) {
                val drawableX = ((key.width - padding.left - padding.right
                        - key.icon!!.intrinsicWidth)) / 2 + padding.left
                val drawableY = ((key.height - padding.top - padding.bottom
                        - key.icon!!.intrinsicHeight)) / 2 + padding.top
                canvas.translate(drawableX.toFloat(), drawableY.toFloat())
                key.icon!!.state = drawableState
                key.icon!!.setBounds(
                    0, 0,
                    key.icon!!.intrinsicWidth, key.icon!!.intrinsicHeight
                )
                key.icon!!.draw(canvas)
                canvas.translate(-drawableX.toFloat(), -drawableY.toFloat())
            }
            canvas.translate(
                (-key.x - kbdPaddingLeft).toFloat(),
                (-key.y - kbdPaddingTop).toFloat()
            )
        }
        mInvalidatedKey = null
        // Overlay a dark rectangle to dim the keyboard
        if (mMiniKeyboardOnScreen) {
            paint.color = (mBackgroundDimAmount * 0xFF).toInt() shl 24
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }
        if (DEBUG && mShowTouchPoints) {
            paint.alpha = 128
            paint.color = -0x10000
            canvas.drawCircle(mStartX.toFloat(), mStartY.toFloat(), 3f, paint)
            canvas.drawLine(
                mStartX.toFloat(),
                mStartY.toFloat(),
                mLastX.toFloat(),
                mLastY.toFloat(),
                paint
            )
            paint.color = -0xffff01
            canvas.drawCircle(mLastX.toFloat(), mLastY.toFloat(), 3f, paint)
            paint.color = -0xff0100
            canvas.drawCircle(
                ((mStartX + mLastX) / 2).toFloat(),
                ((mStartY + mLastY) / 2).toFloat(),
                2f,
                paint
            )
        }
        mDrawPending = false
        mDirtyRect.setEmpty()
    }


    fun showSpaceIcon() {
        mShowSpaceKeyText = false
        invalidateKey(mKeyboard!!.getSpaceKey()!!)
    }

    fun sendUpdateSpaceMsg() {
        Log.d(TAG, "sendUpdateSpaceMsg: ")
        if (mHandler != null) {
            mHandler!!.removeMessages(MSG_UPDATE_SPACE)
            mHandler!!.sendMessageDelayed(
                mHandler!!.obtainMessage(MSG_UPDATE_SPACE),
                2000
            )
        }
    }

    fun setSpaceKeyText(str: String?) {
        val spaceKey = mKeyboard!!.getSpaceKey()
        spaceKey!!.label = str
        mShowSpaceKeyText = true
        reMoveUpdateSpaceMsg()
        invalidateKey(spaceKey)
    }

    private fun reMoveUpdateSpaceMsg() {
        if (mHandler != null) mHandler!!.removeMessages(MSG_UPDATE_SPACE)
    }

    private fun getKeyIndices(x: Int, y: Int, allKeys: IntArray?): Int {
        val keys = mKeys!!
        var primaryIndex: Int = NOT_A_KEY
        var closestKey: Int = NOT_A_KEY
        var closestKeyDist = mProximityThreshold + 1
        Arrays.fill(mDistances, Int.MAX_VALUE)
        val nearestKeyIndices = mKeyboard!!.getNearestKeys(x, y)
        val keyCount = nearestKeyIndices!!.size
        for (i in 0 until keyCount) {
            val key = keys[nearestKeyIndices[i]]
            var dist = 0
            val isInside: Boolean = key.isInside(x, y)
            if (isInside) {
                primaryIndex = nearestKeyIndices[i]
            }
            if (((mProximityCorrectOn
                        && key.squaredDistanceFrom(x, y).also { dist = it } < mProximityThreshold)
                        || isInside)
                && key.codes!![0] > 32
            ) {
                // Find insertion point
                val nCodes: Int = key.codes!!.size
                if (dist < closestKeyDist) {
                    closestKeyDist = dist
                    closestKey = nearestKeyIndices[i]
                }
                if (allKeys == null) continue
                for (j in mDistances.indices) {
                    if (mDistances[j] > dist) {
                        // Make space for nCodes codes
                        System.arraycopy(
                            mDistances, j, mDistances, j + nCodes,
                            mDistances.size - j - nCodes
                        )
                        System.arraycopy(
                            allKeys, j, allKeys, j + nCodes,
                            allKeys.size - j - nCodes
                        )
                        for (c in 0 until nCodes) {
                            allKeys[j + c] = key.codes!![c]
                            mDistances[j + c] = dist
                        }
                        break
                    }
                }
            }
        }
        if (primaryIndex == NOT_A_KEY) {
            primaryIndex = closestKey
        }
        return primaryIndex
    }

    private fun detectAndSendKey(index: Int, x: Int, y: Int, eventTime: Long) {
        if (index != NOT_A_KEY && index < mKeys!!.size) {
            val key = mKeys!![index]
            if (key.text != null && !key.text.toString().equals("0")) {
                //泰语键盘的特殊处理
                val text = if (mKeyboard!!.isDaiKeyboard()) {
                    adjustCase(key).toString()
                } else {
                    adjustCase(key.text).toString()
                }
                mKeyboardActionListener?.onText(text)
                mKeyboardActionListener?.onRelease(NOT_A_KEY)
            } else {
                var code = key.codes!![0]
                val codes =
                    IntArray(MAX_NEARBY_KEYS)
                Arrays.fill(codes, NOT_A_KEY)
                getKeyIndices(x, y, codes)
                // Multi-tap
                if (mInMultiTap) {
                    if (mTapCount != -1) {
//                        mKeyboardActionListener.onKey(Keyboard.KEYCODE_DELETE, KEY_DELETE);
                    } else {
                        mTapCount = 0
                    }
                    code = key.codes!![mTapCount]
                }
                mKeyboardActionListener?.onKey(code, codes)
                mKeyboardActionListener?.onRelease(code)
            }
            mLastSentIndex = index
            mLastTapTime = eventTime
        }
    }

    private fun detectSwipeUpKey(index: Int, x: Int, y: Int, eventTime: Long): Boolean {
        if (index != NOT_A_KEY && index < mKeys!!.size) {
            val key = mKeys!![index]
            mLastSentIndex = index
            mLastTapTime = eventTime
            if (key.text != null && key.smallLabel != null) {
                val smallInput = adjustCase(key.smallLabel).toString()
                mKeyboardActionListener?.onKey(smallInput[0].toInt(), key.codes)
                mKeyboardActionListener?.onRelease(NOT_A_KEY)
                return true
            }
        }
        return false
    }

    /**
     * Handle multi-tap keys by producing the key label for the current multi-tap state.
     */
    private fun getPreviewText(key: Key): CharSequence? {
        return if (mInMultiTap) {
            // Multi-tap
            mPreviewLabel.setLength(0)
            mPreviewLabel.append(key.codes!![if (mTapCount < 0) 0 else mTapCount].toChar())
            adjustCase(mPreviewLabel)
        } else {
            adjustCase(key)
        }
    }

    private fun showPreview(keyIndex: Int) {
        val oldKeyIndex = mCurrentKeyIndex
        val previewPopup = mPreviewPopup!!
        mCurrentKeyIndex = keyIndex
        // Release the old key and press the new key
        val keys = mKeys!!
        if (oldKeyIndex != mCurrentKeyIndex) {
            if (oldKeyIndex != NOT_A_KEY && keys.size > oldKeyIndex) {
                val oldKey = keys[oldKeyIndex]
                oldKey.onReleased(mCurrentKeyIndex == NOT_A_KEY)
                invalidateKey(oldKeyIndex)
            }
            if (mCurrentKeyIndex != NOT_A_KEY && keys.size > mCurrentKeyIndex) {
                val newKey = keys[mCurrentKeyIndex]
                newKey.onPressed()
                invalidateKey(mCurrentKeyIndex)
            }
        }
        // If key changed and preview is on ...
        if (oldKeyIndex != mCurrentKeyIndex && mShowPreview) {
            mHandler!!.removeMessages(MSG_SHOW_PREVIEW)
            if (previewPopup.isShowing) {
                if (keyIndex == NOT_A_KEY) {
                    mHandler!!.sendMessageDelayed(
                        mHandler!!.obtainMessage(MSG_REMOVE_PREVIEW),
                        DELAY_AFTER_PREVIEW.toLong()
                    )
                }
            }
            if (keyIndex != NOT_A_KEY) {
                showKey(keyIndex)
            }
        }
        mBuffer = null
    }

    private fun isAFunctionKey(key: Key): Boolean {
        if (key.backgroundType !== 1) {
            return true
        }
        //语音按键和空格键的弹出屏蔽
        return if (key.codes!![0] === -11 || key.codes!![0] === FunctionKeyCode.KEY_NUM_CN || key.codes!![0] === FunctionKeyCode.KEY_ENTER || key.codes!![0] === FunctionKeyCode.KEY_SYMBOL || key.codes!![0] === FunctionKeyCode.KEY_DEL || key.codes!![0] === FunctionKeyCode.KEY_SPACE
        ) {
            true
        } else false
    }

    private fun showKey(keyIndex: Int) {
//        if (!Settings.instance.getCurrent()!!.isPopOn()) {
//            return
//        }
        val previewPopup = mPreviewPopup!!
        val keys = mKeys!!
        if (keyIndex < 0 || keyIndex >= mKeys!!.size) return
        val key = keys[keyIndex]
        if (isAFunctionKey(key)) {
            previewPopup.dismiss()
            return
        }
        if (key.icon != null) {
            mPreviewText.setCompoundDrawables(
                null, null, null,
                if (key.iconPreview != null) key.iconPreview else key.icon
            )
            mPreviewText.setText(null)
        } else {
            mPreviewText.setCompoundDrawables(null, null, null, null)
            mPreviewText.text = getPreviewText(key)
            if (key.label!!.length > 1 && key.codes!!.size < 2) {
                mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mKeyTextSize.toFloat())
                mPreviewText.setTypeface(Typeface.DEFAULT_BOLD)
            } else {
                mPreviewText.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    mPreviewTextSizeLarge.toFloat()
                )
                mPreviewText.setTypeface(Typeface.DEFAULT)
            }
        }
        mPreviewText.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val popupWidth = Math.max(
            mPreviewText.measuredWidth, key.width
                    + mPreviewText.paddingLeft + mPreviewText.paddingRight
        )
        val popupHeight = mPreviewHeight
        val lp = mPreviewText.layoutParams
        if (lp != null) {
            lp.width = popupWidth
            lp.height = popupHeight
        }
        if (!mPreviewCentered) {
            mPopupPreviewX = key.x - mPreviewText.paddingLeft + paddingLeft
            mPopupPreviewY = key.y - popupHeight + mPreviewOffset
        } else {
            // TODO: Fix this if centering is brought back
            mPopupPreviewX = 160 - mPreviewText.measuredWidth / 2
            mPopupPreviewY = -mPreviewText.measuredHeight
        }
        mHandler!!.removeMessages(MSG_REMOVE_PREVIEW)
        getLocationInWindow(mCoordinates)
        mCoordinates[0] += mMiniKeyboardOffsetX // Offset may be zero
        mCoordinates[1] += mMiniKeyboardOffsetY // Offset may be zero

        // Set the preview background state
        mPreviewText.background.state =
            if (key.popupResId !== 0) LONG_PRESSABLE_STATE_SET else EMPTY_STATE_SET
        mPopupPreviewX += mCoordinates[0]
        mPopupPreviewY += mCoordinates[1]

        // If the popup cannot be shown above the key, put it on the side
        getLocationOnScreen(mCoordinates)
        if (mPopupPreviewY + mCoordinates[1] < 0) {
            // If the key you're pressing is on the left side of the keyboard, show the popup on
            // the right, offset by enough to see at least one key to the left/right.
            if (key.x + key.width <= width / 2) {
                mPopupPreviewX += (key.width * 2.5).toInt()
            } else {
                mPopupPreviewX -= (key.width * 2.5).toInt()
            }
            mPopupPreviewY += popupHeight
        }
        if (previewPopup.isShowing) {
            previewPopup.update(
                mPopupPreviewX, mPopupPreviewY,
                popupWidth, popupHeight
            )
        } else {
            previewPopup.width = popupWidth
            previewPopup.height = popupHeight
            previewPopup.showAtLocation(
                mPopupParent, Gravity.NO_GRAVITY,
                mPopupPreviewX, mPopupPreviewY
            )
        }
        //        mPreviewText.setVisibility(VISIBLE);
        mHandler!!.sendMessageDelayed(
            mHandler!!.obtainMessage(MSG_SHOW_PREVIEW),
            DELAY_AFTER_PREVIEW.toLong()
        )
    }


    /**
     * Requests a redraw of the entire keyboard. Calling [.invalidate] is not sufficient
     * because the keyboard renders the keys to an off-screen buffer and an invalidate() only
     * draws the cached buffer.
     *
     * @see .invalidateKey
     */
    fun invalidateAllKeys() {
        mDirtyRect.union(0, 0, width, height)
        mDrawPending = true
        invalidate()
    }

    /**
     * Invalidates a key so that it will be redrawn on the next repaint. Use this method if only
     * one key is changing it's content. Any changes that affect the position or size of the key
     * may not be honored.
     *
     * @param keyIndex the index of the key in the attached [Keyboard].
     * @see .invalidateAllKeys
     */
    fun invalidateKey(keyIndex: Int) {
        if (mKeys == null) return
        if (keyIndex < 0 || keyIndex >= mKeys!!.size) {
            return
        }
        val key = mKeys!![keyIndex]
        mInvalidatedKey = key
        mDirtyRect.union(
            key.x + paddingLeft, key.y + paddingTop,
            key.x + key.width + paddingLeft, key.y + key.height + paddingTop
        )
        onBufferDraw()
        invalidate(
            key.x + paddingLeft, key.y + paddingTop,
            key.x + key.width + paddingLeft, key.y + key.height + paddingTop
        )
    }

    /**
     * Invalidates a key so that it will be redrawn on the next repaint. Use this method if only
     * one key is changing it's content. Any changes that affect the position or size of the key
     * may not be honored.
     *
     * @see .invalidateKey
     */
    fun invalidateKey(key: Key) {
        if (mKeys == null) return
        mInvalidatedKey = key
        mDirtyRect.union(
            key.x + paddingLeft, key.y + paddingTop,
            key.x + key.width + paddingLeft, key.y + key.height + paddingTop
        )
        onBufferDraw()
        invalidate(
            key.x + paddingLeft, key.y + paddingTop,
            key.x + key.width + paddingLeft, key.y + key.height + paddingTop
        )
    }

    private fun openPopupIfRequired(me: MotionEvent): Boolean {
        // Check if we have a popup layout specified first.
        if (mPopupLayout == 0) {
            return false
        }
        if (mCurrentKey < 0 || mCurrentKey >= mKeys!!.size) {
            return false
        }
        val popupKey = mKeys!![mCurrentKey]
        val result: Boolean = onLongPress(popupKey)
        if (result) {
            mAbortKey = true
            showPreview(NOT_A_KEY)
        }
        return result
    }

    /**
     * Called when a key is long pressed. By default this will open any popup keyboard associated
     * with this key through the attributes popupLayout and popupCharacters.
     *
     * @param popupKey the key that was long pressed
     * @return true if the long press is handled, false otherwise. Subclasses should call the
     * method on the base class if the subclass doesn't wish to handle the call.
     */
    protected fun onLongPress(popupKey: Key): Boolean {
        if (popupKey.popupCharacters != null) {
            if (mKeyboard!!.isDaiKeyboard() && popupKey.label2 != null && popupKey.label2!!.equals("ฯ") && !mKeyboard!!.isShifted()) {
                return false
            }
            val popupCharacters = adjustCase(popupKey.popupCharacters)
            mMiniKeyboardContainer = mMiniKeyboardCache!![popupCharacters!!]
            if (mMiniKeyboardContainer == null) {
                val inflater = context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE
                ) as LayoutInflater
                mMiniKeyboardContainer = inflater.inflate(mPopupLayout, null)
                mMiniKeyboard = mMiniKeyboardContainer!!.findViewById<View>(
                    R.id.popup_keyboard
                ) as MiniKeyboardView
                mMiniKeyboard!!.setUITheme(mUITheme!!)
                if (popupKey.codes!![0] === FunctionKeyCode.KEY_ARAB_ALPHABET) {
                    mMiniKeyboard!!.setMiniMiniKey(popupCharacters)
                } else {
                    //设置选中
                    mMiniKeyboard!!.setDefaultActiveIndex(popupKey.defaultPopSelectIndex)
                    mMiniKeyboard!!.setMiniKey(popupCharacters)
                }
                mMiniKeyboard!!.setPopupParent(this)
                mMiniKeyboardContainer!!.measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
                )
                mMiniKeyboardCache!![popupCharacters] = mMiniKeyboardContainer!!
            } else {
                mMiniKeyboard = mMiniKeyboardContainer!!.findViewById<View>(
                    R.id.popup_keyboard
                ) as MiniKeyboardView
            }
            getLocationInWindow(mCoordinates)
            mPopupX = popupKey.x + paddingLeft
            mPopupY = popupKey.y + paddingTop
            mPopupX = mPopupX + popupKey.width / 2 - mMiniKeyboardContainer!!.measuredWidth / 2
            mPopupY = mPopupY - mMiniKeyboardContainer!!.measuredHeight
            mPopupX = if (mPopupX < 0) 0 else mPopupX
            mPopupX =
                if (mPopupX + mMiniKeyboardContainer!!.measuredWidth > width) width - mMiniKeyboardContainer!!.measuredWidth else mPopupX
            val x = mPopupX + mMiniKeyboardContainer!!.paddingRight + mCoordinates[0]
            val y = mPopupY + mMiniKeyboardContainer!!.paddingBottom + mCoordinates[1]
            mMiniKeyboard!!.setPopupOffset(if (x < 0) 0 else x, y)
            //            mMiniKeyboard.setShifted(isShifted());
            mPopupKeyboard!!.contentView = mMiniKeyboardContainer
            mPopupKeyboard!!.isClippingEnabled = false
            mPopupKeyboard!!.width = mMiniKeyboardContainer!!.measuredWidth
            mPopupKeyboard!!.height = mMiniKeyboardContainer!!.measuredHeight
            mPopupKeyboard!!.isTouchable = false
            mPopupKeyboard!!.showAtLocation(this, Gravity.NO_GRAVITY, x, y)
            mMiniKeyboardOnScreen = true
            //            mMiniKeyboard.onTouchEvent(getTranslatedEvent(me));
            invalidateAllKeys()
            return true
        }
        return false
    }

    override fun onHoverEvent(event: MotionEvent?): Boolean {
        if (mAccessibilityManager!!.isTouchExplorationEnabled && event!!.pointerCount == 1) {
            val action = event.action
            when (action) {
                MotionEvent.ACTION_HOVER_ENTER -> {
                    event.action = MotionEvent.ACTION_DOWN
                }
                MotionEvent.ACTION_HOVER_MOVE -> {
                    event.action = MotionEvent.ACTION_MOVE
                }
                MotionEvent.ACTION_HOVER_EXIT -> {
                    event.action = MotionEvent.ACTION_UP
                }
            }
            return onTouchEvent(event)
        }
        return true
    }

    override fun onTouchEvent(me: MotionEvent): Boolean {

        // Convert multi-pointer up/down events to single up/down events to
        // deal with the typical multi-pointer behavior of two-thumb typing
        val pointerCount: Int = me.getPointerCount()
        val action: Int = me.getAction()
        var result = false
        val now: Long = me.getEventTime()

        if (pointerCount != mOldPointerCount) {
            if (pointerCount == 1) {
                // Send a down event for the latest pointer
                val down = MotionEvent.obtain(
                    now, now, MotionEvent.ACTION_DOWN,
                    me.getX(), me.getY(), me.getMetaState()
                )
                result = onModifiedTouchEvent(down, false)
                down.recycle()
                // If it's an up action, then deliver the up as well.
                if (action == MotionEvent.ACTION_UP) {
                    result = onModifiedTouchEvent(me, true)
                }
            } else {
                // Send an up event for the last pointer
                val up = MotionEvent.obtain(
                    now, now, MotionEvent.ACTION_UP,
                    mOldPointerX, mOldPointerY, me.getMetaState()
                )
                result = onModifiedTouchEvent(up, true)
                up.recycle()
            }
        } else {
            if (pointerCount == 1) {
                result = onModifiedTouchEvent(me, false)
                mOldPointerX = me.getX()
                mOldPointerY = me.getY()
            } else {
                // Don't do anything when 2 pointers are down and moving.
                result = true
            }
        }
        mOldPointerCount = pointerCount

        return result
    }


    private fun onModifiedTouchEvent(me: MotionEvent, possiblePoly: Boolean): Boolean {
        var touchX = me.x.toInt() - paddingLeft
        var touchY = me.y.toInt() - paddingTop
        if (touchY >= -mVerticalCorrection) touchY += mVerticalCorrection
        val action = me.action
        val eventTime = me.eventTime
        val keyIndex = getKeyIndices(touchX, touchY, null)
        mPossiblePoly = possiblePoly

        // Track the last few movements to look for spurious swipes.
        if (action == MotionEvent.ACTION_DOWN) mSwipeTracker.clear()
        mSwipeTracker.addMovement(me)
        // Ignore all motion events until a DOWN.
        if (mGestureDetector!!.onTouchEvent(me)) {
            showPreview(NOT_A_KEY)
            mHandler!!.removeMessages(MSG_REPEAT)
            mHandler!!.removeMessages(MSG_LONGPRESS)
            return true
        }
        if (mAbortKey
            && action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_CANCEL && action != MotionEvent.ACTION_UP
        ) {
            return true
        }
        // Needs to be called after the gesture detector gets a turn, as it may have
        // displayed the mini keyboard
        if (mMiniKeyboardOnScreen && action == MotionEvent.ACTION_UP) {
            mKeyboardActionListener?.onText(mMiniKeyboard!!.getCurrentLongPressChar())
            dismissPopupKeyboard()
            return true
        }
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mAbortKey = false
                mStartX = touchX
                mStartY = touchY
                mLastCodeX = touchX
                mLastCodeY = touchY
                mLastKeyTime = 0
                mCurrentKeyTime = 0
                mLastKey = NOT_A_KEY
                mCurrentKey = keyIndex
                mDownKey = keyIndex
                mDownTime = me.eventTime
                mLastMoveTime = mDownTime
                checkMultiTap(eventTime, keyIndex)
                mKeyboardActionListener?.onPress(if (keyIndex != NOT_A_KEY) mKeys!![keyIndex].codes!![0] else 0)
                if (mCurrentKey >= 0 && mKeys!![mCurrentKey].repeatable) {
                    mRepeatKeyIndex = mCurrentKey
                    val msg =
                        mHandler!!.obtainMessage(MSG_REPEAT)
                    mHandler!!.sendMessageDelayed(
                        msg,
                        REPEAT_START_DELAY.toLong()
                    )
                    repeatKey()
                    // Delivering the key could have caused an abort
                    if (mAbortKey) {
                        mRepeatKeyIndex = NOT_A_KEY
                        return true
                    }
                }
                if (mCurrentKey != NOT_A_KEY) {
                    val msg = mHandler!!.obtainMessage(
                        MSG_LONGPRESS,
                        me
                    )
                    mHandler!!.sendMessageDelayed(
                        msg,
                        LONGPRESS_TIMEOUT.toLong()
                    )
                }
                showPreview(keyIndex)
            }
            MotionEvent.ACTION_MOVE -> {
                var continueLongPress = false
                if (keyIndex != NOT_A_KEY) {
                    if (mCurrentKey == NOT_A_KEY) {
                        mCurrentKey = keyIndex
                        mCurrentKeyTime = eventTime - mDownTime
                    } else {
                        if (keyIndex == mCurrentKey) {
                            mCurrentKeyTime += eventTime - mLastMoveTime
                            continueLongPress = true
                        } else if (mRepeatKeyIndex == NOT_A_KEY) {
                            resetMultiTap()
                            mLastKey = mCurrentKey
                            mLastCodeX = mLastX
                            mLastCodeY = mLastY
                            mLastKeyTime = mCurrentKeyTime + eventTime - mLastMoveTime
                            mCurrentKey = keyIndex
                            mCurrentKeyTime = 0
                        }
                    }
                }
                if (!continueLongPress) {
                    // Cancel old longpress
                    mHandler!!.removeMessages(MSG_LONGPRESS)
                    // Start new longpress if key has changed
                    if (keyIndex != NOT_A_KEY) {
                        val msg = mHandler!!.obtainMessage(
                            MSG_LONGPRESS,
                            me
                        )
                        mHandler!!.sendMessageDelayed(
                            msg,
                            LONGPRESS_TIMEOUT.toLong()
                        )
                    }
                }
                showPreview(mCurrentKey)
                mLastMoveTime = eventTime
            }
            MotionEvent.ACTION_UP -> {
                removeMessages()
                if (keyIndex == mCurrentKey) {
                    mCurrentKeyTime += eventTime - mLastMoveTime
                } else {
                    resetMultiTap()
                    mLastKey = mCurrentKey
                    mLastKeyTime = mCurrentKeyTime + eventTime - mLastMoveTime
                    mCurrentKey = keyIndex
                    mCurrentKeyTime = 0
                }
                if (mCurrentKeyTime < mLastKeyTime && mCurrentKeyTime < DEBOUNCE_TIME && mLastKey != NOT_A_KEY) {
                    mCurrentKey = mLastKey
                    touchX = mLastCodeX
                    touchY = mLastCodeY
                }
                showPreview(NOT_A_KEY)
                Arrays.fill(mKeyIndices, NOT_A_KEY)
                // If we're not on a repeating key (which sends on a DOWN event)
                invalidateKey(keyIndex)
                if (mRepeatKeyIndex == NOT_A_KEY && !mMiniKeyboardOnScreen && !mAbortKey) {
                    detectAndSendKey(mCurrentKey, touchX, touchY, eventTime)
                }
                mRepeatKeyIndex = NOT_A_KEY
            }
            MotionEvent.ACTION_CANCEL -> {
                removeMessages()
                dismissPopupKeyboard()
                mAbortKey = true
                showPreview(NOT_A_KEY)
                invalidateKey(mCurrentKey)
            }
        }
        mLastX = touchX
        mLastY = touchY
        return true
    }


    private fun repeatKey(): Boolean {
        val key = mKeys!![mRepeatKeyIndex]
        detectAndSendKey(mCurrentKey, key.x, key.y, mLastTapTime)
        return true
    }

    protected fun swipeRight() {
        mKeyboardActionListener?.swipeRight()
    }

    protected fun swipeLeft() {
        mKeyboardActionListener?.swipeLeft()
    }

    protected fun swipeUp() {
        mKeyboardActionListener?.swipeUp()
    }

    protected fun swipeDown() {
        mKeyboardActionListener?.swipeDown()
    }

    fun closing() {
        if (mPreviewPopup != null && mPreviewPopup!!.isShowing) {
            mPreviewPopup!!.dismiss()
        }
        removeMessages()
        dismissPopupKeyboard()
        mBuffer = null
        mCanvas = null
        if (mMiniKeyboardCache != null){
            mMiniKeyboardCache!!.clear()
        }
    }

    private fun removeMessages() {
        if (mHandler != null) {
            mHandler!!.removeMessages(MSG_REPEAT)
            mHandler!!.removeMessages(MSG_LONGPRESS)
            mHandler!!.removeMessages(MSG_SHOW_PREVIEW)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        closing()
    }


    private fun dismissPopupKeyboard() {
        if (mPopupKeyboard != null && mPopupKeyboard!!.isShowing) {
            mPopupKeyboard!!.dismiss()
            mMiniKeyboardOnScreen = false
            mMiniKeyboard!!.clearActive()
            invalidateAllKeys()
        }
    }

    fun handleBack(): Boolean {
        if (mPopupKeyboard!!.isShowing) {
            dismissPopupKeyboard()
            return true
        }
        return false
    }

    private fun resetMultiTap() {
        mLastSentIndex = NOT_A_KEY
        mTapCount = 0
        mLastTapTime = -1
        mInMultiTap = false
    }

    private fun checkMultiTap(eventTime: Long, keyIndex: Int) {
        if (keyIndex == NOT_A_KEY) return
        val key = mKeys!![keyIndex]
        if (key.codes!!.size > 1) {
            mInMultiTap = true
            if (eventTime < mLastTapTime + MULTITAP_INTERVAL && keyIndex == mLastSentIndex && !key.isSpace()) {
                mTapCount = (mTapCount + 1) % key.codes!!.size
                return
            } else {
                mTapCount = -1
                return
            }
        }
        if (eventTime > mLastTapTime + MULTITAP_INTERVAL || keyIndex != mLastSentIndex) {
            resetMultiTap()
        }
    }

    fun lockShift() {
        mKeyboard!!.lockShift()
        invalidateAllKeys()
    }

    fun isLockShift(): Boolean {
        return mKeyboard!!.isLockShift()
    }

    private var lastEnterMode = -1
    fun setEnterActionMode(enterActionMode: Int) {
        if (lastEnterMode == enterActionMode) return
        mCurrentEnterMode = if (enterActionMode > 0) enterActionMode else 0
        val enterKey = mKeyboard!!.getEnterKey()
        Log.d(TAG, "setEnterActionMode: enterActionMode=$enterActionMode")
        if (mCurrentEnterMode <= 1 || mCurrentEnterMode == 4 || mCurrentEnterMode == 6) {
            enterKey!!.icon = resources.getDrawable(R.drawable.icon_enter)
            enterKey.label = null
        } else {
            val enterActionTextId = enterKey!!.getEnterActionTextStringId(mCurrentEnterMode)
            enterKey.icon = null
            enterKey.label = context.resources.getString(enterActionTextId)
        }
        invalidateKey(enterKey)
        lastEnterMode = enterActionMode
    }


    private class SwipeTracker {
        val mPastX = FloatArray(NUM_PAST)
        val mPastY = FloatArray(NUM_PAST)
        val mPastTime = LongArray(NUM_PAST)
        var yVelocity = 0f
        var xVelocity = 0f

        fun clear() {
            mPastTime[0] = 0
        }

        fun addMovement(ev: MotionEvent) {
            val time = ev.eventTime
            val N = ev.historySize
            for (i in 0 until N) {
                addPoint(
                    ev.getHistoricalX(i), ev.getHistoricalY(i),
                    ev.getHistoricalEventTime(i)
                )
            }
            addPoint(ev.x, ev.y, time)
        }

        private fun addPoint(x: Float, y: Float, time: Long) {
            var drop = -1
            var i: Int
            val pastTime = mPastTime
            i = 0
            while (i < NUM_PAST) {
                if (pastTime[i] == 0L) {
                    break
                } else if (pastTime[i] < time - LONGEST_PAST_TIME) {
                    drop = i
                }
                i++
            }
            if (i == NUM_PAST && drop < 0) {
                drop = 0
            }
            if (drop == i) drop--
            val pastX = mPastX
            val pastY = mPastY
            if (drop >= 0) {
                val start = drop + 1
                val count = NUM_PAST - drop - 1
                System.arraycopy(pastX, start, pastX, 0, count)
                System.arraycopy(pastY, start, pastY, 0, count)
                System.arraycopy(pastTime, start, pastTime, 0, count)
                i -= drop + 1
            }
            pastX[i] = x
            pastY[i] = y
            pastTime[i] = time
            i++
            if (i < NUM_PAST) {
                pastTime[i] = 0
            }
        }

        @JvmOverloads
        fun computeCurrentVelocity(units: Int, maxVelocity: Float = Float.MAX_VALUE) {
            val pastX = mPastX
            val pastY = mPastY
            val pastTime = mPastTime
            val oldestX = pastX[0]
            val oldestY = pastY[0]
            val oldestTime = pastTime[0]
            var accumX = 0f
            var accumY = 0f
            var N = 0
            while (N < NUM_PAST) {
                if (pastTime[N] == 0L) {
                    break
                }
                N++
            }
            for (i in 1 until N) {
                val dur = (pastTime[i] - oldestTime).toInt()
                if (dur == 0) continue
                var dist = pastX[i] - oldestX
                var vel = dist / dur * units // pixels/frame.
                accumX = if (accumX == 0f) vel else (accumX + vel) * .5f
                dist = pastY[i] - oldestY
                vel = dist / dur * units // pixels/frame.
                accumY = if (accumY == 0f) vel else (accumY + vel) * .5f
            }
            xVelocity =
                if (accumX < 0.0f) Math.max(accumX, -maxVelocity) else Math.min(accumX, maxVelocity)
            yVelocity =
                if (accumY < 0.0f) Math.max(accumY, -maxVelocity) else Math.min(accumY, maxVelocity)
        }

        companion object {
            const val NUM_PAST = 4
            const val LONGEST_PAST_TIME = 200
        }
    }
}
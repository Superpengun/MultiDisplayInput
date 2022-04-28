package com.zqy.hci.widget

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.util.Xml
import androidx.annotation.XmlRes
import com.zqy.hci.R
import com.zqy.hci.bean.FunctionKeyCode
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * @Author:CWQ
 * @DATE:2022/1/20
 * @DESC:
 */
class Keyboard {


    /**
     * Horizontal gap default for all rows
     */
    private var mDefaultHorizontalGap = 0

    /**
     * Default key width
     */
    private var mDefaultWidth = 0

    /**
     * Default key height
     */
    private var mDefaultHeight = 0

    /**
     * Default gap between rows
     */
    private var mDefaultVerticalGap = 0

    /**
     * Is the keyboard in the shifted state
     */
    private var mShifted = false

    /**
     * Key instance for the shift key, if present
     */
    private var mShiftKeys: Array<Key?> = arrayOf(null, null)

    /**
     * Key index for the shift key, if present
     */
    private var mShiftKeyIndices = intArrayOf(-1, -1)

    /**
     * Current key width, while loading the keyboard
     */
    private var mKeyWidth = 0

    /**
     * Current key height, while loading the keyboard
     */
    private var mKeyHeight = 0

    /**
     * Total height of the keyboard, including the padding and keys
     */
    private var mTotalHeight = 0

    /**
     * Total width of the keyboard, including left side gaps and keys, but not any gaps on the
     * right side.
     */
    private var mTotalWidth = 0

    /**
     * List of keys in this keyboard
     */
    private var mKeys: ArrayList<Key>? = null

    /**
     * List of modifier keys such as Shift & Alt, if any
     */
    private var mModifierKeys: ArrayList<Key>? = null

    /**
     * Width of the screen available to fit the keyboard
     */
    private var mDisplayWidth = 0

    /**
     * Height of the screen
     */
    private var mDisplayHeight = 0

    /**
     * Keyboard mode, or zero, if none.
     */
    private var mKeyboardMode = 0

    // Variables for pre-computing nearest keys.

    // Variables for pre-computing nearest keys.
    private var mCellWidth = 0
    private var mCellHeight = 0
    private var mGridNeighbors: Array<IntArray?>? = null
    private var mProximityThreshold = 0

    /**
     * Number of key widths from current touch point to search for nearest keys.
     */
    private var SEARCH_DISTANCE = 1.8f

    private val rows: ArrayList<Row> = ArrayList<Row>()
    private var mAltstate = false
    private var mEnterKey: Key? = null
    private var spaceKey: Key? = null
    private var isDaiKb = false

    fun setMkbName(mkbName: String?) {
        isDaiKb = !TextUtils.isEmpty(mkbName) && mkbName!!.contains("_dai")
    }

    fun isDaiKeyboard() = isDaiKb


    /**
     * Container for keys in the keyboard. All keys in a row are at the same Y-coordinate.
     * Some of the key size defaults can be overridden per row from what the {@link Keyboard}
     * defines.
     *
     * @attr ref android.R.styleable#Keyboard_keyWidth
     * @attr ref android.R.styleable#Keyboard_keyHeight
     * @attr ref android.R.styleable#Keyboard_horizontalGap
     * @attr ref android.R.styleable#Keyboard_verticalGap
     * @attr ref android.R.styleable#Keyboard_Row_rowEdgeFlags
     * @attr ref android.R.styleable#Keyboard_Row_keyboardMode
     */
    class Row {
        /**
         * Default width of a key in this row.
         */
        var defaultWidth = 0

        /**
         * Default height of a key in this row.
         */
        var defaultHeight = 0

        /**
         * Default horizontal gap between keys in this row.
         */
        var defaultHorizontalGap = 0

        /**
         * Vertical gap following this row.
         */
        var verticalGap = 0

        var mKeys: ArrayList<Key> = ArrayList<Key>()

        /**
         * Edge flags for this row of keys. Possible values that can be assigned are
         * [EDGE_TOP][Keyboard.EDGE_TOP] and [EDGE_BOTTOM][Keyboard.EDGE_BOTTOM]
         */
        var rowEdgeFlags = 0

        /**
         * The keyboard mode for this row
         */
        var mode = 0

        var parent: Keyboard? = null

        constructor(parent: Keyboard) {
            this.parent = parent
        }

        constructor(res: Resources, parent: Keyboard, parser: XmlResourceParser) {
            this.parent = parent
            var a = res.obtainAttributes(
                Xml.asAttributeSet(parser),
                R.styleable.Keyboard
            )
            defaultWidth = getDimensionOrFraction(
                a,
                R.styleable.Keyboard_keyWidth,
                parent.mDisplayWidth, parent.mDefaultWidth
            )
            defaultHeight = getDimensionOrFraction(
                a,
                R.styleable.Keyboard_keyHeight,
                parent.mDisplayHeight, parent.mDefaultHeight
            )
            defaultHorizontalGap = getDimensionOrFraction(
                a,
                R.styleable.Keyboard_horizontalGap,
                parent.mDisplayWidth, parent.mDefaultHorizontalGap
            )
            verticalGap = getDimensionOrFraction(
                a,
                R.styleable.Keyboard_verticalGap,
                parent.mDisplayHeight, parent.mDefaultVerticalGap
            )
            a.recycle()
            a = res.obtainAttributes(
                Xml.asAttributeSet(parser),
                R.styleable.Keyboard_Row
            )
            rowEdgeFlags = a.getInt(R.styleable.Keyboard_Row_rowEdgeFlags, 0)
            mode = a.getInt(
                R.styleable.Keyboard_Row_keyboardMode,
                0
            )
        }
    }


    /**
     * Class for describing the position and characteristics of a single key in the keyboard.
     *
     * @attr ref android.R.styleable#Keyboard_keyWidth
     * @attr ref android.R.styleable#Keyboard_keyHeight
     * @attr ref android.R.styleable#Keyboard_horizontalGap
     * @attr ref android.R.styleable#Keyboard_Key_codes
     * @attr ref android.R.styleable#Keyboard_Key_keyIcon
     * @attr ref android.R.styleable#Keyboard_Key_keyLabel
     * @attr ref android.R.styleable#Keyboard_Key_iconPreview
     * @attr ref android.R.styleable#Keyboard_Key_isSticky
     * @attr ref android.R.styleable#Keyboard_Key_isRepeatable
     * @attr ref android.R.styleable#Keyboard_Key_isModifier
     * @attr ref android.R.styleable#Keyboard_Key_popupKeyboard
     * @attr ref android.R.styleable#Keyboard_Key_popupCharacters
     * @attr ref android.R.styleable#Keyboard_Key_keyOutputText
     * @attr ref android.R.styleable#Keyboard_Key_keyEdgeFlags
     */
    class Key {
        companion object {
            private val KEY_STATE_NORMAL_ON = intArrayOf(
                android.R.attr.state_checkable,
                android.R.attr.state_checked
            )

            private val KEY_STATE_PRESSED_ON = intArrayOf(
                android.R.attr.state_pressed,
                android.R.attr.state_checkable,
                android.R.attr.state_checked
            )

            private val KEY_STATE_NORMAL_OFF = intArrayOf(
                android.R.attr.state_checkable
            )

            private val KEY_STATE_PRESSED_OFF = intArrayOf(
                android.R.attr.state_pressed,
                android.R.attr.state_checkable,
                android.R.attr.state_checked
            )

            private val KEY_STATE_PRESSED_ACTIVE = intArrayOf(
                android.R.attr.state_pressed,
                android.R.attr.state_activated,
                android.R.attr.state_active
            )

            private val KEY_STATE_NORMAL_ACTIVE = intArrayOf(
                android.R.attr.state_activated,
                android.R.attr.state_active
            )

            private val KEY_STATE_PRESSED_UNACTIVE = intArrayOf(
                android.R.attr.state_pressed,
                android.R.attr.state_activated
            )

            private val KEY_STATE_NORMAL_UNACTIVE = intArrayOf(
                android.R.attr.state_activated
            )


            private val KEY_STATE_NORMAL = intArrayOf()

            private val KEY_STATE_PRESSED = intArrayOf(
                android.R.attr.state_pressed
            )
            private val ENTER_ICON_ID = intArrayOf(
                R.drawable.icon_enter,
                R.drawable.icon_go,
                R.drawable.icon_search,
                R.drawable.icon_enter,
                R.drawable.icon_next,
                R.drawable.icon_enter,
                R.drawable.icon_last,
                R.drawable.icon_confirm
            )
            private val ENTER_TEXT_ID = intArrayOf(
                R.string.confirm,
                R.string.go,
                R.string.search,
                R.string.confirm,
                R.string.next,
                R.string.confirm,
                R.string.last,
                R.string.confirm
            )
        }

        /**
         * All the key codes (unicode or custom code) that this key could generate, zero'th
         * being the most important.
         */
        var codes: IntArray? = null

        /**
         * Label to display
         */
        var label: CharSequence? = null

        /**
         * Label to display
         */
        var label2: CharSequence? = null

        /**
         * Small Label to display
         */
        var smallLabel: CharSequence? = null

        /**
         * Icon to display instead of a label. Icon takes precedence over a label
         */
        var icon: Drawable? = null

        /**
         * Preview version of the icon, for the preview popup
         */
        var iconPreview: Drawable? = null

        /**
         * Width of the key, not including the gap
         */
        var width = 0

        /**
         * Height of the key, not including the gap
         */
        var height = 0

        /**
         * The horizontal gap before this key
         */
        var gap = 0

        /**
         * Whether this key is sticky, i.e., a toggle key
         */
        var sticky = false

        /**
         * state of the sticky key
         **/
        var stickyState = false

        /**
         * state of the active key
         */
        var active = false

        /**
         * state of the active key
         */
        var activeAble = false

        /**
         * X coordinate of the key in the keyboard layout
         */
        var x = 0

        /**
         * Y coordinate of the key in the keyboard layout
         */
        var y = 0

        /**
         * The current pressed state of this key
         */
        var pressed = false

        /**
         * If this is a sticky key, is it on?
         */
        var on = false

        /**
         * Text to output when pressed. This can be multiple characters, like ".com"
         */
        var text: CharSequence? = null

        /**
         * Popup characters
         */
        var popupCharacters: CharSequence? = null

        /**
         * Popup characters
         */
        var defaultPopSelectIndex = 0

        /**
         * Background type Function Normal
         */
        var backgroundType = 0

        /**
         * Flags that specify the anchoring to edges of the keyboard for detecting touch events
         * that are just out of the boundary of the key. This is a bit mask of
         * [Keyboard.EDGE_LEFT], [Keyboard.EDGE_RIGHT], [Keyboard.EDGE_TOP] and
         * [Keyboard.EDGE_BOTTOM].
         */
        var edgeFlags = 0

        /**
         * Whether this is a modifier key, such as Shift or Alt
         */
        var modifier = false

        /**
         * The keyboard that this key belongs to
         */
        private var keyboard: Keyboard? = null

        /**
         * If this key pops up a mini keyboard, this is the resource id for the XML layout for that
         * keyboard.
         */
        var popupResId = 0

        /**
         * Whether this key repeats itself when held down
         */
        var repeatable = false


        private val mEnterActionDrawableId = 0


        /**
         * Create an empty key with no attributes.
         */
        constructor(parent: Row) {
            keyboard = parent.parent
            height = parent.defaultHeight
            width = parent.defaultWidth
            gap = parent.defaultHorizontalGap
            edgeFlags = parent.rowEdgeFlags
        }

        /**
         * Create a key with the given top-left coordinate and extract its attributes from
         * the XML parser.
         *
         * @param res    resources associated with the caller's context
         * @param parent the row that this key belongs to. The row must already be attached to
         *               a {@link Keyboard}.
         * @param x      the x coordinate of the top-left
         * @param y      the y coordinate of the top-left
         * @param parser the XML parser containing the attributes for this key
         */
        constructor(res: Resources, parent: Row, x: Int, y: Int, parser: XmlResourceParser) {
            keyboard = parent.parent
            height = parent.defaultHeight
            width = parent.defaultWidth
            gap = parent.defaultHorizontalGap
            edgeFlags = parent.rowEdgeFlags

            this.x = x
            this.y = y

            var a = res.obtainAttributes(
                Xml.asAttributeSet(parser),
                R.styleable.Keyboard
            )

            width = getDimensionOrFraction(
                a,
                R.styleable.Keyboard_keyWidth,
                keyboard!!.mDisplayWidth, parent.defaultWidth
            )
            height = getDimensionOrFraction(
                a,
                R.styleable.Keyboard_keyHeight,
                keyboard!!.mDisplayHeight, parent.defaultHeight
            )
            gap = getDimensionOrFraction(
                a,
                R.styleable.Keyboard_horizontalGap,
                keyboard!!.mDisplayWidth, parent.defaultHorizontalGap
            )
            a.recycle()
            a = res.obtainAttributes(
                Xml.asAttributeSet(parser),
                R.styleable.Keyboard_Key
            )
            this.x += gap
            val codesValue = TypedValue()
            a.getValue(
                R.styleable.Keyboard_Key_codes,
                codesValue
            )
            if (codesValue.type == TypedValue.TYPE_INT_DEC
                || codesValue.type == TypedValue.TYPE_INT_HEX
            ) {
                codes = intArrayOf(codesValue.data)
            } else if (codesValue.type == TypedValue.TYPE_STRING) {
                codes = parseCSV(codesValue.string.toString())
            }

            iconPreview = a.getDrawable(R.styleable.Keyboard_Key_iconPreview)
            if (iconPreview != null) {
                iconPreview!!.setBounds(
                    0, 0, iconPreview!!.intrinsicWidth,
                    iconPreview!!.intrinsicHeight
                )
            }
            popupCharacters = a.getText(
                R.styleable.Keyboard_Key_popupCharacters
            )
            defaultPopSelectIndex = a.getInt(
                R.styleable.Keyboard_Key_defaultPopSelectIndex, 0
            )
            popupResId = a.getResourceId(
                R.styleable.Keyboard_Key_popupKeyboard, 0
            )
            repeatable = a.getBoolean(
                R.styleable.Keyboard_Key_isRepeatable, false
            )
            modifier = a.getBoolean(
                R.styleable.Keyboard_Key_isModifier, false
            )
            sticky = a.getBoolean(
                R.styleable.Keyboard_Key_isSticky, false
            )
            stickyState = a.getBoolean(
                R.styleable.Keyboard_Key_stickyState, false
            )

            activeAble = a.getBoolean(
                R.styleable.Keyboard_Key_isActive, false
            )

            on = stickyState

            edgeFlags = a.getInt(R.styleable.Keyboard_Key_keyEdgeFlags, 0)
            edgeFlags = edgeFlags or parent.rowEdgeFlags

            icon = a.getDrawable(
                R.styleable.Keyboard_Key_keyIcon
            )
            if (icon != null) {
                icon!!.setBounds(0, 0, icon!!.intrinsicWidth, icon!!.intrinsicHeight)
            }
            label = a.getText(R.styleable.Keyboard_Key_keyLabel)
            label2 = a.getText(R.styleable.Keyboard_Key_keyLabel2)
            smallLabel = a.getText(R.styleable.Keyboard_Key_keySmallLabel)
            text = a.getText(R.styleable.Keyboard_Key_keyOutputText)
            backgroundType = a.getInt(R.styleable.Keyboard_Key_backgroundType, 1)


            if (codes == null && !TextUtils.isEmpty(label)) {
                codes = intArrayOf(label.toString()[0].code)
            }
            a.recycle()
        }

        /**
         * Informs the key that it has been pressed, in case it needs to change its appearance or
         * state.
         *
         * @see #onReleased(boolean)
         */
        fun onPressed() {
            pressed = !pressed
        }

        /**
         * Changes the pressed state of the key.
         * <p>
         * <p>Toggled state of the key will be flipped when all the following conditions are
         * fulfilled:</p>
         * <p>
         * <ul>
         * <li>This is a sticky key, that is, {@link #sticky} is {@code true}.
         * <li>The parameter {@code inside} is {@code true}.
         * <li>{@link android.os.Build.VERSION#SDK_INT} is greater than
         * {@link android.os.Build.VERSION_CODES#LOLLIPOP_MR1}.
         * </ul>
         *
         * @param inside whether the finger was released inside the key. Works only on Android M and
         *               later. See the method document for details.
         * @see #onPressed()
         */
        fun onReleased(inside: Boolean) {
            pressed = !pressed
            if (sticky && !activeAble && inside) {
                on = !on
            } else if (sticky && activeAble && inside) {
                active = !active
            }
        }


        fun parseCSV(value: String): IntArray {
            var count = 0
            var lastIndex = 0
            if (value.isNotEmpty()) {
                count++
                while (value.indexOf(",", lastIndex + 1).also { lastIndex = it } > 0) {
                    count++
                }
            }
            val values = IntArray(count)
            count = 0
            val st = StringTokenizer(value, ",")
            while (st.hasMoreTokens()) {
                try {
                    values[count++] = st.nextToken().toInt()
                } catch (nfe: NumberFormatException) {
                    Log.e(
                        TAG,
                        "Error parsing keycodes $value"
                    )
                }
            }
            return values
        }

        /**
         * Detects if a point falls inside this key.
         *
         * @param x the x-coordinate of the point
         * @param y the y-coordinate of the point
         * @return whether or not the point falls inside the key. If the key is attached to an edge,
         * it will assume that all points between the key and the edge are considered to be inside
         * the key.
         */
        fun isInside(x: Int, y: Int): Boolean {
            val leftEdge = edgeFlags and EDGE_LEFT > 0
            val rightEdge = edgeFlags and EDGE_RIGHT > 0
            val topEdge = edgeFlags and EDGE_TOP > 0
            val bottomEdge =
                edgeFlags and EDGE_BOTTOM > 0
            return ((x >= this.x || leftEdge && x <= this.x + width)
                    && (x < this.x + width || rightEdge && x >= this.x)
                    && (y >= this.y || topEdge && y <= this.y + height)
                    && (y < this.y + height || bottomEdge && y >= this.y))
        }

        /**
         * Returns the square of the distance between the center of the key and the given point.
         *
         * @param x the x-coordinate of the point
         * @param y the y-coordinate of the point
         * @return the square of the distance of the point from the center of the key
         */
        fun squaredDistanceFrom(x: Int, y: Int): Int {
            var xDist = this.x + width / 2 - x
            val yDist = this.y + height / 2 - y
            if (isSpace()) {
                val temp = Math.min(Math.abs(this.x - x), Math.abs(this.x + width - x))
                if (Math.abs(xDist) > temp) {
                    xDist = temp
                }
            }
            return xDist * xDist + yDist * yDist
        }

        /**
         * Returns the drawable state for the key, based on the current state and type of the key.
         *
         * @return the drawable state of the key.
         * @see android.graphics.drawable.StateListDrawable.setState
         */
        fun getCurrentDrawableState(): IntArray {
            var states: IntArray = KEY_STATE_NORMAL
            if (on) {
                states = if (pressed) {
                    KEY_STATE_PRESSED_ON
                } else {
                    KEY_STATE_NORMAL_ON
                }
            } else {
                if (activeAble) {
                    if (active) {
                        states = if (pressed) {
                            KEY_STATE_PRESSED_ACTIVE
                        } else {
                            KEY_STATE_NORMAL_ACTIVE
                        }
                    } else if (!active) {
                        states = if (pressed) {
                            KEY_STATE_PRESSED_UNACTIVE
                        } else {
                            KEY_STATE_NORMAL_UNACTIVE
                        }
                    }
                } else {
                    states = if (pressed) {
                        KEY_STATE_PRESSED
                    } else {
                        KEY_STATE_NORMAL_UNACTIVE
                    }
                }
            }
            return states
        }

        fun chooseBackground(
            normalBackground: Drawable?,
            functionBackground: Drawable?
        ): Drawable? {
            return if (backgroundType == 1) normalBackground else functionBackground
        }

        fun chooseLabelColor(normalColor: Int, functionColor: Int): Int {
            return if (backgroundType == 1) normalColor else functionColor
        }

        fun isSpace(): Boolean {
            return codes!![0] == FunctionKeyCode.KEY_SPACE
        }


        fun isEnter(): Boolean {
            return codes!![0] == FunctionKeyCode.KEY_ENTER
        }

        fun getEnterActionDrawableId(action: Int): Int {
            return R.drawable.icon_enter
        }

        fun getEnterActionTextStringId(mCurrentEnterMode: Int): Int {
            return ENTER_TEXT_ID[mCurrentEnterMode - 1]
        }
    }

    /**
     * Creates a keyboard from the given xml key layout file.
     *
     * @param context        the application or service context
     * @param xmlLayoutResId the resource file that contains the keyboard layout and keys.
     */
    constructor(context: Context, @XmlRes xmlLayoutResId: Int) : this(context, xmlLayoutResId, 0)

    /**
     * Creates a keyboard from the given xml key layout file. Weeds out rows
     * that have a keyboard mode defined but don't match the specified mode.
     *
     * @param context        the application or service context
     * @param xmlLayoutResId the resource file that contains the keyboard layout and keys.
     * @param modeId         keyboard mode identifier
     */
    constructor(
        context: Context,
        @XmlRes xmlLayoutResId: Int,
        modeId: Int
    ) {
        val dm = context.resources.displayMetrics
        mDisplayWidth = dm.widthPixels
        mDisplayHeight = dm.heightPixels

        mDefaultHorizontalGap = 0
        mDefaultWidth = mDisplayWidth / 10
        mDefaultVerticalGap = 0
        mDefaultHeight = mDefaultWidth
        mKeys = arrayListOf()
        mModifierKeys = ArrayList()
        mKeyboardMode = modeId
        loadKeyboard(context, context.resources.getXml(xmlLayoutResId))
    }

    /**
     * Creates a keyboard from the given xml key layout file. Weeds out rows
     * that have a keyboard mode defined but don't match the specified mode.
     *
     * @param context        the application or service context
     * @param xmlLayoutResId the resource file that contains the keyboard layout and keys.
     * @param modeId         keyboard mode identifier
     * @param width          sets width of keyboard
     * @param height         sets height of keyboard
     */
    constructor(
        context: Context,
        @XmlRes xmlLayoutResId: Int,
        modeId: Int,
        width: Int,
        height: Int
    ) {
        mDisplayWidth = width
        mDisplayHeight = height

        mDefaultHorizontalGap = 0
        mDefaultWidth = mDisplayWidth / 10
        mDefaultVerticalGap = 0
        mDefaultHeight = mDefaultWidth
        mKeys = arrayListOf()
        mModifierKeys = ArrayList()
        mKeyboardMode = modeId
        loadKeyboard(context, context.resources.getXml(xmlLayoutResId))
    }

    /**
     * <p>Creates a blank keyboard from the given resource file and populates it with the specified
     * characters in left-to-right, top-to-bottom fashion, using the specified number of columns.
     * </p>
     * <p>If the specified number of columns is -1, then the keyboard will fit as many keys as
     * possible in each row.</p>
     *
     * @param context             the application or service context
     * @param layoutTemplateResId the layout template file, containing no keys.
     * @param characters          the list of characters to display on the keyboard. One key will be created
     *                            for each character.
     * @param columns             the number of columns of keys to display. If this number is greater than the
     *                            number of keys that can fit in a row, it will be ignored. If this number is -1, the
     *                            keyboard will fit as many keys as possible in each row.
     */
    constructor(
        context: Context,
        layoutTemplateResId: Int,
        characters: CharSequence,
        columns: Int,
        horizontalPadding: Int
    ) : this(context, layoutTemplateResId) {
        var x = 0
        var y = 0
        var column = 0
        mTotalWidth = 0

        val row: Row = Row(this)
        row.defaultHeight = mDefaultHeight
        row.defaultWidth = mDefaultWidth
        row.defaultHorizontalGap = mDefaultHorizontalGap
        row.verticalGap = mDefaultVerticalGap
        row.rowEdgeFlags = EDGE_TOP or EDGE_BOTTOM
        val maxColumns = if (columns == -1) Int.MAX_VALUE else columns
        for (i in 0 until characters.length) {
            val c = characters[i]
            if (column >= maxColumns
                || x + mDefaultWidth + horizontalPadding > mDisplayWidth
            ) {
                x = 0
                y += mDefaultVerticalGap + mDefaultHeight
                column = 0
            }
            val key: Key = Key(row)
            key.x = x
            key.y = y
            key.label = c.toString()
            key.codes = intArrayOf(c.toInt())
            column++
            x += key.width + key.gap
            mKeys?.add(key)
            row.mKeys.add(key)
            if (x > mTotalWidth) {
                mTotalWidth = x
            }
        }
        mTotalHeight = y + mDefaultHeight
        rows.add(row)
    }

    fun resize(newWidth: Int, newHeight: Int) {
        val numRows = rows.size
        for (rowIndex in 0 until numRows) {
            val row: Row = rows[rowIndex]
            val numKeys: Int = row.mKeys.size
            var totalGap = 0
            var totalWidth = 0
            for (keyIndex in 0 until numKeys) {
                val key: Key = row.mKeys.get(keyIndex)
                if (keyIndex > 0) {
                    totalGap += key.gap
                }
                totalWidth += key.width
            }
            if (totalGap + totalWidth > newWidth) {
                var x = 0
                val scaleFactor = (newWidth - totalGap).toFloat() / totalWidth
                for (keyIndex in 0 until numKeys) {
                    val key: Key =
                        row.mKeys.get(keyIndex)
                    key.width *= scaleFactor.toInt()
                    key.x = x
                    x += key.width + key.gap
                }
            }
        }
        mTotalWidth = newWidth
    }

    fun getKeys(): List<Key>? = mKeys

    fun getModifierKeys(): List<Key>? = mModifierKeys

    protected fun getHorizontalGap() = mDefaultHorizontalGap

    protected fun setHorizontalGap(gap: Int) {
        mDefaultHorizontalGap = gap
    }

    protected fun getVerticalGap(): Int {
        return mDefaultVerticalGap
    }

    protected fun setVerticalGap(gap: Int) {
        mDefaultVerticalGap = gap
    }

    protected fun getKeyHeight(): Int {
        return mDefaultHeight
    }

    protected fun setKeyHeight(height: Int) {
        mDefaultHeight = height
    }

    protected fun getKeyWidth(): Int {
        return mDefaultWidth
    }

    protected fun setKeyWidth(width: Int) {
        mDefaultWidth = width
    }

    /**
     * Returns the total height of the keyboard
     *
     * @return the total height of the keyboard
     */
    fun getHeight(): Int {
        return mTotalHeight
    }

    fun getMinWidth(): Int {
        return mTotalWidth
    }

    fun setShifted(shiftState: Boolean): Boolean {
        for (shiftKey in mShiftKeys) {
            if (shiftKey != null) {
                if (shiftState) {
                    shiftKey.active = true
                } else {
                    shiftKey.on = false
                    shiftKey.active = false
                }
            }
        }
        if (mShifted != shiftState) {
            mShifted = shiftState
            return true
        }
        return false
    }

    fun getShiftMode(): Int {
        val shiftKey: Key? = mShiftKeys[0]
        return if (shiftKey != null) {
            if (!shiftKey.active && !shiftKey.on) {
                0
            } else if (!shiftKey.active) {
                2
            } else {
                1
            }
        } else {
            0
        }
    }

    fun isShifted(): Boolean {
        return mShifted
    }

    fun lockShift() {
        for (shiftKey in mShiftKeys) {
            if (shiftKey != null) {
                shiftKey.on = true
                shiftKey.active = false
            }
        }
        mShifted = true
    }

    fun isLockShift(): Boolean {
        for (shiftKey in mShiftKeys) {
            if (shiftKey != null) {
                Log.d(TAG, "shiftKey.on:" + shiftKey.on)
                return shiftKey.on
            }
        }
        return false
    }

    fun setAltKey(altState: Boolean): Boolean {
        for (altKey in mModifierKeys!!) {
            if (altKey != null) {
                altKey.on = altState
            }
        }
        if (mAltstate != altState) {
            mAltstate = altState
            return true
        }
        return false
    }

    fun getAltState(): Boolean {
        return mAltstate
    }

    /**
     * @hide
     */
    fun getShiftKeyIndices(): IntArray? {
        return mShiftKeyIndices
    }

    fun getShiftKeyIndex(): Int {
        return mShiftKeyIndices[0]
    }

    fun getSpaceKey(): Key? {
        return spaceKey
    }

    fun getEnterKey(): Key? {
        return mEnterKey
    }


    private fun computeNearestNeighbors() {
        // Round-up so we don't have any pixels outside the grid
        mCellWidth =
            (getMinWidth() + GRID_WIDTH - 1) / GRID_WIDTH
        mCellHeight =
            (getHeight() + GRID_HEIGHT - 1) / GRID_HEIGHT
        mGridNeighbors = arrayOfNulls(GRID_SIZE)
        val indices = IntArray(mKeys!!.size)
        val gridWidth: Int = GRID_WIDTH * mCellWidth
        val gridHeight: Int = GRID_HEIGHT * mCellHeight
        var x = 0
        while (x < gridWidth) {
            var y = 0
            while (y < gridHeight) {
                var count = 0
                for (i in mKeys!!.indices) {
                    val key: Key = mKeys!![i]
                    if (key.squaredDistanceFrom(
                            x,
                            y
                        ) < mProximityThreshold || key.squaredDistanceFrom(
                            x + mCellWidth - 1,
                            y
                        ) < mProximityThreshold || (key.squaredDistanceFrom(
                            x + mCellWidth - 1,
                            y + mCellHeight - 1
                        )
                                < mProximityThreshold) || key.squaredDistanceFrom(
                            x,
                            y + mCellHeight - 1
                        ) < mProximityThreshold
                    ) {
                        indices[count++] = i
                    }
                }
                val cell = IntArray(count)
                System.arraycopy(indices, 0, cell, 0, count)
                mGridNeighbors!![y / mCellHeight * GRID_WIDTH + x / mCellWidth] = cell
                y += mCellHeight
            }
            x += mCellWidth
        }
    }

    /**
     * Returns the indices of the keys that are closest to the given point.
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return the array of integer indices for the nearest keys to the given point. If the given
     * point is out of range, then an array of size zero is returned.
     */
    fun getNearestKeys(x: Int, y: Int): IntArray? {
        if (mGridNeighbors == null) computeNearestNeighbors()
        if (x >= 0 && x < getMinWidth() && y >= 0 && y < getHeight()) {
            val index: Int =
                y / mCellHeight * GRID_WIDTH + x / mCellWidth
            if (index < GRID_SIZE) {
                return mGridNeighbors!![index]
            }
        }
        return IntArray(0)
    }

    protected fun createRowFromXml(
        res: Resources,
        parser: XmlResourceParser
    ): Row {
        return Row(res, this, parser)
    }

    protected fun createKeyFromXml(
        res: Resources, parent: Row, x: Int, y: Int,
        parser: XmlResourceParser
    ): Key {
        return Key(res, parent, x, y, parser)
    }

    private fun loadKeyboard(context: Context, parser: XmlResourceParser) {
        var inKey = false
        var inRow = false
        val leftMostKey = false
        var row = 0
        var x = 0
        var y = 0
        var key: Key? = null
        var currentRow: Row? = null
        val res = context.resources
        var skipRow = false
        try {
            var event: Int
            while (parser.next().also { event = it } != XmlResourceParser.END_DOCUMENT) {
                if (event == XmlResourceParser.START_TAG) {
                    val tag = parser.name
                    if (TAG_ROW == tag) {
                        inRow = true
                        x = 0
                        currentRow = createRowFromXml(res, parser)
                        rows.add(currentRow)
                        skipRow = currentRow.mode != 0 && currentRow.mode != mKeyboardMode
                        if (skipRow) {
                            skipToEndOfRow(parser)
                            inRow = false
                        }
                    } else if (TAG_KEY == tag) {
                        inKey = true
                        key = createKeyFromXml(res, currentRow!!, x, y, parser)
                        mKeys!!.add(key)
                        if (key.codes!!.get(0) == KEYCODE_SHIFT) {
                            // Find available shift key slot and put this shift key in it
                            for (i in mShiftKeys.indices) {
                                if (mShiftKeys[i] == null) {
                                    mShiftKeys[i] = key
                                    mShiftKeyIndices[i] = mKeys!!.size - 1
                                    break
                                }
                            }
                            //                            mModifierKeys.add(key);
                        } else if (key.codes!!.get(0) == KEYCODE_ALT) {
                            mModifierKeys!!.add(key)
                        } else if (key.isEnter()) {
                            mEnterKey = key
                        } else if (key.isSpace()) {
                            spaceKey = key
                        }
                        currentRow.mKeys.add(key)
                    } else if (TAG_KEYBOARD == tag) {
                        parseKeyboardAttributes(res, parser)
                    }
                } else if (event == XmlResourceParser.END_TAG) {
                    if (inKey) {
                        inKey = false
                        x += key!!.gap + key.width
                        if (x > mTotalWidth) {
                            mTotalWidth = x
                        }
                    } else if (inRow) {
                        inRow = false
                        y += currentRow!!.verticalGap
                        y += currentRow.defaultHeight
                        row++
                    } else {

                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error:$e")
            e.printStackTrace()
        }
        mTotalHeight = y - mDefaultVerticalGap
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skipToEndOfRow(parser: XmlResourceParser) {
        var event: Int
        while (parser.next().also { event = it } != XmlResourceParser.END_DOCUMENT) {
            if (event == XmlResourceParser.END_TAG
                && parser.name == TAG_ROW
            ) {
                break
            }
        }
    }

    private fun parseKeyboardAttributes(res: Resources, parser: XmlResourceParser) {
        val a = res.obtainAttributes(
            Xml.asAttributeSet(parser),
            R.styleable.Keyboard
        )
        mDefaultWidth = getDimensionOrFraction(
            a,
            R.styleable.Keyboard_keyWidth,
            mDisplayWidth, mDisplayWidth / 10
        )
        mDefaultHeight = getDimensionOrFraction(
            a,
            R.styleable.Keyboard_keyHeight,
            mDisplayHeight, 50
        )
        mDefaultHorizontalGap = getDimensionOrFraction(
            a,
            R.styleable.Keyboard_horizontalGap,
            mDisplayWidth, 0
        )
        mDefaultVerticalGap = getDimensionOrFraction(
            a,
            R.styleable.Keyboard_verticalGap,
            mDisplayHeight, 0
        )
        mProximityThreshold = (mDefaultWidth * SEARCH_DISTANCE).toInt()
        mProximityThreshold *= mProximityThreshold // Square it for comparison
        a.recycle()
    }


    companion object {
        private val TAG = Keyboard::class.java.simpleName

        private val TAG_KEYBOARD = "Keyboard"
        private val TAG_ROW = "Row"
        private val TAG_KEY = "Key"

        private val EDGE_LEFT = 0x01
        private val EDGE_RIGHT = 0x02
        private val EDGE_TOP = 0x04
        private val EDGE_BOTTOM = 0x08

        private val KEYCODE_SHIFT = -1
        private val KEYCODE_MODECHANGE = -2
        private val KEYCODE_CANCEL = -3
        private val KEYCODE_DONE = -4
        private val KEYCODE_DELETE = -5
        private val KEYCODE_ALT = -20

        private val LOWER = 0
        private val UPPER = 1
        private val UPPER_LOCK = 2

        private val GRID_WIDTH = 10
        private val GRID_HEIGHT = 5
        private val GRID_SIZE = GRID_WIDTH * GRID_HEIGHT

        fun getDimensionOrFraction(a: TypedArray, index: Int, base: Int, defValue: Int): Int {
            val value = a.peekValue(index) ?: return defValue
            if (value.type == TypedValue.TYPE_DIMENSION) {
                return a.getDimensionPixelOffset(index, defValue)
            } else if (value.type == TypedValue.TYPE_FRACTION) {
                return Math.round(a.getFraction(index, base, base, defValue.toFloat()))
            }
            return defValue
        }
    }
}
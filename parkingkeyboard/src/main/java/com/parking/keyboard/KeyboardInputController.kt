package com.parking.keyboard

import android.util.Log
import android.view.View
import android.widget.Button
import com.parking.keyboard.engine.KeyboardEntry
import com.parking.keyboard.engine.NumberType
import com.parking.keyboard.view.InputView
import com.parking.keyboard.view.KeyboardView
import com.parking.keyboard.view.OnKeyboardChangedListener
import com.parking.vehiclekeyboard.R
import com.zqy.input.common.utils.ToastUtil

class KeyboardInputController(
    private val mKeyboardView: KeyboardView, private val mInputView: InputView
) {
    //    private val mOnInputChangedListeners: MutableList<OnInputChangedListener> = CopyOnWriteArrayList()
    private lateinit var mOnInputChangedListener: OnInputChangedListener
    private var mLockedOnNewEnergyType = false
    private var mDebugEnabled = true
    private var mSwitchVerify = true
    private var mMessageHandler: MessageHandler? = null

    /**
     * 使用键盘View和输入View，创建键盘输入控制器
     *
     * @param keyboardView 键盘View
     * @param inputView    输入框View
     */
    init {
        // 绑定输入框被选中的触发事件：更新键盘
        mInputView.addOnFieldViewSelectedListener { index ->
            val number = mInputView.number
            if (mDebugEnabled) {
                Log.w(TAG, "点击输入框更新键盘, 号码：$number，序号：$index")
            }
            // 除非锁定新能源类型，否则都让引擎自己检测车牌类型
            if (mLockedOnNewEnergyType) {
                mKeyboardView.update(number, index, false, NumberType.NEW_ENERGY)
            } else {
                mKeyboardView.update(number, index, false, NumberType.AUTO_DETECT)
            }
        }

        // 绑定键盘按键点击事件：更新输入框字符操作，输入框长度变化
        mKeyboardView.addKeyboardChangedListener(syncKeyboardInputState())
        // 检测键盘更新，尝试自动提交只有一位文本按键的操作
//        mKeyboardView.addKeyboardChangedListener(new AutoCommit(mInputView));
        // 触发键盘更新回调
        mKeyboardView.addKeyboardChangedListener(triggerInputChangedCallback())
    }

    /**
     * 绑定新能源车牌类型锁定按钮实现接口。
     * 当键盘切换新能源车牌时，会调用此接口相关函数来更改锁定按钮状态。
     *
     * @param proxy 锁定按钮代理实现接口
     * @return KeyboardInputController
     */
    fun bindLockTypeProxy(proxy: LockNewEnergyProxy): KeyboardInputController {
        // 点击按钮时，切换新能源车牌绑定状态
        proxy.setOnClickListener { tryLockNewEnergyType(!mLockedOnNewEnergyType) }
        // 新能源车牌绑定状态，同步键盘更新的新能源类型
        mKeyboardView.addKeyboardChangedListener(object : OnKeyboardChangedListener.Simple() {
            override fun onKeyboardChanged(keyboard: KeyboardEntry) {
                // 如果键盘更新当前为新能源类型时，强制锁定为新能源类型
                if (NumberType.NEW_ENERGY == keyboard.currentNumberType) {
                    tryLockNewEnergyType(true)
                }
                // 同步锁定按钮
                proxy.onNumberTypeChanged(NumberType.NEW_ENERGY == keyboard.currentNumberType)
            }
        })
        return this
    }

    /**
     * 使用默认Toast的消息显示处理接口。
     * 默认时，键盘状态切换的提示消息，通过Toast接口来显示。
     *
     * @return KeyboardBinder
     */
    fun useDefaultMessageHandler(): KeyboardInputController {
        return setMessageHandler(object : MessageHandler {
            override fun onMessageError(message: Int) {
//                Toast.makeText(mKeyboardView.getContext(), message, Toast.LENGTH_SHORT).show();
                ToastUtil.bottomShowToast(mKeyboardView.context, message)
            }

            override fun onMessageTip(message: Int) {
//                Toast.makeText(mKeyboardView.getContext(), message, Toast.LENGTH_SHORT).show();
                ToastUtil.bottomShowToast(mKeyboardView.context, message)
            }
        })
    }

    /**
     * 更新输入组件的车牌号码，并默认选中最后编辑位。
     *
     * @param number 车牌号码
     */
    fun updateNumber(number: String?) {
        updateNumberLockType(number, false)
    }

    /**
     * 更新输入组件的车牌号码，指定是否锁定新能源类型，并默认选中最后编辑位。
     *
     * @param number                车牌号码
     * @param lockedOnNewEnergyType 是否锁定为新能源类型
     */
    private fun updateNumberLockType(number: String?, lockedOnNewEnergyType: Boolean) {
        val newNumber = number ?: ""
        mLockedOnNewEnergyType = lockedOnNewEnergyType
        mInputView.updateNumber(newNumber)
        mInputView.performLastPendingFieldView()
    }
    ////
    /**
     * 设置键盘提示消息回调接口
     *
     * @param handler 消息回调接口
     * @return KeyboardBinder
     */
    private fun setMessageHandler(handler: MessageHandler): KeyboardInputController {
        mMessageHandler = Objects.notNull(handler)
        return this
    }

    /**
     * 添加输入变更回调接口
     *
     * @param listener 回调接口
     * @return KeyboardInputController
     */
    fun addOnInputChangedListener(listener: OnInputChangedListener): KeyboardInputController {
        mOnInputChangedListener = listener
        return this
    }

    /**
     * 移除输入变更回调接口
     *
     * @param listener 回调接口
     * @return KeyboardInputController
     */
//    fun removeOnInputChangedListener(listener: OnInputChangedListener): KeyboardInputController {
//        mOnInputChangedListeners.remove(Objects.notNull(listener))
//        return this
//    }

    /**
     * 设置是否在新能源和普通车牌切换的时候校验规则
     * @param verify 是否校验
     * @return KeyboardInputController
     */
    fun setSwitchVerify(verify: Boolean): KeyboardInputController {
        mSwitchVerify = verify
        return this
    }

    /**
     * 设置是否启用调试信息
     *
     * @param enabled 是否启用
     * @return KeyboardInputController
     */
    fun setDebugEnabled(enabled: Boolean): KeyboardInputController {
        mDebugEnabled = enabled
        return this
    }

    //////
    private fun updateInputViewItemsByNumberType(type: NumberType) {
        // 如果检测到的车牌号码为新能源、地方武警，需要显示第8位车牌
        val show: Boolean =
            NumberType.NEW_ENERGY == type || NumberType.WJ2012 == type || mLockedOnNewEnergyType
        mInputView.set8thVisibility(show)
    }

    private fun tryLockNewEnergyType(toLock: Boolean) {
        // not changed
        if (toLock == mLockedOnNewEnergyType) {
            return
        }
        val completed = mInputView.isCompleted
        if (toLock) {
            triggerLockEnergyType(completed)
        } else { // unlock
            triggerUnlockEnergy(completed)
        }
    }

    // 解锁新能源车牌
    private fun triggerUnlockEnergy(completed: Boolean) {
        mLockedOnNewEnergyType = false
        mMessageHandler!!.onMessageTip(R.string.pwk_now_is_normal)
        val lastItemSelected = mInputView.isLastFieldViewSelected
        updateInputViewItemsByNumberType(NumberType.AUTO_DETECT)
        if (completed || lastItemSelected) {
            mInputView.performLastPendingFieldView()
        } else {
            mInputView.rePerformCurrentFieldView()
        }
    }

    // 锁定新能源车牌
    private fun triggerLockEnergyType(completed: Boolean) {
        if (!mSwitchVerify || Texts.isNewEnergyType(mInputView.number)) {
            mLockedOnNewEnergyType = true
            mMessageHandler!!.onMessageTip(R.string.pwk_now_is_energy)
            updateInputViewItemsByNumberType(NumberType.NEW_ENERGY)
            if (completed) {
                mInputView.performNextFieldView()
            } else {
                mInputView.rePerformCurrentFieldView()
            }
        } else {
            mMessageHandler!!.onMessageError(R.string.pwk_change_to_energy_disallow)
        }
    }

    // 输入变更回调
    private fun triggerInputChangedCallback(): OnKeyboardChangedListener {
        return object : OnKeyboardChangedListener.Simple() {
            override fun onTextKey(text: String) {
                notifyChanged()
            }

            override fun onDeleteKey() {
                notifyChanged()
            }

            override fun onConfirmKey() {
                val number = mInputView.number
                mOnInputChangedListener.onCompleted(number, false)
            }

            private fun notifyChanged() {
                val completed = mInputView.isCompleted
                val number = mInputView.number
                try {
                    mOnInputChangedListener.onChanged(number, completed)
                } finally {
                    if (completed) {
                        mOnInputChangedListener.onCompleted(number, true)
                    }
                }
            }
        }
    }

    private fun syncKeyboardInputState(): OnKeyboardChangedListener {
        return object : OnKeyboardChangedListener.Simple() {
            override fun onTextKey(text: String) {
                mInputView.updateSelectedCharAndSelectNext(text)
            }

            override fun onDeleteKey() {
                mInputView.removeLastCharOfNumber()
            }

            override fun onKeyboardChanged(keyboard: KeyboardEntry) {
                if (mDebugEnabled) {
                    Log.w(
                        TAG,
                        "键盘已更新，" + "预设号码号码：" + keyboard.presetNumber + "，最终探测类型：" + keyboard.currentNumberType
                    )
                }
                updateInputViewItemsByNumberType(keyboard.currentNumberType)
            }
        }
    }

    /**
     * 锁定车牌类型代理接口
     */
    interface LockNewEnergyProxy {
        /**
         * 设置点击切换车牌类型的点击回调接口。通常使用Button来实现。
         *
         * @param listener 点击回调接口。
         */
        fun setOnClickListener(listener: View.OnClickListener?)

        /**
         * 当车牌类型发生变化时，此方法被回调。
         *
         * @param isNewEnergyType 当前是否为新能源类型
         */
        fun onNumberTypeChanged(isNewEnergyType: Boolean)
    }

    @Deprecated("")
    interface LockTypeProxy : LockNewEnergyProxy

    /**
     * 使用Button组件实现的锁定新能源车牌切换逻辑
     */
    open class ButtonProxyImpl(private val mButton: Button) : LockNewEnergyProxy {
        override fun setOnClickListener(listener: View.OnClickListener?) {
            mButton.setOnClickListener(listener)
        }

        override fun onNumberTypeChanged(isNewEnergyType: Boolean) {
            if (isNewEnergyType) {
                mButton.setText(R.string.pwk_change_to_normal)
            } else {
                mButton.setText(R.string.pwk_change_to_energy)
            }
        }
    }

    @Deprecated("")
    class ButtonProxy(button: Button) : ButtonProxyImpl(button)
    companion object {
        private const val TAG = "KeyboardInputController"

        /**
         * 使用键盘View和输入View，创建键盘输入控制器
         *
         * @param keyboardView 键盘View
         * @param inputView    输入框View
         * @return KeyboardInputController
         */
        @JvmStatic
        fun with(keyboardView: KeyboardView, inputView: InputView): KeyboardInputController {
            return KeyboardInputController(keyboardView, inputView)
        }
    }
}
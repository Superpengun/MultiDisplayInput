package com.zqy.hci.ui

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import com.parking.keyboard.KeyboardInputController.ButtonProxyImpl
import com.parking.keyboard.OnInputChangedListener
import com.parking.keyboard.PopupKeyboard
import com.parking.keyboard.view.InputView
import com.zqy.hci.R
import com.zqy.hci.listener.OnNewPlateActionListener
import com.zqy.input.common.utils.SharedPreferenceUtils

class NewPlatePop : PopupWindow() {
    val LAST_PLATE = "last_plate";
    private lateinit var mContext: Context
    private lateinit var mPlateView: InputView
    private lateinit var mPopupKeyboard: PopupKeyboard
    private lateinit var mVehicleTypeButton: Button
    private lateinit var mActionButton: Button
    private lateinit var mLastPlateButton: Button
    private lateinit var mListener: OnNewPlateActionListener
    private var currentNumber = ""
    private var lastCommitNumber = ""
    private lateinit var mParentView: View
    private lateinit var mParentDialog: Dialog

    fun initView(view: View, dialog: Dialog) {
        mParentView = view
        mContext = mParentView.context
        mParentDialog = dialog
        val resId: Int = R.layout.layout_newplate
        contentView = LayoutInflater.from(mContext).inflate(resId, null, true)
        mPlateView = contentView.findViewById(R.id.input_view)
        mVehicleTypeButton = contentView.findViewById(R.id.bt_vehicle_type)
        mActionButton = contentView.findViewById(R.id.bt_action)
        mLastPlateButton = contentView.findViewById(R.id.bt_vehicle_last)
        mPopupKeyboard = PopupKeyboard(mContext)
        mActionButton.setOnClickListener {
            mListener.onPlateCommit(currentNumber)
            lastCommitNumber = currentNumber
            SharedPreferenceUtils.getInstance(mContext).put(LAST_PLATE, currentNumber)
        }
        mLastPlateButton.setOnClickListener {
            mListener.onPlateCommit(lastCommitNumber)
        }
        lastCommitNumber = SharedPreferenceUtils.getInstance(mContext).getString(LAST_PLATE, "")
    }

    fun setOnNewPlateActionListener(listener: OnNewPlateActionListener) {
        mListener = listener
    }

    fun show() {
        width = 800
        height = 150
        val xOffset = 0
        val yOffset = contentView.height
        showAsDropDown(mParentView, 0, -mParentView.height - height, Gravity.TOP or Gravity.START)
        mLastPlateButton.text = "上次车牌:$lastCommitNumber"
        mPopupKeyboard.attach(mPlateView, mParentDialog)
        mPopupKeyboard.controller.updateNumber("")
        keyboardInit()
        mPlateView.performFirstFieldView()
    }

    fun dismiss(dialog: Dialog) {
        mPopupKeyboard.dismiss(dialog.window)
        dismiss()
    }

    private fun keyboardInit() {
        mPopupKeyboard.controller
            .setDebugEnabled(true)
            .setSwitchVerify(true)
            .bindLockTypeProxy(object : ButtonProxyImpl(mVehicleTypeButton) {
                override fun onNumberTypeChanged(isNewEnergyType: Boolean) {
                    super.onNumberTypeChanged(isNewEnergyType)
                    if (isNewEnergyType) {
                        mVehicleTypeButton.setTextColor(mContext.getColor(android.R.color.holo_green_light))
                    } else {
                        mVehicleTypeButton.setTextColor(mContext.getColor(R.color.key_text_color))
                    }
                }
            })
            .addOnInputChangedListener(object : OnInputChangedListener {
                override fun onChanged(number: String?, isCompleted: Boolean) {
                    if (number != null) {
                        currentNumber = number
                    }
                }

                override fun onCompleted(number: String?, isAutoCompleted: Boolean) {
                    when (isAutoCompleted) {
                        false -> {
                            if (number != null) {
                                Log.d("NewPlatePop", "onCompleted: $number")
                                mListener.onPlateCommit(number)
                                lastCommitNumber = number
                                SharedPreferenceUtils.getInstance(mContext).put(LAST_PLATE, number)
                            }
                        }

                        else -> {}
                    }
                }

            })
    }
}
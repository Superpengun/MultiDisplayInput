package com.zqy.hci.widget

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.IBinder
import android.view.Window
import android.view.WindowManager
import com.zqy.hci.R

/**
 * @Author:CWQ
 * @DATE:2022/1/20
 * @DESC:
 */
object DialogHelper {

    /**
     * 获取输入方式选择框
     * @param context Context
     * @param token IBinder
     * @param listener OnClickListener
     * @return Dialog
     */
    fun getInputSelectDialog(
        context: Context,
        token: IBinder,
        checkedItem: Int = 0,
        listener: DialogInterface.OnClickListener
    ): AlertDialog {
        val builder = AlertDialog.Builder(context).apply {
            setTitle(context.getString(R.string.switch_kb))
            setSingleChoiceItems(R.array.inputItems, checkedItem, listener)
            setCancelable(true)
            setPositiveButton(context.getString(R.string.cancel), null)
        }
        val dialog = builder.create()
        val lv = dialog.listView
        lv.isScrollbarFadingEnabled = false
        val window: Window = dialog.window!!
        val params = window.attributes
        params.token = token
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
        params.dimAmount = 0.5f
        window.attributes = params
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
}
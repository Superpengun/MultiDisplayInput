package com.zqy.hci.keyboard

import android.R

class KeyDrawableStateProvider(
    keyTypeFunctionAttrId: Int /*R.attr.key_type_function*/,
    keyActionAttrId: Int /*R.attr.key_type_action*/,
    keyActionTypeDoneAttrId: Int /*R.attr.action_done*/,
    keyActionTypeSearchAttrId: Int /*R.attr.action_search*/,
    keyActionTypeGoAttrId: Int /*R.attr.action_go*/
) {
    val KEY_STATE_NORMAL = intArrayOf()
    val KEY_STATE_PRESSED = intArrayOf(R.attr.state_pressed)
    val KEY_STATE_FUNCTIONAL_NORMAL: IntArray
    val KEY_STATE_FUNCTIONAL_PRESSED: IntArray
    val DRAWABLE_STATE_MODIFIER_NORMAL = intArrayOf()
    val DRAWABLE_STATE_MODIFIER_PRESSED = intArrayOf(R.attr.state_pressed)
    val DRAWABLE_STATE_MODIFIER_LOCKED = intArrayOf(R.attr.state_checked)
    val DRAWABLE_STATE_ACTION_NORMAL = intArrayOf()
    val DRAWABLE_STATE_ACTION_DONE: IntArray
    val DRAWABLE_STATE_ACTION_SEARCH: IntArray
    val DRAWABLE_STATE_ACTION_GO: IntArray
    val KEY_STATE_ACTION_NORMAL: IntArray
    val KEY_STATE_ACTION_PRESSED: IntArray

    init {
        KEY_STATE_FUNCTIONAL_NORMAL = intArrayOf(keyTypeFunctionAttrId)
        KEY_STATE_FUNCTIONAL_PRESSED = intArrayOf(keyTypeFunctionAttrId, R.attr.state_pressed)
        DRAWABLE_STATE_ACTION_DONE = intArrayOf(keyActionTypeDoneAttrId)
        DRAWABLE_STATE_ACTION_SEARCH = intArrayOf(keyActionTypeSearchAttrId)
        DRAWABLE_STATE_ACTION_GO = intArrayOf(keyActionTypeGoAttrId)
        KEY_STATE_ACTION_NORMAL = intArrayOf(keyActionAttrId)
        KEY_STATE_ACTION_PRESSED = intArrayOf(keyActionAttrId, R.attr.state_pressed)
    }
}
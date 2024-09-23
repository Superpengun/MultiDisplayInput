package com.zqy.hci.handwrite

/**
 * @author:zhenqiyuan
 * @data:2022/9/21
 * @描述：
 * @package:com.zqy.hci.handwrite
 */
class StrokeData() {
    var point_length = 0
    private var mPoints: ShortArray? = null
    private var mCurIndex: Int
    private var last_point_x = 0
    private var last_point_y = 0

    /**
     * 像点集中添加新的点
     * @param x 横坐标
     * @param y 纵坐标
     * @return 是否添加成功
     */
    @Synchronized
    fun addStroke(x: Short, y: Short): Boolean {
        if (x < 0 || y < 0) return false
        if (x.toInt() == last_point_x && y.toInt() == last_point_y) return false
        if (mCurIndex / 2 < MAX_POINT - 2) {
            mPoints!![mCurIndex] = x
            last_point_x = x.toInt()
            mCurIndex++
            mPoints!![mCurIndex] = y
            last_point_y = y.toInt()
            mCurIndex++
            return true
        }
        return false
    }

    /**
     * 添加整次识别结束点-1，-1
     */
    @Synchronized
    fun addEndStroke() {
        mPoints!![mCurIndex] = -1
        mCurIndex++
        mPoints!![mCurIndex] = -1
        mCurIndex++
    }

    /**
     * 添加抬笔标记点 -1,0
     */
    @Synchronized
    fun addTouchUpStroke() {
        mPoints!![mCurIndex] = -1
        mCurIndex++
        mPoints!![mCurIndex] = 0
        mCurIndex++
    }

    /**
     * 获取当前已添加的的笔迹数组
     * @return 笔迹点数组
     */
    @Synchronized
    fun getStroke(): ShortArray {
        val pts = ShortArray(mCurIndex)
        System.arraycopy(mPoints, 0, pts, 0, pts.size)
        return pts
    }

    /**
     * 获取共添加了多少个点
     * @return 点个数
     */
    @Synchronized
    fun getStrokeCount(): Int {
        return mCurIndex / 2
    }

    /**
     * 重设当前点指针
     */
    @Synchronized
    fun resetStroke() {
        mCurIndex = 0
    }

    /**
     * 获取笔迹的较长一边
     * @return 长边
     */
    @Synchronized
    fun getStrokeLength(): Int {
        val first_point_x = mPoints!![0].toInt()
        val first_point_y = mPoints!![1].toInt()
        val x = Math.abs(last_point_x - first_point_x)
        val y = Math.abs(last_point_y - first_point_y)
        return if (x > y) {
            x
        } else {
            y
        }
    }

    companion object {
        /**
         * 最多可识别的点数
         */
        private const val MAX_POINT = 20480
        private const val TAG = "StrokeData"
    }

    init {
        mPoints = ShortArray(MAX_POINT * 2)
        mCurIndex = 0
    }
}
package com.zqy.input.common.utils

import android.app.Application
import android.content.Context
import java.lang.ref.WeakReference


/**
 *Created by liqiang on 2020/4/16.
 **/
class ContextProvider(ctx: Context) {
    private var context:WeakReference<Context> = WeakReference(ctx)
    companion object{
        val instance: ContextProvider by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){
            val context = ApplicationContextProvider.ctx ?: throw IllegalStateException("context == null")
            ContextProvider(context)
        }
        /**
         * 获取实例
         */
//        fun get(): ContextProvider?{
//            if (instance == null) {
//                synchronized(ContextProvider::class.java) {
//                    if (instance == null) {
//                        val context = ApplicationContextProvider.ctx ?: throw IllegalStateException("context == null")
//                        instance = ContextProvider(context)
//                    }
//                }
//            }
//            return instance
//        }
    }

    /**
     * 获取上下文
     */
    fun getContext(): Context {
        return context.get()!!
    }

    fun getApplication(): Application {
        return WeakReference<Application>(context.get()!!.applicationContext as Application).get()!!
    }

}
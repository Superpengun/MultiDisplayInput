package com.sinovoice.ai

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch


class AIEventThread() : Thread("AIEventThread") {

    private var listener: AIEventListener? = null

    private var handler: Handler? = null
    private val handlerInitLatch: CountDownLatch = CountDownLatch(1)

    /**
     *
     */
    override fun run() {
        Looper.prepare()
        handler = Handler(Looper.myLooper()!!)
        handlerInitLatch.countDown()
        Looper.loop()
    }

    fun setListener(listener: AIEventListener?) {
        this.listener = listener
    }

    fun getHandler(): Handler? {
        try {
            handlerInitLatch.await()
        } catch (ie: InterruptedException) {
            // continue?
        }
        return handler
    }

    fun postTask(runnable: Runnable, delay: Long) {
        handler?.postDelayed(runnable, delay)
    }

    fun notifyAIEvent(event: AIEvent) {
        getHandler()?.post {
            listener?.onEvent(event)
        }
    }
}

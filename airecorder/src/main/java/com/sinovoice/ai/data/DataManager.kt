package com.sinovoice.ai.data

import java.util.concurrent.LinkedBlockingQueue

internal class DataManager {

    companion object {
        private var instance: DataManager? = null

        @Synchronized
        fun get(): DataManager {
            if (instance == null) {
                instance =
                    DataManager()
            }
            return instance!!
        }
    }


    lateinit var dataForRecognizeQueue: LinkedBlockingQueue<RecordData>

    fun initData() {
        dataForRecognizeQueue = LinkedBlockingQueue(100)
    }

    fun clearData() {
        dataForRecognizeQueue.clear()
    }

    fun getRecognizeData(): RecordData {
        return dataForRecognizeQueue.take()
    }

    fun putRecognizeData(data: RecordData): Boolean {
        return dataForRecognizeQueue.offer(data)
    }

    fun pollRecognizeData(): RecordData? {
        return dataForRecognizeQueue.poll()
    }
}
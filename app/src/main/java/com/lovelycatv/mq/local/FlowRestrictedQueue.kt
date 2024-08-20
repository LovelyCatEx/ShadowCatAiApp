package com.lovelycatv.mq.local

import com.lovelycatv.ai.shadowcat.app.util.common.runAsync
import com.lovelycatv.ai.shadowcat.app.util.common.runIfNotNull
import kotlinx.coroutines.delay
import java.util.concurrent.LinkedBlockingQueue

typealias FlowRestrictedQueueObserver<T> = (t: T) -> Unit

class FlowRestrictedQueue<T>(
    private val minIntervalMills: Long,
    private val oneObserverOnly: Boolean = false
) : LinkedBlockingQueue<T>() {
    private val observers: MutableList<FlowRestrictedQueueObserver<T>> = mutableListOf()

    private var lastTransferTime: Long = 0L

    override fun put(e: T) {
        val interval = System.currentTimeMillis() - lastTransferTime
        if (interval >= minIntervalMills) {
            sendAll(e)
        } else {
            super.put(e)
            runAsync {
                val remainingTime = minIntervalMills - interval + (minIntervalMills * super.size)
                delay(remainingTime)
                poll().runIfNotNull {
                    sendAll(it)
                }
            }
        }
    }

    /**
     * Observe this queue
     *
     * @param fx Actions
     * @param override Only works when parameter oneObserverOnly is true. If override is also true, then the new observer will override old one.
     */
    fun observe(override: Boolean = true, fx: FlowRestrictedQueueObserver<T>) {
        if (this.observers.size == 0 || !oneObserverOnly) {
            this.observers.add(fx)
        } else if(override) {
            this.observers[0] = fx
        }
    }

    private fun sendAll(t: T) {
        this.lastTransferTime = System.currentTimeMillis()
        this.observers.forEach {
            it(t)
        }
    }
}
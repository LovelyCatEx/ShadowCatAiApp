package com.lovelycatv.ai.shadowcat.app.util.common

import kotlinx.coroutines.delay

/**
 * Some tasks such as IO may finished fast, so this function will make things gracefully
 */

class ForceDelay<T>(
    private val delayTimeMills: Long,
    private val onFinish: (result: T?, timeCost: Long) -> Unit
) {
    private var startTime: Long = 0
    private var finished: Boolean = false

    fun start(startTime: Long = System.currentTimeMillis()) {
        this.startTime = startTime
    }

    suspend fun start(startTime: Long = System.currentTimeMillis(), fx: suspend (ForceDelay<T>) -> T?) {
        this.startTime = startTime

        val result = fx(this)

        // If user did not call $finish(), call it when fx() finished
        if (!finished) {
            finish(result)
        }
    }

    suspend fun finish(result: T?) {
        this.finished = true
        val now = System.currentTimeMillis()
        val timeCost = now - startTime
        if (timeCost >= delayTimeMills) {
            onFinish(result, timeCost)
        } else {
            val remainingTime = delayTimeMills - timeCost
            delay(remainingTime)
            onFinish(result, timeCost)
        }
    }

}
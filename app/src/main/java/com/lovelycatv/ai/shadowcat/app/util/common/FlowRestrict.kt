package com.lovelycatv.ai.shadowcat.app.util.common

import kotlinx.coroutines.delay

/**
 * FlowRestrict designed for some quick actions that could not be prevent conveniently
 *
 * @property delayTimeMills ms
 * @property delayAction If false, action too quick will be ignored otherwise the performance of the action will be delayed
 * @property fx Actions you want to perform
 */
class FlowRestrict(
    private val delayTimeMills: Long,
    private val delayAction: Boolean = true,
    private val fx: (args: Array<out Any?>) -> Unit
) {
    private var lastPerformTime: Long = 0

    suspend fun perform(vararg args: Any?) {
        with(System.currentTimeMillis() - lastPerformTime) {
            if (this >= delayTimeMills) {
                fx(args)
            } else if (delayAction) {
                val remainingTime = delayTimeMills - this
                delay(remainingTime)
                fx(args)
            }

        }

        this.lastPerformTime = System.currentTimeMillis()
    }
}
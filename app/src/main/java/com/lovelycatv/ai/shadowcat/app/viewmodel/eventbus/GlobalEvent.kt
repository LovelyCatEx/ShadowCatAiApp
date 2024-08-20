package com.lovelycatv.ai.shadowcat.app.viewmodel.eventbus

data class GlobalEvent(
    val event: GlobalEventEnum,
    val payload: Any?
) {
    constructor(event: GlobalEventEnum) : this(event, null)

    companion object {
        @JvmStatic
        fun idle() = GlobalEvent(GlobalEventEnum.IDLE)
    }
}
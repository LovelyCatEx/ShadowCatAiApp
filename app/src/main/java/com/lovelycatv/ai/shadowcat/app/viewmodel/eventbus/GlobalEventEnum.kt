package com.lovelycatv.ai.shadowcat.app.viewmodel.eventbus

enum class GlobalEventEnum {
    IDLE,
    MAIN_SESSION_LIST_REFRESH_LOCAL,
    MAIN_SESSION_LIST_REFRESH_NETWORK;
}

fun GlobalEventEnum.post(payload: Any? = null) = GlobalEventBus.instance.postEvent(GlobalEvent(this, payload))
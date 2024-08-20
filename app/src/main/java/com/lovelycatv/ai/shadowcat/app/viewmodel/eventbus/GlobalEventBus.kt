package com.lovelycatv.ai.shadowcat.app.viewmodel.eventbus

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GlobalEventBus : ViewModel() {
    companion object {
        @JvmStatic
        val instance = GlobalEventBus()
    }

    private val _currentEvent = MutableLiveData(GlobalEvent.idle())
    val currentEvent: LiveData<GlobalEvent> get() = _currentEvent

    fun postEvent(event: GlobalEvent) {
        Log.d("GlobalEventBus", event.event.name)
        this._currentEvent.postValue(event)
    }

    fun flush() {
        this._currentEvent.postValue(GlobalEvent(GlobalEventEnum.IDLE))
    }
}

fun flushGlobalEvent() = GlobalEventBus.instance.flush()
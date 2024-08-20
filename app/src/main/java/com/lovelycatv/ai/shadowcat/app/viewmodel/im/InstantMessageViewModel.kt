package com.lovelycatv.ai.shadowcat.app.viewmodel.im

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lovelycatv.ai.shadowcat.app.im.message.func.CallBackMessage
import com.lovelycatv.ai.shadowcat.app.im.vm.StreamingDataPack
import com.lovelycatv.mq.local.FlowRestrictedQueue

class InstantMessageViewModel : ViewModel() {
    companion object {
        @JvmStatic
        var instance = InstantMessageViewModel()
    }

    // Default value is true, but real value will be posted later
    private val _imClientConnected = MutableLiveData(true)
    val imClientConnected: LiveData<Boolean> get() = _imClientConnected

    fun setImClientConnectionStatus(connected: Boolean) {
        this._imClientConnected.postValue(connected)
        this.canSendMessage.postValue(connected)
    }

    val canSendMessage = MutableLiveData(true)

    private val _imCallbackMessage = MutableLiveData(CallBackMessage.idle())
    val imCallbackMessage: LiveData<CallBackMessage> get() = _imCallbackMessage

    fun onReceivedCallbackMessageFromChatServer(message: CallBackMessage) {
        _imCallbackMessage.postValue(message)
    }

    val _imStreamingDataPack = FlowRestrictedQueue<StreamingDataPack>(100, true)

    fun onReceivedStreamingData(
        isNewStream: Boolean,
        streamId: String,
        sessionId: String,
        messageId: Long,
        data: String,
        completed: Boolean
    ) {
        this._imStreamingDataPack.put(StreamingDataPack(isNewStream, streamId, sessionId, messageId, data, completed))
    }
}
package com.lovelycatv.ai.shadowcat.app.im.handler

import android.util.Log
import com.alibaba.fastjson2.JSON
import com.lovelycatv.ai.shadowcat.app.exception.im.MessageTypeNotSupportException
import com.lovelycatv.ai.shadowcat.app.exception.im.StreamingDataReceiveException
import com.lovelycatv.ai.shadowcat.app.im.message.MessageChain
import com.lovelycatv.ai.shadowcat.app.im.message.TextMessage
import com.lovelycatv.ai.shadowcat.app.im.message.func.CallBackMessage
import com.lovelycatv.ai.shadowcat.app.util.common.runAsync
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

typealias StreamingCallback = suspend (sessionId: String, streamId: String, data: String, isNewStream: Boolean) -> Unit
typealias StreamCompleteCallback = suspend (sessionId: String, streamId: String, messageId: Long, fullData: String) -> Unit
typealias ChatServerCallback = suspend (data: CallBackMessage) -> Unit

class MessageChainHandler : SimpleChannelInboundHandler<MessageChain>() {
    private var onStreamingDataReceived: StreamingCallback? = null
    private var onStreamingDataCompleted: StreamCompleteCallback? = null
    private var onChatServerCallback: ChatServerCallback? = null

    fun setOnStreamingDataReceived(listener: StreamingCallback) {
        this.onStreamingDataReceived = listener
    }

    fun setOnStreamingDataCompleted(listener: StreamCompleteCallback) {
        this.onStreamingDataCompleted = listener
    }

    fun setOnChatServerCallback(listener: ChatServerCallback) {
        this.onChatServerCallback = listener
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: MessageChain) {
        if (msg.messages.size == 0) {
            return
        }
        runAsync {
            if (msg.isFunc) {
                functionalMessageProcessor(ctx, msg)
            } else {
                streamingMessageProcessor(ctx, msg)
            }
        }
    }

    // StreamId, Data
    private val streamDataMap = mutableMapOf<String, String>()
    private val streamWithSessionId = mutableMapOf<String, String>()

    private suspend fun functionalMessageProcessor(ctx: ChannelHandlerContext, msg: MessageChain) {
        Log.d("Netty", "Message From Remote ==> " + JSON.toJSONString(msg))
        val message = msg.messages[0]
        if (message is CallBackMessage) {
            when (message.code) {
                CallBackMessage.CODE_SESSION_STREAM_MESSAGE_COMPLETED -> {
                    val streamId= message.message.split(":")[0]
                    val messageId = message.message.split(":")[1]
                    if (streamDataMap.containsKey(streamId)) {
                        if (this.onStreamingDataCompleted != null) {
                            this.onStreamingDataCompleted!!(streamWithSessionId[streamId]!!, streamId, messageId.toLong(), streamDataMap[streamId]!!)
                        }
                        // Remove data to prevent memory leak
                        streamDataMap.remove(streamId)
                        streamWithSessionId.remove(streamId)
                    } else {
                        throw StreamingDataReceiveException()
                    }
                }
                else -> {
                    if (this.onChatServerCallback != null) {
                        this.onChatServerCallback!!(message)
                    }
                }
            }
        } else {
            throw MessageTypeNotSupportException("Message type ${message.messageType.name} not support yet")
        }
    }

    private suspend fun streamingMessageProcessor(ctx: ChannelHandlerContext, msg: MessageChain) {
        val streamId = msg.streamId
        var output = ""
        for (message in msg.messages) {
            if (message is TextMessage) {
                output += message.message
            } else {
                throw MessageTypeNotSupportException("Message type ${message.messageType.name} not support yet")
            }
        }

        val isNewStream = !streamDataMap.containsKey(streamId)
        if (isNewStream) {
            streamDataMap[streamId] = output
        } else {
            streamDataMap[streamId] = streamDataMap[streamId] + output;
        }

        if (!streamWithSessionId.containsKey(streamId)) {
            streamWithSessionId[streamId] = msg.sessionId
        }

        if (onStreamingDataReceived != null) {
            onStreamingDataReceived!!(msg.sessionId, streamId, output, isNewStream)
        }
    }
}
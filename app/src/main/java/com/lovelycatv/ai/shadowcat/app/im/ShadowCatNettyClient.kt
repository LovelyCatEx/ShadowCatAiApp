package com.lovelycatv.ai.shadowcat.app.im

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.lovelycatv.ai.shadowcat.app.config.connection.ConnectionSettings
import com.lovelycatv.ai.shadowcat.app.exception.im.ClientDisconnectedException
import com.lovelycatv.ai.shadowcat.app.exception.im.MessageNotSendException
import com.lovelycatv.ai.shadowcat.app.im.codec.CustomFrameDecoder
import com.lovelycatv.ai.shadowcat.app.im.codec.MyObjectDecoder
import com.lovelycatv.ai.shadowcat.app.im.codec.MyObjectEncoder
import com.lovelycatv.ai.shadowcat.app.im.handler.ChatServerCallback
import com.lovelycatv.ai.shadowcat.app.im.handler.MessageChainHandler
import com.lovelycatv.ai.shadowcat.app.im.handler.StreamCompleteCallback
import com.lovelycatv.ai.shadowcat.app.im.handler.StreamingCallback
import com.lovelycatv.ai.shadowcat.app.im.message.MessageChain
import com.lovelycatv.ai.shadowcat.app.im.message.TextMessage
import com.lovelycatv.ai.shadowcat.app.im.message.func.LoginMessage
import com.lovelycatv.ai.shadowcat.app.util.GlobalConstants
import com.lovelycatv.ai.shadowcat.app.util.common.runAsync
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.InetSocketAddress


class ShadowCatNettyClient(
    val serverConnectionSettings: ConnectionSettings,
    val retryInterval: Int,
    val onChatServerCallback: ChatServerCallback,
    val onStreamDataReceived: StreamingCallback,
    val onStreamDataCompleted: StreamCompleteCallback
) {
    companion object {
        @JvmStatic
        var instance: ShadowCatNettyClient? = null
    }
    private var bootstrap: Bootstrap = Bootstrap()
        .group(NioEventLoopGroup())
        .channel(NioSocketChannel::class.java)
        .handler(object : ChannelInitializer<NioSocketChannel>() {
            override fun initChannel(ch: NioSocketChannel) {
                ch.pipeline().addLast(CustomFrameDecoder())
                ch.pipeline().addLast(MyObjectEncoder())
                ch.pipeline().addLast(MyObjectDecoder())

                ch.pipeline().addLast(MessageChainHandler().apply {
                    this.setOnStreamingDataReceived(onStreamDataReceived)
                    this.setOnStreamingDataCompleted(onStreamDataCompleted)
                    this.setOnChatServerCallback(onChatServerCallback)
                })
            }

        })

    val connected = MutableLiveData(false)
    private var channel: Channel? = null
    private var retriedTimes = 0

    fun isConnected() = this.connected.value!! && this.channel != null

    suspend fun connect() {
        if (isConnected()) {
            return
        }
        try {
            val serverAddress = InetSocketAddress(
                serverConnectionSettings.currentConnectedServer.address.replace("http://", "").replace("https://", ""),
                serverConnectionSettings.currentConnectedServer.chatPort
            )

            Log.d(GlobalConstants.LOG_TAG_NETTY, "Ready connect to $serverAddress")

            withContext(Dispatchers.IO) {
                channel = bootstrap.connect(serverAddress).sync().channel()
                if (channel != null) {
                    connected.postValue(true)

                    // Login to server after connected
                    channel!!.writeAndFlush(MessageChain().apply {
                        this.isFunc = true
                        this.messages.add(LoginMessage(
                            serverConnectionSettings.currentConnectedAccount.username,
                            serverConnectionSettings.currentConnectedAccount.password
                        ))
                    })

                    channel!!.closeFuture().addListener {
                        connected.postValue(false)
                        doWhenDisconnected()
                    }

                    // Reset retry times count
                    retriedTimes = 0
                } else {
                    connected.postValue(false)
                }
            }
        } catch (e: Exception) {
            connected.postValue(false)
            Log.e(GlobalConstants.LOG_TAG_NETTY, "Connection Error")
            doWhenDisconnected()
            if (e !is ConnectException) {
                e.printStackTrace()
            }
        }
    }

    fun getChannel() = this.channel

    fun sendMessage(sessionId: String, message: String) {
        if (!isConnected()) {
            throw ClientDisconnectedException()
        }

        try {
            this.channel!!.writeAndFlush(MessageChain().apply {
                this.sessionId = sessionId
                this.messages.add(TextMessage(message))
            })
        } catch (e: Exception) {
            e.printStackTrace()
            throw MessageNotSendException(e.message ?: "Message for session: [$sessionId] could not be sent")
        }

    }

    private fun doWhenDisconnected() {
        runAsync {
            delay(retryInterval * 1000L)
            retriedTimes += 1
            Log.w(GlobalConstants.LOG_TAG_NETTY, "Disconnected from server, retried times: $retriedTimes")
            connect()
        }
    }
}
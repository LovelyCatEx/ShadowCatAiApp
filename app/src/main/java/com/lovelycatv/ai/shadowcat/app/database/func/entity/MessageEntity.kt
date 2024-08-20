package com.lovelycatv.ai.shadowcat.app.database.func.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.alibaba.fastjson2.annotation.JSONField

@Entity("messages")
data class MessageEntity(
    @JSONField(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    @JSONField(name = "messageId")
    var messageId: Long,
    @JSONField(name = "sessionId")
    var sessionId: String,
    @JSONField(name = "assistant")
    var assistant: Boolean = false,
    @JSONField(name = "messageType")
    var messageType: Int,
    @JSONField(name = "message")
    var message: String,
    @JSONField(name = "datetime")
    var datetime: Long
) {
    @JSONField(name = "uid")
    @Ignore
    var uid: Long = 0

    constructor() : this(0, 0, "", false, 0, "", 0)

    companion object {
        @JvmStatic
        fun messageOnly(assistant: Boolean, message: String): MessageEntity {
            return MessageEntity(0,0,"",assistant,0, message,0)
        }

        @JvmStatic
        fun forAssistant(messageId: Long, sessionId: String, message: String): MessageEntity {
            return MessageEntity(
                0,
                messageId,
                sessionId,
                true,
                0,
                message,
                System.currentTimeMillis()
            )
        }

        @JvmStatic
        fun forUser(messageId: Long, sessionId: String, message: String): MessageEntity {
            return MessageEntity(
                0,
                messageId,
                sessionId,
                false,
                0,
                message,
                System.currentTimeMillis()
            )
        }
    }
}
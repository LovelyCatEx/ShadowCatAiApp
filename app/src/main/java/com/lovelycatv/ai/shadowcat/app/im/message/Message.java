package com.lovelycatv.ai.shadowcat.app.im.message;

import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;

/**
 * @author lovelycat
 * @version 1.0
 * @since 2024-08-03 22:51
 */
public abstract class Message implements Serializable {
    @JSONField(name = "type")
    private MessageType messageType;

    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}

package com.lovelycatv.ai.shadowcat.app.im.message.func;

import com.lovelycatv.ai.shadowcat.app.im.message.Message;
import com.lovelycatv.ai.shadowcat.app.im.message.MessageChain;
import com.lovelycatv.ai.shadowcat.app.im.message.MessageType;

/**
 * @author lovelycat
 * @version 1.0
 * @since 2024-08-05 14:54
 */
public class CallBackMessage extends Message {
    public static final int CODE_IDLE = 0;
    public static final int CODE_LOGIN_SUCCESS = 100;
    public static final int CODE_LOGIN_FAILED = 101;

    public static final int CODE_SESSION_STREAM_MESSAGE_COMPLETED = 200;
    public static final int CODE_SESSION_INVALID = 201;
    public static final int CODE_SESSION_MESSAGE_RECEIVED = 202;
    public static final int CODE_SESSION_BUSY = 203;

    private int code;
    private String message;

    public CallBackMessage(int code, String message) {
        super(MessageType.FUNC_CALLBACK);
        this.code = code;
        this.message = message;
    }

    public static CallBackMessage idle() {
        return new CallBackMessage(CODE_IDLE, null);
    }

    public static MessageChain build(int code, String message) {
        MessageChain messageChain = new MessageChain(new CallBackMessage(code, message));
        messageChain.setFunc(true);
        return messageChain;
    }

    public static MessageChain loginFailed() {
        MessageChain messageChain = new MessageChain(new CallBackMessage(CODE_LOGIN_FAILED, "Username or password incorrect"));
        messageChain.setFunc(true);
        return messageChain;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}

package com.lovelycatv.ai.shadowcat.app.im.message;

/**
 * @author lovelycat
 * @version 1.0
 * @since 2024-08-03 22:52
 */
public class TextMessage extends Message {
    private String message = "";

    public TextMessage(String message) {
        super(MessageType.TEXT);
        this.message = message;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

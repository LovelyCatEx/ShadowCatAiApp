package com.lovelycatv.ai.shadowcat.app.im.message.func;


import com.lovelycatv.ai.shadowcat.app.im.message.Message;
import com.lovelycatv.ai.shadowcat.app.im.message.MessageType;

import java.io.Serial;

/**
 * @author lovelycat
 * @version 1.0
 * @since 2024-08-04 00:57
 */
public class LoginMessage extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;

    public LoginMessage(String username, String password) {
        super(MessageType.FUNC_LOGIN);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

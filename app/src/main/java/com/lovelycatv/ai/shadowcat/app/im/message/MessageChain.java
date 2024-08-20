package com.lovelycatv.ai.shadowcat.app.im.message;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.lovelycatv.ai.shadowcat.app.im.message.func.CallBackMessage;
import com.lovelycatv.ai.shadowcat.app.im.message.func.LoginMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lovelycat
 * @version 1.0
 * @since 2024-08-03 22:52
 */
public class MessageChain implements Serializable {
    private List<Message> messages = new ArrayList<>();
    private String sessionId;
    private String streamId;
    private boolean func;

    public MessageChain(List<Message> messageList) {
        messages.addAll(messageList);
    }

    public MessageChain(Message... messageArray) {
        messages.addAll(List.of(messageArray));
    }

    public MessageChain(Message message) {
        messages.add(message);
    }

    public MessageChain() {

    }

    public static MessageChain decodeJSON(String json) {
        JSONObject msgObject = JSONObject.parse(json);
        MessageChain msg = new MessageChain();
        msg.setFunc(msgObject.getBoolean("func"));
        msg.setSessionId(msgObject.getString("sessionId"));
        msg.setStreamId(msgObject.getString("streamId"));
        JSONArray messages = msgObject.getJSONArray("messages");
        int size = messages.size();
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = messages.getJSONObject(i);
            Class<? extends Message> jsonClass = Message.class;
            switch (jsonObject.getString("type")) {
                case "TEXT":
                    jsonClass = TextMessage.class;
                    break;
                case "FUNC_CALLBACK":
                    jsonClass = CallBackMessage.class;
                    break;
                case "FUNC_LOGIN":
                    jsonClass = LoginMessage.class;
                    break;
            }
            msg.add(JSON.parseObject(jsonObject.toJSONString(), jsonClass));
        }
        return msg;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void add(Message message) {
        this.messages.add(message);
    }

    public void setFunc(boolean func) {
        this.func = func;
    }

    public boolean isFunc() {
        return func;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getStreamId() {
        return streamId;
    }
}

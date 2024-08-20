package com.lovelycatv.ai.shadowcat.app.im.codec;

import com.alibaba.fastjson2.JSONObject;
import com.lovelycatv.ai.shadowcat.app.im.message.MessageChain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MyObjectEncoder extends MessageToByteEncoder<MessageChain> {
    private static final Serializer SERIALIZER = new Serializer();

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageChain msg, ByteBuf out) throws Exception {
        String jsonString = JSONObject.toJSONString(msg);
        byte[] jsonBytes = jsonString.getBytes(Charset.defaultCharset());

        System.out.println(jsonBytes.length);
        System.out.println(jsonString);

        out.writeInt(jsonBytes.length);
        out.writeBytes(jsonBytes);
    }

    static class Serializer {
        public byte[] serialize(Object obj) {
            // 使用适当的序列化机制，例如Java序列化
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
                out.writeObject(obj);
                out.flush();
                return bos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
 

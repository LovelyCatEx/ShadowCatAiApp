package com.lovelycatv.ai.shadowcat.app.im.codec;

import com.lovelycatv.ai.shadowcat.app.im.message.MessageChain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MyObjectDecoder extends ByteToMessageDecoder {
    private static final Deserializer DESERIALIZER = new Deserializer();
 
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        String jsonString = new String(bytes, Charset.defaultCharset());
        // System.out.println("Netty: " + jsonString);

        out.add(MessageChain.decodeJSON(jsonString));
    }

    static class Deserializer {
        public <T> T deserialize(byte[] data, Class<T> clazz) {
            // 使用适当的反序列化机制
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
                return (T) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
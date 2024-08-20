package com.lovelycatv.ai.shadowcat.app.im.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class CustomFrameDecoder extends LengthFieldBasedFrameDecoder {

    public CustomFrameDecoder() {
        super(4096, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        return super.decode(ctx, in);
    }
}
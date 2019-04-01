package com.rpc.handers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;

@Data
public class ResultHandler extends ChannelInboundHandlerAdapter {
    private Object response;

    /**
     * 读取远程调用返回值
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        response = msg;
        ctx.close();
    }
}

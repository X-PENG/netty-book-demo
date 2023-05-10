package com.peng.sixthnettydemo.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiscardClientHandler extends SimpleChannelInboundHandler<Object> {
    private ByteBuf buf;
    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("[DiscardClientHandler.channelActive]");
        this.ctx = ctx;

        // 初始化buf
        this.buf = ctx.alloc().directBuffer(256).writeZero(256);

        // 触发发送消息
        sendDataToServer();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("[DiscardClientHandler.channelInactive]");
        buf.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("[DiscardClientHandler.exceptionCaught]");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 无需读取数据，不用实现...
    }

    private void sendDataToServer() {
        ctx.writeAndFlush(buf.retainedDuplicate()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // 继续发送
                    sendDataToServer();
                } else {
                    log.warn("[DiscardClientHandler.sendDataToServer] fail");
                    future.cause().printStackTrace();
                    future.channel().close();
                }
            }
        });
    }

}

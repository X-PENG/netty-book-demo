package com.peng.sixthnettydemo.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {
    // 记录读取的次数
    private int readCount = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        try {
            readCount++;
            int tmpCount = 0;
            // 读取数据，打印
            while (buf.isReadable()) {
                tmpCount++;
                log.info("[DiscardServerHandler] channelRead[{} | {}] content:{}", readCount, tmpCount, ((char) buf.readByte()));
            }
//            log.info("[DiscardServerHandler] channelRead[{}] content:{}", readCount, buf.toString());
        } finally {
            // finally 保证一定会释放buf，避免内存泄漏
            // 直接丢弃消息
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // TODO 作用是啥？
        ctx.close();
    }
}

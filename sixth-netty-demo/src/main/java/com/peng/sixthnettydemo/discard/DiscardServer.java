package com.peng.sixthnettydemo.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiscardServer {
    private int port;

    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws InterruptedException {
        // 循环处理IO事件的线程组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1); // 通常只需要1个线程处理连接请求
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .group(bossGroup, workerGroup)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 监听端口号，返回异步任务
            ChannelFuture bindFuture = serverBootstrap.bind(port);
            // 增加异步回调
            bindFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("[DiscardServer] bind port:{} success...", port);
                    } else {
                        log.error("[DiscardServer] bind port:{} occur error:{}", port, future.cause());
                        future.channel().close();
                    }
                }
            });
            bindFuture.sync();

            log.info("[DiscardServer] bind finished...");

            Channel serverChannel = bindFuture.channel();
            ChannelFuture closeFuture = serverChannel.closeFuture();
            // 阻塞，等待server关闭。TODO 如何关闭？
            closeFuture.sync();

            log.info("[DiscardServer] closed...");
        } finally {
            // 释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DiscardServer server = new DiscardServer(8080);
        server.run();
    }
}

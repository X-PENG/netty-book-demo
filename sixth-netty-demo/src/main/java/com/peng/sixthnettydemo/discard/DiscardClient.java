package com.peng.sixthnettydemo.discard;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiscardClient {
    private String remoteHost;
    private int remotePort;

    public DiscardClient(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public void run() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class)
                    .group(group)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DiscardClientHandler());
                        }
                    });

            ChannelFuture connectFuture = bootstrap.connect(remoteHost, remotePort);
            connectFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("[DiscardClient] connect port:{} success", remotePort);
                    } else {
                        log.error("[DiscardClient] connect port:{} occur err:{}", remotePort, future.cause());
                        future.channel().close();
                    }
                }
            });
            connectFuture.sync();
            log.info("[DiscardClient] connect finished...");

            Channel channel = connectFuture.channel();
            channel.closeFuture().sync();
            log.info("[DiscardClient] closed...");
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new DiscardClient("127.0.0.1", 8080).run();
    }
}

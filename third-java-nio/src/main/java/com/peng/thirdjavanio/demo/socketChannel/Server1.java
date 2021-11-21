package com.peng.thirdjavanio.demo.socketChannel;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author xiezhipeng <xiezhipeng.peng@bytedance.com>
 * @Date 2021/11/21
 */
@Slf4j
public class Server1 {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", 9001));
//        serverSocketChannel.configureBlocking(false);
        SocketChannel socketChannel = serverSocketChannel.accept();
        System.out.println(socketChannel);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        while (socketChannel.read(byteBuffer) != -1) {
            byteBuffer.flip();
            byte[] bytes = new byte[byteBuffer.limit()];
            while (byteBuffer.position() < byteBuffer.limit()) {
                bytes[byteBuffer.position()] = byteBuffer.get();
            }
            log.info("receive: {}", new String(bytes));
            byteBuffer.clear();
        }
        log.info("server end");
    }
}

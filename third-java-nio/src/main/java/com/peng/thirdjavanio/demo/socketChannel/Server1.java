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
        // 使用非阻塞模式时，若客户端没发送数据，socketChannel.read会返回0。为什么！？因为channel没关闭，不会返回-1，但又没读到数据，因此只能返回0。
//        socketChannel.configureBlocking(false);
        log.info("socketChannel: {}", socketChannel);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int readLen = 0;
        while ((readLen = socketChannel.read(byteBuffer)) != -1) {
            log.info("--readLen:{}--", readLen);
            byteBuffer.flip();
            byte[] bytes = new byte[byteBuffer.limit()];
            while (byteBuffer.hasRemaining()) {
                bytes[byteBuffer.position()] = byteBuffer.get();
            }
            log.info("receive: {}", new String(bytes));
            byteBuffer.clear();
        }
        log.info("server end");
    }
}

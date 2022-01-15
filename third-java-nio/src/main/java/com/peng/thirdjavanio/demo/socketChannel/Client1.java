package com.peng.thirdjavanio.demo.socketChannel;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


/**
 * @author xiezhipeng <xiezhipeng.peng@bytedance.com>
 * @Date 2021/11/21
 */
@Slf4j
public class Client1 {
    public static void main(String[] args) throws IOException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        boolean connect = socketChannel.connect(new InetSocketAddress("127.0.0.1", 9001));
        System.out.println(connect);
        while (!socketChannel.finishConnect()) {
        }
        System.out.println("connect success");
        System.out.println("localAddress:" + socketChannel.getLocalAddress().toString());
        System.out.println("remoteAddress:" + socketChannel.getRemoteAddress().toString());
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put("hello world".getBytes());
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
//        socketChannel.shutdownOutput(); // 测试sleep之前shutdown
        Thread.sleep(10000);
        socketChannel.shutdownOutput();
        socketChannel.close();
        log.info("client end");
    }
}

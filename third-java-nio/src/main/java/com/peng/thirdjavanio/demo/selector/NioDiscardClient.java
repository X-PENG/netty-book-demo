package com.peng.thirdjavanio.demo.selector;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NioDiscardClient {
    public static List<SocketChannel> socketChannelList = new ArrayList<>();
    public static ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    /**
     * 连接server，发送数据
     * @param args
     */
    public static void main(String[] args) throws IOException, InterruptedException {
//        for (int i = 0; i < 2; i++) { // 测试多个客户端
            startClient();
//        }
        while (true){
            TimeUnit.SECONDS.sleep(3);
        }
    }

    public static void startClient() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("localhost", 9800));
        while (!socketChannel.finishConnect()) {}
        log.info("--connected socketChannel:{}---", socketChannel);
        int i = 100;
        while (i-- > 0) {
            // debug试试每次发送会发生啥
            write(socketChannel, "data" + i);
        }
        socketChannel.shutdownOutput();
        socketChannel.close();
        log.info("---close socketChannel:{}---", socketChannel);
//        socketChannelList.add(socketChannel);
    }

    public static void write(SocketChannel socketChannel, String msg) throws IOException {
        byteBuffer.clear();
        byteBuffer.put(msg.getBytes());
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        byteBuffer.clear();
    }
}

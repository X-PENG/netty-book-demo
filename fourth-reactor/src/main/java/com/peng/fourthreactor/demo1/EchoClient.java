package com.peng.fourthreactor.demo1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * @author xiezhipeng <xiezhipeng.peng@bytedance.com>
 * @Date 2023/04/15
 */
@Slf4j
public class EchoClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(Const.socketAddress);
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        while (true) {
            int select = selector.select();
            if (select <= 0) {
                continue;
            }

            log.info("EchoClient select:{}", select);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey curKey = iterator.next();
                iterator.remove();
                SocketChannel curChannel = (SocketChannel) curKey.channel();

                if (curKey.isConnectable()) {
                    log.info("connect success {}", curChannel.finishConnect());
                    curKey.interestOps(SelectionKey.OP_WRITE);
                }

                if (curKey.isWritable()) {
                    String identify = curChannel.getLocalAddress().toString();
                    String msg = "hello, this is " + identify;
                    ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
                    // 简单处理，不考虑缓冲区满的问题，认为能一次写入
                    int write = curChannel.write(buffer);
                    log.info("client send:{}", write);
                    curKey.interestOps(SelectionKey.OP_READ);
                }

                if (curKey.isReadable()) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    // 认为能一次读取完
                    int read = curChannel.read(byteBuffer);
                    if (read == -1) {
                        log.info("read close");
                        continue;
                    }
                    if (read == 0) {
                        log.info("read nothing");
                        continue;
                    }
                    byte[] bufferArray = byteBuffer.array();
                    byte[] data = new byte[read];
                    System.arraycopy(bufferArray, 0, data, 0, read);
                    log.info("receive data from server: {}", new String(data));
                }
            }
        }
    }
}

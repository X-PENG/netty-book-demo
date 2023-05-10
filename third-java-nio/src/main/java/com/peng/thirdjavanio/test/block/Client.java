package com.peng.thirdjavanio.test.block;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9001));

        Selector selector = Selector.open();
        SelectionKey key = socketChannel.register(selector, SelectionKey.OP_CONNECT);

        // 创建要发送的数据
        StringBuilder sb = new StringBuilder();
        // 1MB的数据 TODO 可以适当增大或缩小发送数据量，控制发送次数
        for (int i = 0; i < 1024 * 1024 * 1; i++) {
            sb.append("a");
        }
        // 此处返回的ByteBuffer已经是「读模式」了，不用再调用flip方法
        ByteBuffer byteBuffer = Charset.defaultCharset().encode(sb.toString());
        // 将要写的数据绑定到selectionKey上
        key.attach(byteBuffer);

        int writeCount = 0;

        log.info("start write, position:{}, limit:{}, capacity:{}", byteBuffer.position(), byteBuffer.limit(), byteBuffer.capacity());
        while (true) {
            int select = selector.select();
            if (select <= 0) {
                continue;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey selectionKey = keyIterator.next();
                keyIterator.remove();
                if (selectionKey.isConnectable()) {
                    // TODO 为啥必须调用finishConnect方法程序才正常？
                    log.info("connect success....{}", ((SocketChannel) selectionKey.channel()).finishConnect());
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    continue;
                }

                if (selectionKey.isWritable()) {
                    writeCount++;
                    // 获取需要写的数据
                    ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                    log.info("write round {} start, position:{}, limit:{}, capacity:{}", writeCount, buffer.position(), buffer.limit(), buffer.capacity());
                    // 如果有剩余的没写完继续写
                    if (buffer.hasRemaining()) {
                        SocketChannel sc = (SocketChannel) selectionKey.channel();
                        // 非阻塞写，当发送缓冲区满了就不可写了，此时会直接返回；若是阻塞IO，当发送缓冲区满了，写会阻塞直到所有数据都写到缓冲区
                        int write = sc.write(buffer);
                        log.info("write round {} end, writeData:{}, position:{}, limit:{}, capacity:{}", writeCount, write, buffer.position(), buffer.limit(), buffer.capacity());
                    } else {
                        log.info("write round {} finished", writeCount);
                        // TODO 一定要cancel吗？
//                        selectionKey.cancel();
                        socketChannel.close();
                    }
                }

            }
        }
    }
}

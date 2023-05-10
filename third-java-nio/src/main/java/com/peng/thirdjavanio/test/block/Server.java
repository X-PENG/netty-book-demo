package com.peng.thirdjavanio.test.block;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", 9001));
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        int readCount = 0;
        log.info("start");
        while (true) {
            int select = selector.select();
            if (select <= 0) {
                continue;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keysIterator = selectedKeys.iterator();
            while (keysIterator.hasNext()) {
                SelectionKey selectionKey = keysIterator.next();
                keysIterator.remove();

                if (selectionKey.isAcceptable()) {
                    SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
                    if (socketChannel == null) {
                        log.info("accept null");
                        continue;
                    }

                    log.info("accept success:{}, selectorSame:{}", socketChannel.getRemoteAddress(), selector == selectionKey.selector());
                    socketChannel.configureBlocking(false);
                    SelectionKey newKey = socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    // 绑定一个ByteArrayOutputStream，将该SocketChannel读到的数据都写到byteArrayOutputStream中
                    newKey.attach(byteArrayOutputStream);
                    continue;
                }
                if (selectionKey.isReadable()) {
                    readCount++;
                    log.info("read round {} start", readCount);
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    // TODO 可以适当增大或缩小Buffer，控制读取次数
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 128);
                    int readLen = socketChannel.read(byteBuffer);

                    if (readLen == -1) {
                        // 输出读取到的一部分数据
                        ByteArrayOutputStream outputStream = (ByteArrayOutputStream) selectionKey.attachment();
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
                        byte[] readBuffer = new byte[100];
                        byteArrayInputStream.read(readBuffer);
                        log.info("read round {} finished, dataSize:{}, data:{}", readCount, readBuffer.length, new String(readBuffer));
                        socketChannel.close();
                    } else if (readLen == 0) {
                        log.info("read nothing");
                        continue;
                    } else {
                        byte[] data = byteBuffer.array();
                        // 将读到的数据往输出流中写
                        ByteArrayOutputStream outputStream = (ByteArrayOutputStream) selectionKey.attachment();
                        outputStream.write(data);
                        log.info("read round {} end, curReadData:{}, readAllData:{}", readCount, data.length, outputStream.size());
                    }
                }
            }
        }
    }
}

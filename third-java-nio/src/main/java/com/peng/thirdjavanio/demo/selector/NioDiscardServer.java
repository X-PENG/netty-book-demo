package com.peng.thirdjavanio.demo.selector;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author xiezhipeng <xiezhipeng.peng@bytedance.com>
 * @Date 2022/01/15
 */
@Slf4j
public class NioDiscardServer {

    /**
     * serverSocket注册到selector
     * serverSocket接收连接，并连接成功的socket注册到selector
     * @param args
     */
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress("localhost", 9800));
        log.info("serverSocketChannel.validOps(): {}", serverSocketChannel.validOps());
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 使用selector监听channel的IO事件
        while (true) {
            /**
             * 为什么客户端关闭了select返回值一直大于0，且selectionKey对象和上次select结果相同！！？
             * 因为客户端程序的socketChannel关闭了，但服务端这边的socketChannel还没close（期待服务端这边也close），select就会一直认为有IO事件就绪（即已关闭事件，属于读就绪），
             * 只要服务端这边socketChannel也close即可！！！
             */
            int select = selector.select();
            if (select <= 0) {
                continue;
            }
            // SelectionKey应该是Channel通道维度的，一个通道每次IO事件就绪，都会返回相同的SelectionKey对象。
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            log.info("select:{}, selectionKeys.size:{}, selectionKeys:{}", select, selectionKeys.size(), selectionKeys);

            Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
            while (selectionKeyIterator.hasNext()) {
                SelectionKey selectionKey = selectionKeyIterator.next();
                if (selectionKey.isAcceptable()) {
                    log.info("--isAcceptable--");
                    log.info("【isAcceptable】serverSocketChannel == selectionKey.channel(): {}", serverSocketChannel == selectionKey.channel());
                    SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
                    // 因为是非阻塞模式，accept会立即返回，可能返回null
                    if (socketChannel == null) {
                        log.warn("socketChannel == null");
                        continue;
                    }
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    log.info("--isReadable--");
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    int readLen = 0;
                    while (true) {
                        readLen = socketChannel.read(byteBuffer);
                        if (readLen <= 0) {
                            log.info("readLen: {}", readLen);
                            if (readLen == -1) {
//                                socketChannel.shutdownInput();
//                                socketChannel.close();
                            }
                            break;
                        }
                        log.info("------read socketChannel:{}----", socketChannel);
                        log.info(" content:{}", new String(readByteBuffer(byteBuffer)));
                    }
                } else {
                    log.error("------error-----");
                }

                // 处理完需要remove，否则下次select后会重复处理这个selectionKey
                selectionKeyIterator.remove();
            }
        }
    }

    public static byte[] readByteBuffer(ByteBuffer byteBuffer) {
        byteBuffer.flip();
        byte[] result = new byte[byteBuffer.limit()];
        while (byteBuffer.hasRemaining()) {
            result[byteBuffer.position()] = byteBuffer.get();
        }
        byteBuffer.clear();
        return result;
    }
}

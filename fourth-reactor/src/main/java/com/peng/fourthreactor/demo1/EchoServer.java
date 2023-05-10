package com.peng.fourthreactor.demo1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * 回显服务器，实现单Reactor单线程模式（（实现的不够抽象，有兴趣可以设计的更加抽象，可以参考Netty的设计））。
 * 思考：
 * 1. Reactor监听到IO事件后怎么知道分发给哪个handler？
 * 感觉reactor不应该理解分发给哪个handler，reactor本身只是负责监听和分发，但具体分发给哪个handler是业务逻辑，不应该由reactor负责，
 * reactor应该约定一个协议，要求注册的channel实现handler路由分发逻辑，而reactor只负责在有IO事件时触发分发。
 *
 */
@Slf4j
public class EchoServer {
    ServerSocketChannel serverSocketChannel;
    Selector selector;// 作为Reactor监听IO事件

    public EchoServer(SocketAddress socketAddress) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(socketAddress);
        selector = Selector.open();
        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 绑定Handler
        selectionKey.attach(new AcceptHandler());
    }

    public void run() throws IOException {
        log.info("EchoServer run...");
        while (true) {
            int select = selector.select();
            if (select <= 0) {
                continue;
            }
            log.info("EchoServer select:{}", select);

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();

                // 重点：协议约定每个selectionKey的attachment都是Handler
                ((Handler) selectionKey.attachment()).Dispatch(selectionKey);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        EchoServer echoServer = new EchoServer(Const.socketAddress);
        echoServer.run();
    }
}

interface Handler {
    // 分发并处理SelectionKey，由子Handler具体实现
    void Dispatch(SelectionKey selectionKey) throws IOException;
}

@Slf4j
class AcceptHandler implements Handler {

    @Override
    public void Dispatch(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isAcceptable()) {
            SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
            if (socketChannel == null) {
                return;
            }
            log.info("accept success {}", socketChannel.getRemoteAddress());
            socketChannel.configureBlocking(false);
            SelectionKey newKey = socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
            // 绑定Handler
            newKey.attach(new IOHandler());
        }
    }
}

@Slf4j
class IOHandler implements Handler {

    ByteBuffer readBuffer;

    public IOHandler() {
    }

    @Override
    public void Dispatch(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        // 大致逻辑：先读取客户端发送的数据，再回写给客户端
        if (selectionKey.isReadable()) {
            // 读取客户端发送的数据 TODO 怎么知道客户端发完一份完整数据呢？（涉及粘包和半包问题）
            // 这里假设一次读取就收到了客户端一份完整的数据（很简单的处理）
            readBuffer = ByteBuffer.allocate(1024);
            int read = socketChannel.read(readBuffer);
            if (read == -1) {
                log.info("close socketChannel [{}]", socketChannel.getRemoteAddress());
                socketChannel.close();
                return;
            }
            if (read == 0) {
                log.info("read nothing");
                return;
            }
            byte[] bufferArray = readBuffer.array();
            byte[] data = new byte[read];
            System.arraycopy(bufferArray, 0, data, 0, read);
            log.info("read data from client[{}] read:{}, msg:{}", socketChannel.getRemoteAddress(), read, new String(data, StandardCharsets.UTF_8));
            readBuffer.flip();

            // 读完之后需要回写
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        }

        if (selectionKey.isWritable()) {
            // 如果没写完要继续写
            if (readBuffer.hasRemaining()) {
                int write = socketChannel.write(readBuffer);
                log.info("send client data:{}", write);
            } else {
                log.info("echo end to client[{}]", socketChannel.getRemoteAddress());
                readBuffer = null;
                // 写完了再读 TODO 能否同时读写呢？
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }
}



package com.peng.thirdjavanio.demo.datagramChannel;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @author xiezhipeng <xiezhipeng.peng@bytedance.com>
 * @Date 2021/11/21
 */
@Slf4j
public class Client1 {
    public static void main(String[] args) throws IOException, InterruptedException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        System.out.println("localAddress:" + datagramChannel.getLocalAddress());
        System.out.println("remoteAddress:" + datagramChannel.getRemoteAddress());
        datagramChannel.configureBlocking(false);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put("hello world, DatagramChanel".getBytes());
        byteBuffer.flip();
        log.info("start send");
        int send = datagramChannel.send(byteBuffer, Constant.address1);
        log.info("send: {}", send);
        Thread.sleep(3000);

        byteBuffer.clear();
        byteBuffer.put("client end".getBytes());
        byteBuffer.flip();
        log.info("start send");
        send = datagramChannel.send(byteBuffer, Constant.address1);
        log.info("send: {}", send);

        log.info("client sleep");
        Thread.sleep(10000);
        log.info("client end");
        datagramChannel.close();
    }
}

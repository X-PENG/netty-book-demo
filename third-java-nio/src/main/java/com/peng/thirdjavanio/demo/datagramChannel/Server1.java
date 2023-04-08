package com.peng.thirdjavanio.demo.datagramChannel;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @author xiezhipeng <xiezhipeng.peng@bytedance.com>
 * @Date 2021/11/21
 */
@Slf4j
public class Server1 {
    public static void main(String[] args) throws IOException, InterruptedException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        System.out.println("localAddress:" + datagramChannel.getLocalAddress());
        System.out.println("remoteAddress:" + datagramChannel.getRemoteAddress());
//        datagramChannel.configureBlocking(false);
        DatagramChannel bindDatagramChannel = datagramChannel.bind(Constant.address1);
        System.out.println(datagramChannel == bindDatagramChannel);
        System.out.println("localAddress:" + datagramChannel.getLocalAddress().toString());
//        System.out.println("remoteAddress:" + datagramChannel.getRemoteAddress().toString());// npe

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        SocketAddress client = null;
        System.out.println("start receive");
        while ((client = datagramChannel.receive(byteBuffer)) != null) {
            System.out.println(client.toString());

            byteBuffer.flip();
            byte[] bytes = new byte[byteBuffer.limit()];
            while (byteBuffer.position() < byteBuffer.limit()) {
                bytes[byteBuffer.position()] = byteBuffer.get();
            }
            log.info("receive:{}", new String(bytes));

            byteBuffer.clear();
         }

        log.info("server sleep");
        Thread.sleep(10000);
        log.info("server end");
    }
}

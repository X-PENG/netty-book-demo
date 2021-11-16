package com.peng.thirdjavanio.demo.channel;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author xiezhipeng <xiezhipeng.peng@bytedance.com>
 * @Date 2021/11/16
 */
@Slf4j
public class ChannelDemo {


    /**
     * 读文件
     */
    @Test
    public void t1() throws IOException {
        URL resource = this.getClass().getResource("/file/t1.txt");
        log.info("path: {}", resource.getPath());
        // 字节流
        FileInputStream fileInputStream = new FileInputStream(resource.getPath());
        // 将字节流装饰成字符流
        InputStreamReader bufferedInputStream = new InputStreamReader(fileInputStream);
        // 字符缓冲流
        BufferedReader bufferedReader = new BufferedReader(bufferedInputStream);

//        如果读取了流，会影响FileChannel的读取
//        log.info("read：{}", bufferedReader.readLine());
//        log.info("read：{}", bufferedReader.readLine());
//        log.info("read：{}", bufferedReader.readLine());
//        log.info("read：{}", bufferedReader.readLine());
//        log.info("read：{}", bufferedReader.readLine());
//        log.info("read：{}", bufferedReader.readLine());
//        log.info("read：{}", bufferedReader.readLine());
//        log.info("read：{}", bufferedReader.readLine());
//        bufferedReader.close();

        FileChannel fileChannel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        fileChannel.read(byteBuffer);
        printBufferInfo(byteBuffer);

        // 处理读到的数据
        byteBuffer.flip();
        printBufferInfo(byteBuffer);
        byte[] byteArray = new byte[byteBuffer.limit()];
        while (byteBuffer.position() < byteBuffer.limit()) {
            byteArray[byteBuffer.position()] = byteBuffer.get();
        }
        System.out.println("FileChannel read：\n" + new String(byteArray));

    }

    /**
     * 写文件
     */
    public void t2() {

    }

    /**
     * 复制文件
     */
    public void t3() {

    }


    /**
     * 测试FileChannel又读又写
     */
    public void t4() {

    }

    private void printBufferInfo(Buffer buffer) {
        log.info("position = {}", buffer.position());
        log.info("limit = {}", buffer.limit());
    }
}

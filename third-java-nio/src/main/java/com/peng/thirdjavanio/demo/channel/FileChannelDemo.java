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
public class FileChannelDemo {


    /**
     * 读文件
     */
    @Test
    public void t1() throws IOException {
        // 会从classpath（类路径）下去读取资源
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
        log.info("first read: {}", fileChannel.read(byteBuffer));
        printBufferInfo(byteBuffer);

        // 处理读到的数据
        byteBuffer.flip();
        printBufferInfo(byteBuffer);
        byte[] byteArray = new byte[byteBuffer.limit()];
        while (byteBuffer.position() < byteBuffer.limit()) {
            byteArray[byteBuffer.position()] = byteBuffer.get();
        }

        // 再次读取
        byteBuffer.clear();
        // 没有数据可读返回-1
        log.info("second read: {}", fileChannel.read(byteBuffer));
        System.out.println("FileChannel read：\n" + new String(byteArray));

    }

    /**
     * 写文件
     */
    @Test
    public void t2() throws IOException {
        String s1 = "How are you?";
        String s2 = "你好吗？";
        String s3 = "I am fine,thanks.";
        String s4 = "我很好，谢谢。";
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put(s1.getBytes());
        byteBuffer.put(s2.getBytes());
        byteBuffer.put(s3.getBytes());
        byteBuffer.put(s4.getBytes());

        byteBuffer.flip();
        URL resource = this.getClass().getResource("/file");
        log.info("path: {}", resource.getPath());
        FileOutputStream fileOutputStream = new FileOutputStream(resource.getPath() + "/t2.txt");
        FileChannel fileChannel = fileOutputStream.getChannel();
        int i = fileChannel.write(byteBuffer);
        log.info("i={}", i);
        fileChannel.close();
        fileOutputStream.close();
    }

    /**
     * 复制文件
     */
    @Test
    public void t3() throws IOException {
        URL resourceDir = this.getClass().getResource("/file");
        FileInputStream fileInputStream = new FileInputStream(resourceDir.getPath() + "/1-尚硅谷项目课程系列之Elasticsearch.pdf");
        FileOutputStream fileOutputStream = new FileOutputStream(resourceDir.getPath() + "/1-尚硅谷项目课程系列之Elasticsearch(copy).pdf");
        copyFile(fileInputStream, fileOutputStream);
    }

    private void copyFile(FileInputStream fileInputStream, FileOutputStream fileOutputStream) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = fileInputStream.getChannel();
            outChannel = fileOutputStream.getChannel();

            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int readCount = 0;
            while (inChannel.read(byteBuffer) != -1) {
                readCount++;
                byteBuffer.flip();
                int outLength = outChannel.write(byteBuffer);
                if (outLength != byteBuffer.capacity()) {
                    log.warn("not equals[{}]，outLength={}" , readCount, outLength);
                }
                byteBuffer.clear();
            }
            log.info("final readCount={}", readCount);
        } finally {
            outChannel.close();
            fileOutputStream.close();
            inChannel.close();
            fileInputStream.close();
        }
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

package com.peng.thirdjavanio.demo.channel;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class FileChannelDemo {


    /**
     * 读文件，channel.read(buffer)
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

//        // 测试下用输入流获取的Channel进行写操作
//        byteBuffer.clear();
//        byteBuffer.put("测试一下".getBytes());
//        byteBuffer.flip();
//        fileChannel.write(byteBuffer); // 会报错：java.nio.channels.NonWritableChannelException
//        fileChannel.close();
    }

    /**
     * 写文件，channel.write(buffer)
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
     * 追加文件。
     * 结论：
     * 1. 写文件时，是生成新文件再写，还是在原来文件的基础上追加，是由FileOutputStream决定的（append构造器参数）。
     * 2. 若FileOutputStream是「追加模式」，则fileChannel.write就是追加写。
     */
    @Test
    public void t4() throws IOException {
        URL resource = this.getClass().getResource("/file/append.txt");
        // 流设为追加模式
        FileOutputStream fileOutputStream = new FileOutputStream(resource.getPath(), true);
        FileChannel outChannel = fileOutputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put("追加一第段\n".getBytes());
        byteBuffer.put("追加二第段\n".getBytes());
        byteBuffer.flip();
        printBufferInfo(byteBuffer);
        outChannel.write(byteBuffer);
        outChannel.close();
        fileOutputStream.close();
    }


    /**
     * 测试FileChannel又读又写。
     * 结果：
     * 追加的内容又可以被读到。
     */
    @Test
    public void t5() throws IOException {
        // 注意：是classpath类路径下的资源文件，不是编译前resource中的文件
        URL resource = this.getClass().getResource("/file/readAndWrite.txt");
        FileInputStream inputStream = new FileInputStream(resource.getPath());
        // 从输入流获取的FileChannel不可写，只能读
        FileChannel inChannel = inputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        inChannel.read(byteBuffer);
        byteBuffer.flip();
        printBufferInfo(byteBuffer);
        byte[] bytes = new byte[byteBuffer.limit()];
        while (byteBuffer.position() < byteBuffer.limit()) {
            bytes[byteBuffer.position()] = byteBuffer.get();
        }
        System.out.println("first read:\n" + new String(bytes));

//        FileOutputStream outputStream = new FileOutputStream(resource.getPath()); // 使用该构造器会先删除已存在的文件，再生成新文件
//        FileChannel outChannel = outputStream.getChannel();
        // 可追加
        FileOutputStream outputStream = new FileOutputStream(resource.getPath(), true);
        FileChannel outChannel = outputStream.getChannel();
        byteBuffer.clear();
        byteBuffer.put("写第一段话\n".getBytes());
        byteBuffer.put("写第二段话\n".getBytes());
        byteBuffer.put("写第三段话\n".getBytes());
        byteBuffer.flip();
        outChannel.write(byteBuffer);
        outChannel.close();
        outputStream.close();

        byteBuffer.clear();
        inChannel.read(byteBuffer);
        byteBuffer.flip();
        printBufferInfo(byteBuffer);
        bytes = new byte[byteBuffer.limit()];
        while (byteBuffer.position() < byteBuffer.limit()) {
            bytes[byteBuffer.position()] = byteBuffer.get();
        }
        System.out.println("second read:\n" + new String(bytes));
    }

    /**
     * 随机读，不顺序读。
     * read的时候指定position
     */
    @Test
    public void t6() throws IOException {
        URL resource = this.getClass().getResource("/file/randomRead.txt");

        FileInputStream inputStream = new FileInputStream(resource.getPath());
        FileChannel inChannel = inputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int read = inChannel.read(byteBuffer, "I like apple.\n".length());
        log.info("read length:{}", read);
        byteBuffer.flip();
        printBufferInfo(byteBuffer);
        byte[] bytes = new byte[byteBuffer.limit()];
        while (byteBuffer.position() < byteBuffer.limit()) {
            bytes[byteBuffer.position()] = byteBuffer.get();
        }
        System.out.println("first read:\n" + new String(bytes));
    }

    /**
     * 随机写，不顺序写。
     * 使用追加模式，且write的时候指定position。
     * 结论：会覆盖旧内容，会将position这个位置以及之后length长度的内容更新。
     */
    @Test
    public void t7() throws IOException {
        URL resource = this.getClass().getResource("/file/randomWrite.txt");

        FileOutputStream outputStream = new FileOutputStream(resource.getPath(), true);
        FileChannel fileChannel = outputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put("abc".getBytes());
        byteBuffer.flip();
        int write = fileChannel.write(byteBuffer, 8);// 会覆盖写
        log.info("write1 length:{}", write);
        fileChannel.force(true); //强制刷盘

        byteBuffer.clear();
        byteBuffer.put("ABCDEF".getBytes());
        byteBuffer.flip();
        write = fileChannel.write(byteBuffer, 1); // 会覆盖写
        log.info("write2 length:{}", write);
        fileChannel.force(true);

        byteBuffer.clear();
        byteBuffer.put("ilikeapple.whataboutyou?yes,metoo.".getBytes());
        byteBuffer.flip();
        write = fileChannel.write(byteBuffer, 0); // 会覆盖写
        log.info("write3 length:{}", write);
        fileChannel.force(true);

        fileChannel.close();
        outputStream.close();
    }

    private void printBufferInfo(Buffer buffer) {
        log.info("position = {}", buffer.position());
        log.info("limit = {}", buffer.limit());
    }
}

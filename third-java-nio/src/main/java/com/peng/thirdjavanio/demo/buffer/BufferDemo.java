package com.peng.thirdjavanio.demo.buffer;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.nio.IntBuffer;

/**
 * @author xiezhipeng <xiezhipeng.peng@bytedance.com>
 * @Date 2021/11/06
 */
@Slf4j
public class BufferDemo {

    /**
     * DEMO：写 读 写 读 重复读
     * 上溢：一直写会出现上溢
     * 下溢：一直读会出现下溢
     */
    @Test
    public void t1() {
        // 创建Buffer
        IntBuffer intBuffer = IntBuffer.allocate(20);
        printBufferInfo(intBuffer);
        // 写入数据
        while (intBuffer.position() < intBuffer.limit()) {
            intBuffer.put(intBuffer.position());
        }
        printBufferInfo(intBuffer);

        // 读取数据
        intBuffer.flip();
        printBufferInfo(intBuffer);
        log.info("read: {}", intBuffer.get());
        printBufferInfo(intBuffer);
        for (int i = 0; i < intBuffer.limit() - 1; i++) {
            log.info("read(for): {}", intBuffer.get());
        }

        // 再写入数据
        intBuffer.clear();
        printBufferInfo(intBuffer);
        int start = 100;
        while (intBuffer.position() < intBuffer.limit() - intBuffer.capacity() / 2) {
            intBuffer.put(start++);
        }

        // 再读取数据
        intBuffer.flip();
        printBufferInfo(intBuffer);
        log.info("read: {}", intBuffer.get());
        log.info("read: {}", intBuffer.get());
        log.info("read: {}", intBuffer.get());
        printBufferInfo(intBuffer);
        while (intBuffer.position() < intBuffer.limit()) {
            log.info("read(while): {}", intBuffer.get());
        }
        printBufferInfo(intBuffer);

        // 重复读取数据
        intBuffer.rewind();
        printBufferInfo(intBuffer);
        while (intBuffer.position() < intBuffer.limit()) {
            log.info("read(while): {}", intBuffer.get());
        }
    }

    private void printBufferInfo(IntBuffer intBuffer) {
        log.info("position = {}", intBuffer.position());
        log.info("capacity = {}", intBuffer.capacity());
        log.info("limit = {}", intBuffer.limit());
    }

    /**
     * 使用compact压缩，可以在没读完的基础上接着写
     */
    @Test
    public void t2() {
        IntBuffer intBuffer = IntBuffer.allocate(10);
        intBuffer.put(1);
        intBuffer.put(2);
        intBuffer.put(3);
        intBuffer.put(4);
        intBuffer.put(5);
        intBuffer.put(6);
        printBufferInfo(intBuffer);

        intBuffer.flip();
        // get(i)不会移动position
        log.info("{}", intBuffer.get(2));
        log.info("{}", intBuffer.get(1));
        log.info("{}", intBuffer.get(0));
        // get方法才会移动position
        log.info("{}", intBuffer.get());
        log.info("{}", intBuffer.get());
        log.info("{}", intBuffer.get());
        printBufferInfo(intBuffer);

        intBuffer.compact();
        intBuffer.put(7);
        intBuffer.put(8);
        intBuffer.put(9);
        intBuffer.put(10);
        intBuffer.flip();
        while (intBuffer.position() < intBuffer.limit()) {
            log.info("read(while): {}", intBuffer.get());
        }
    }

    /**
     * mark和reset
     */
    @Test
    public void t3() {
        IntBuffer intBuffer = IntBuffer.allocate(10);
        intBuffer.put(1);
        intBuffer.put(2);
        intBuffer.put(3);
        intBuffer.put(4);
        intBuffer.put(5);
        intBuffer.put(6);
        intBuffer.put(7);
        intBuffer.put(8);
        intBuffer.put(9);
        intBuffer.put(10);

        intBuffer.flip();
        while (intBuffer.position() < intBuffer.limit()) {
            int i = intBuffer.get();
            if (i == 6) {
                intBuffer.mark();
            }
            log.info("{}", i);
        }

        printBufferInfo(intBuffer);
        log.info("reset");
        intBuffer.reset();
        printBufferInfo(intBuffer);
        while (intBuffer.position() < intBuffer.limit()) {
            log.info("read: {}", intBuffer.get());
        }
    }
}

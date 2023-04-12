package com.peng.fifthasynccallback;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author xiezhipeng <xiezhipeng.peng@bytedance.com>
 * @Date 2022/02/16
 */
@Slf4j
public class TestInterrupt {

    /**
     * 睡眠时被中断，会抛出中断异常，并清除中断标记
     * @throws InterruptedException
     */
    @Test
    public void interrupt() throws InterruptedException {
        Thread thread = new Thread(() -> {
            boolean interrupt = false;
            try {
                log.info("interrupt1:{}", Thread.currentThread().isInterrupted());
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // 可以看到中断标记任务是false
                log.info("InterruptedException, interrupt2:{}", Thread.currentThread().isInterrupted());
                interrupt = true;
            } finally {
                if (interrupt) {
                    // 自己中断自己才行
                    Thread.currentThread().interrupt();
                    log.info("interrupt3:{}", Thread.currentThread().isInterrupted());
                }
            }
        });

        thread.start();

        Thread.sleep(1000);

        log.info("interrupt thread");
        thread.interrupt();

        Thread.sleep(100000000);
    }
}

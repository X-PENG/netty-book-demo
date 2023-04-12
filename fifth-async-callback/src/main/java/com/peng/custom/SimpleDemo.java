package com.peng.custom;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.*;

/**
 * @author xiezhipeng <xiezhipeng.peng@bytedance.com>
 * @Date 2023/04/11
 */
@Slf4j
public class SimpleDemo {

    /**
     * 第一种实现方式：异步任务线程执行完异步任务后，自己触发回调处理
     */
    @Test
    public void Test1() throws InterruptedException {
        // 定义异步任务
        Callable<String> task = () -> {
            for (int i = 0; i < 3; i++) {
                log.info("task1 run[{}]...", i);
                TimeUnit.MILLISECONDS.sleep(500);
            }
            return "task1 success";
        };
        FutureTask<String> futureTask = new FutureTask<>(task);
        // 执行异步任务
        new Thread(() -> {
            futureTask.run();

            // 在异步任务执行完后，异步线程自己触发回调处理
            try {
                String outcome = futureTask.get();
                log.info("回调处理...任务执行结果：{}", outcome);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }).start();

        while (true) {
            TimeUnit.SECONDS.sleep(1);
        }
    }

    /**
     * 第二种实现方式：其他线程等待异步任务完成再进行回调处理
     * @throws InterruptedException
     */
    @Test
    public void Test2() throws InterruptedException {
        // 定义异步任务
        Callable<String> task = () -> {
            for (int i = 0; i < 3; i++) {
                log.info("task2 run[{}]...", i);
                TimeUnit.MILLISECONDS.sleep(500);
            }
            return "task2 success";
        };
        FutureTask<String> futureTask = new FutureTask<>(task);
        // 执行异步任务
        new Thread(futureTask).start();
        // 开启另一个线程对异步任务进行回调处理，不阻塞主线程
        new Thread(() -> {
            String result = null;
            try {
                // 等待异步任务执行完
                result = futureTask.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            log.info("回调处理...任务执行结果：{}", result);
        }).start();

        while (true) {
            TimeUnit.SECONDS.sleep(1);
        }
    }
}

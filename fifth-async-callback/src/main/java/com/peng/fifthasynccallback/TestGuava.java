package com.peng.fifthasynccallback;

import com.google.common.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试Guava的异步回调
 * ListenableFuture：异步任务的抽象
 * FutureCallback：回调处理的抽象
 * Guava线程池（装饰Java线程池）：用于执行异步任务
 *
 * 如何实现异步回调的？
 * 现象：可以看到回调处理线程既不是主线程也不是异步任务线程。
 *
 * 实现原理：
 * 注册回调时，若异步任务已经执行完了，则直接让线程池执行回调任务——CallbackListener；
 * 注册回调时，若异步任务还没有执行完：
 * 如果还是直接让线程池执行回调任务，则会阻塞线程，浪费线程资源；
 * 因此guava的做法是，此时，只将回调任务加入到一个『链表』中，然后异步线程执行完异步任务后，会「主动触发」回调处理：将所有回调任务给线程池执行。
 *
 * 源码：
 * CallbackListener就是回调任务，是Runnable类型，封装了Future和FutureCallback，它的run方法实现了执行回调的逻辑
 * AbstractFuture#complete(com.google.common.util.concurrent.AbstractFuture)：在异步任务执行完后，主动触发回调处理
 *
 * 总结：
 * 实现异步回调，要么被动触发回调，要么主动触发回调：
 * 被动触发回调：其他线程等待异步线程执行完，然后正常返回则onSuccess，异常则onFailure;（缺陷：会阻塞其他线程，浪费线程资源）
 * 主动触发回调：异步任务线程自己在任务执行完后主动触发回调：要么自己亲自执行回调，要么让其他线程执行。
 */
@Slf4j
public class TestGuava {

    @Test
    public void guavaTest() throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        // 装饰者模式，增强线程池，submit方法会返回ListenableFuture，实际还是委托threadPool来执行任务的
        ListeningExecutorService decoratedPool = MoreExecutors.listeningDecorator(threadPool);
        boolean throwException = false;
        ListenableFuture<?> listenableFuture = decoratedPool.submit(() -> {
            log.info("===task start===");
            try {
                log.info("===task run===");
                Thread.sleep(5000);
//                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("", e);
            }
            // 用于测试异常的
            if (throwException) {
                log.error("===task异常===");
                throw new RuntimeException("测试异常");
            }
            log.info("===task end===");
            return "hello world";
        });

//        log.info("sleep 3s");
//        Thread.sleep(3000);

        // 注册异步回调处理
        Futures.addCallback(listenableFuture, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                log.info("onSuccess, result:{}", result);
            }

            @Override
            public void onFailure(Throwable t) {
                log.info("===onFailure===");
            }
        }, decoratedPool);

        log.info("主线程开启异步任务后继续推进主线任务...");
        while (true) {
            Thread.sleep(3000);
        }
    }

}

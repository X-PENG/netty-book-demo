package com.peng.fifthasynccallback;

import com.google.common.util.concurrent.*;
import io.netty.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author xiezhipeng <xiezhipeng.peng@bytedance.com>
 * @Date 2022/02/16
 */
@Slf4j
public class Demo {

    /**
     * Java的FutureTask
     */
    @Test
    public void t1() {

    }

    /**
     * Guava的异步回调
     * ListenableFuture
     * FutureCallback
     * Guava线程池（装饰Java线程池）
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
    @Test
    public void guavaTest() throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        // 装饰者模式，增强线程池，submit方法会返回ListenableFuture，实际还是委托threadPool来执行任务的
        ListeningExecutorService decoratedPool = MoreExecutors.listeningDecorator(threadPool);
        boolean throwException = false;
        ListenableFuture<?> listenableFuture = decoratedPool.submit(() -> {
            log.info("===task start===");
            try {
                Thread.sleep(10000);
//                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("", e);
            }
            if (throwException) {
                log.error("===task异常===");
                throw new RuntimeException("测试异常");
            }
            log.info("===task end===");
            return "hello world";
        });

//        log.info("sleep 3s");
//        Thread.sleep(3000);

        // 注册异步回调
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

        log.info("sleep...");
        while (true) {
            Thread.sleep(3000);
        }
    }


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
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                log.info("InterruptedException, interrupt2:{}", Thread.currentThread().isInterrupted());
                interrupt = true;
            } finally {
                if (interrupt) {
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


    /**
     * Netty的异步回调体系
     *
     * Future（和JDK的Future接口同名）
     * GenericFutureListener(监听异步任务)
     * 使用了观察者模式：「主题」是异步任务Future  「观察者」是监听器GenericFutureListener  当Future完成时，会通知Listener
     *
     * 原理：
     * 注册监听器时，会检查异步任务是否执行完
     * 1. 若已经执行完，则主线程会直接触发通知监听器：实际是开启另一个线程通知监听器，由其他线程执行回调逻辑；
     * 2. 若没有执行完，则只会将Listener保存到列表中；然后，等到异步任务执行完后（setSuccess方法，一般是异步线程自己调用setSuccess），由异步线程「主动触发」通知监听器：也是开启另一个线程，由其他线程执行回调逻辑。
     */
    @Test
    public void NettyTest() throws InterruptedException {
        EventExecutor executor = new SingleThreadEventExecutor(null, new DefaultThreadFactory(SingleThreadEventExecutor.class), true) {
            @Override
            protected void run() {
                Runnable task = takeTask();
                if (task != null) {
                    task.run();
                }
            }
        };
        final Promise<String> promise = new DefaultPromise<String>(executor);

        // 立即完成异步任务，测试如何触发监听器。结果：注册监听器时，发现异步任务已经完成了，主线程会立即触发通知监听器：会开启另一个线程通知监听器，有其他线程执行回调逻辑。
//        promise.setSuccess("immediately");

        promise.addListener(new GenericFutureListener<io.netty.util.concurrent.Future<? super String>>() {
            @Override
            public void operationComplete(io.netty.util.concurrent.Future<? super String> future) throws Exception {
                log.info("任务执行完成，result:{}", future.get());
            }
        });

//        promise.sync(); // 测试

        // 先注册监听器，过一会儿才完成任务，测试如何触发监听器。结果：注册监听器时，异步任务未完成，只会保存监听器列表中；在异步任务完成后（setSuccess），（异步线程）会主动触发通知监听器（此处，因为是主线程setSuccess，所以是主线程触发）：也是开启另一个线程，由其他线程执行回调逻辑。
        Thread.sleep(3000);
        promise.setSuccess("delay");

        promise.sync();
        log.info("===sleep...===");
        while (true) {
            Thread.sleep(100000);
        }
    }
}

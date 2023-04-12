package com.peng.fifthasynccallback;

import io.netty.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * 测试Netty的异步回调体系
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
@Slf4j
public class TestNetty {
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

        promise.addListener(new GenericFutureListener<Future<? super String>>() {
            @Override
            public void operationComplete(io.netty.util.concurrent.Future<? super String> future) throws Exception {
                log.info("任务执行完成，result:{}", future.get());
            }
        });

//        promise.sync(); // 测试

        // 先注册监听器，过一会儿才完成任务，测试如何触发监听器。结果：注册监听器时，异步任务未完成，只会保存监听器列表中；在异步任务完成后（setSuccess），（异步线程）会主动触发通知监听器（此处，因为是主线程setSuccess，所以是主线程触发）：也是开启另一个线程，由其他线程执行回调逻辑。
        Thread.sleep(3000);
        promise.setSuccess("delay");

        log.info("主线程开启异步任务后继续推进主线任务...");
        while (true) {
            Thread.sleep(100000);
        }
    }
}

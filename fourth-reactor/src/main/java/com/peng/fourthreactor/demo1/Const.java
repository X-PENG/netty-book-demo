package com.peng.fourthreactor.demo1;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author xiezhipeng <xiezhipeng.peng@bytedance.com>
 * @Date 2023/04/15
 */
public interface Const {
    SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9001);
}

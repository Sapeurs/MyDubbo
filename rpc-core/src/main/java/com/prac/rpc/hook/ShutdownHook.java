package com.prac.rpc.hook;

import com.prac.rpc.factroy.ThreadPoolFactory;
import com.prac.rpc.util.NacosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将注销服务的方法写到关闭系统的钩子方法中
 *
 * @author: Administrator
 * @date: 2021/7/20 11:14
 * @description: 钩子服务：在某些事件发生后自动去调用的方法
 */
public class ShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);


    //使用单例模式创建对象,保证全局只有一个钩子
    private static final ShutdownHook shutdownHook = new ShutdownHook();

    public static ShutdownHook getShutdownHook() {
        return shutdownHook;
    }

    /**
     * Runtime 对象是 JVM 虚拟机的运行时环境，调用其 addShutdownHook 方法增加一个钩子函数，
     * 创建一个新线程调用 clearRegistry 方法完成注销工作。
     */
    public void addClearAllHook() {
        logger.info("服务端关闭前将自动注销所有服务");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //注销服务
            NacosUtil.clearRegistry();
            //关闭所有线程池
            ThreadPoolFactory.shutDownAll();
        }));
    }

}
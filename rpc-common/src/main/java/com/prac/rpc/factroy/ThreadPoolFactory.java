package com.prac.rpc.factroy;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 创建ThreadPool(线程池)的工具类
 *
 * @author: Sapeurs
 * @date: 2021/7/20 15:06
 * @description:
 */
public class ThreadPoolFactory {

    /**
     * 线程池参数
     */
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAXIMUM_POOL_SIZE_SIZE = 100;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    private final static Logger logger = LoggerFactory.getLogger(ThreadPoolFactory.class);

    //ExecutorService:Java的一个线程池接口
    private static Map<String, ExecutorService> threadPoolsMap = new ConcurrentHashMap<>();

    private ThreadPoolFactory() {
    }

    public static ExecutorService createDefaultThreadPool(String threadNamePrefix) {
        return createDefaultThreadPool(threadNamePrefix, false);
    }

    public static ExecutorService createDefaultThreadPool(String threadNamePrefix, Boolean daemon) {
        //如果key对应的value存在，则直接返回value，否则创建并写入使用第二个参数（函数）计算的值作为value
        ExecutorService pool = threadPoolsMap.computeIfAbsent(threadNamePrefix, k -> createThreadPool(threadNamePrefix, daemon));
        //isShutdown():当调用shutdown()或者shutdownNow()方法后返回为true
        //isTerminated():当调用shutdown()方法后，并且所有提交的任务完成后返回为true;当调用shutdownNow()方法，成功停止后返回为true
        if (pool.isShutdown() || pool.isTerminated()) {
            threadPoolsMap.remove(threadNamePrefix);
            //重新构建一个线程池并且存入Map中
            pool = createThreadPool(threadNamePrefix, daemon);
            threadPoolsMap.put(threadNamePrefix, pool);
        }
        return pool;
    }

    public static void shutDownAll() {
        logger.info("关闭所有线程池...");
        //利用parallelStream()并行关闭所有线程池
        threadPoolsMap.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            //关闭任务
            executorService.shutdown();
            logger.info("关闭线程池[{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                //关闭执行器,阻塞直到关闭请求后所有任务执行完，或者发生超时，或者当前线程被中断
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("关闭线程池失败！");
                //直接关闭，不等任务执行完
                executorService.shutdownNow();
            }
        });
    }

    private static ExecutorService createThreadPool(String threadNamePrefix, Boolean daemon) {
        /**
         * 设置上限为100个线程的阻塞队列
         */
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE_SIZE, KEEP_ALIVE_TIME, TimeUnit.MINUTES, workQueue, threadFactory);
    }

    /**
     * 创建 ThreadFactory . 如果threadNamePrefix不为空则使用自建ThreadFactory，否则使用defaultThreadFactory
     *
     * @param threadNamePrefix 作为创建的线程名字的前缀，指定有意义的线程名称，方便出错时回溯
     * @param daemon           指定是否为 守护线程，当所有的非守护线程结束时，程序也就终止了，同时杀死进程中所有守护线程
     * @return ThreadFactory
     */
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) {
                //使用guava的ThreadFactoryBuilder来自定义创建线程工厂
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        //返回默认的线程池
        return Executors.defaultThreadFactory();
    }

}
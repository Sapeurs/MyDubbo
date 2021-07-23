package com.prac.rpc.transport.socket.server;

import com.prac.rpc.handler.RequestHandler;
import com.prac.rpc.serializer.CommonSerializer;
import com.prac.rpc.transport.AbstractRpcServer;
import com.prac.rpc.transport.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @author: Administrator
 * @date: 2021/7/14 15:23
 * @description: RpcServer的实现类
 */
public class SocketServer extends AbstractRpcServer {


    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    private final ExecutorService threadPool;
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 50;
    private static final long KEEP_ALIVE_TIME = 60;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    private final CommonSerializer serializer;
    private RequestHandler requestHandler = new RequestHandler();

    private String host;
    private int port;

//    protected ServiceRegistry serviceRegistry;
//    protected ServiceProvider serviceProvider;

    public SocketServer(String host, int port) {
        this(host, port, 3);
    }


//    public RpcServer(){
//        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(100);
//        ThreadFactory threadFactory = Executors.defaultThreadFactory();
//        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE,MAX_POOL_SIZE,KEEP_ALIVE_TIME,TimeUnit.SECONDS,workingQueue,threadFactory);
//    }
//    /**
//     * 注册接口
//     * @param service
//     * @param port
//     */
//    @Deprecated
//    public void register(Object service, int port){
//        try(ServerSocket serverSocket = new ServerSocket(port)) {
//            logger.info("服务器正在启动...");
//            Socket socket;
//            while ((socket = serverSocket.accept())!= null){
//                logger.info("客户端连接！IP为：" + socket.getInetAddress());
//                //线程池调用一个工人线程去执行该客户端的连接请求
//                threadPool.execute(new WorkerThread(socket,service));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 为降低耦合度，不把ServiceRegistry和某一个RpcServer绑定在一起，而是在创建RpcServe对象时，
     * 传入一个ServiceRegistry作为这个服务的注册表
     */

    public SocketServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        ArrayBlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workingQueue, threadFactory);
        this.serializer = CommonSerializer.getByCode(serializer);
    }

    /**
     * 启动服务器
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.bind(new InetSocketAddress(host, port));
            logger.info("服务器启动...");
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.info("消费者连接：{} ：{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new SocketRequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("服务器启动时发生错误：", e);
        }
    }


    @Override
    public <T> void publishService(Object service, String serviceName) {

    }
}
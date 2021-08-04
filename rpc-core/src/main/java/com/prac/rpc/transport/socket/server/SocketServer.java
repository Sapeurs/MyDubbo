package com.prac.rpc.transport.socket.server;

import com.prac.rpc.factroy.ThreadPoolFactory;
import com.prac.rpc.handler.RequestHandler;
import com.prac.rpc.hook.ShutdownHook;
import com.prac.rpc.provider.ServiceProviderImpl;
import com.prac.rpc.registry.NacosServiceRegistry;
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
 * @author: Sapeurs
 * @date: 2021/7/14 15:23
 * @description: RpcServer的实现类
 */
public class SocketServer extends AbstractRpcServer {


    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    private final ExecutorService threadPool;
    private final CommonSerializer serializer;
    private RequestHandler requestHandler = new RequestHandler();

    private String host;
    private int port;


    public SocketServer(String host, int port) {
        this(host, port, DEFAULT_SERIALIZER);
    }

    public SocketServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        this.serviceRegistry = new NacosServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
        this.serializer = CommonSerializer.getByCode(serializer);
    }

    /**
     * 启动服务器
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.bind(new InetSocketAddress(host, port));
            logger.info("服务器启动...");
            //添加钩子，服务端关闭时会注销服务
            ShutdownHook.getShutdownHook().addClearAllHook();
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.info("客户端连接：{} ：{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new SocketRequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("服务器启动时发生错误：", e);
        }
    }

}
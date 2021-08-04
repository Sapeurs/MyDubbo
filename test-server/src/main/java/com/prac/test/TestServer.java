package com.prac.test;

import com.prac.rpc.annotation.ServiceScan;
import com.prac.rpc.serializer.CommonSerializer;
import com.prac.rpc.transport.RpcServer;
import com.prac.rpc.transport.socket.server.SocketServer;

/**
 * @author: Sapeurs
 * @date: 2021/7/13 15:44
 * @description: 服务端测试类
 */
@ServiceScan
public class TestServer {

    public static void main(String[] args) {

        //HelloServiceImpl helloService = new HelloServiceImpl();

        /**
         * v1.1
         */
//        ServiceProviderImpl serviceRegistry = new ServiceProviderImpl();
//        //将HelloServiceImpl的对象注册到容器中
//        serviceRegistry.registry(helloService);
//        RpcServer rpcServer = new SocketServer(serviceRegistry);
//        rpcServer.start(9000);

        /**
         * v1.0
         */
        //RpcServer rpcServer = new RpcServer();
        //注册在9000端口
        //rpcServer.register(helloService,9000);


        RpcServer server = new SocketServer("127.0.0.1", 9998, CommonSerializer.DEFAULT_SERIALIZER);
        server.start();

    }

}
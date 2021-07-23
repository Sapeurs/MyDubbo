package com.prac.test;

import com.prac.test.impl.HelloServiceImpl;

/**
 * @author: Administrator
 * @date: 2021/7/13 15:44
 * @description: 服务端测试类
 */
public class TestServer {

    public static void main(String[] args) {

        HelloServiceImpl helloService = new HelloServiceImpl();

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

    }

}
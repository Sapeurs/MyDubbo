package com.prac.test;

import com.prac.rpc.annotation.ServiceScan;
import com.prac.rpc.serializer.CommonSerializer;
import com.prac.rpc.transport.netty.server.NettyServer;

/**
 * @author: Administrator
 * @date: 2021/7/15 9:53
 * @description: 使用Netty的服务端测试
 */
@ServiceScan
public class NettyTestServer {
    public static void main(String[] args) {
//        HelloService helloService = new HelloServiceImpl();
//        ServiceProviderImpl registry = new ServiceProviderImpl();
//        registry.registry(helloService);
//        NettyServer server = new NettyServer();
//        server.start(9999);

        NettyServer server = new NettyServer("127.0.0.1", 9999, CommonSerializer.PROTOBUF_SERIALIZER);
        server.start();

    }
}
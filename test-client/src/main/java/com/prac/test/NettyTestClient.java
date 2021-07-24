package com.prac.test;

import com.prac.rpc.transport.RpcClientProxy;
import com.prac.rpc.transport.netty.client.NettyClient;
import com.rpc.api.ByeService;
import com.rpc.api.HelloObject;
import com.rpc.api.HelloService;

/**
 * @author: Sapeurs
 * @date: 2021/7/15 9:47
 * @description: 使用Netty的客户端测试
 */
public class NettyTestClient {

    public static void main(String[] args) {
//        NettyClient client = new NettyClient("127.0.0.1", 9999);
//        //RpcClientProxy 通过传入不同的 Client（SocketClient、NettyClient）来切换客户端不同的发送方式。

        //使用了Nacos，构造client时不再需要传入地址和端口
        NettyClient client = new NettyClient(2);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject helloObject = new HelloObject(14, "This is a message transported via RPC with Netty");
        String res = helloService.hello(helloObject);
        System.out.println(res);
        ByeService byeService = rpcClientProxy.getProxy(ByeService.class);
        System.out.println(byeService.bye("Server"));


    }

}
package com.prac.rpc.transport;

import com.prac.rpc.entity.RpcRequest;
import com.prac.rpc.entity.RpcResponse;
import com.prac.rpc.transport.netty.client.NettyClient;
import com.prac.rpc.transport.socket.client.SocketClient;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author: Administrator
 * @date: 2021/7/13 11:01
 * @description: Rpc客户端代理类，由于在客户端一侧没有接口的具体实现类，
 * 无法直接生成实例对象，所以通过动态代理的方式生成实例对象，
 * 在调用方法时生成需要的RpcRequest对象并且发送给服务端
 */
@AllArgsConstructor
public class RpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

    //RpcClientProxy 通过传入不同的 Client（SocketClient、NettyClient）来切换客户端不同的发送方式。
    private final RpcClient rpcClient;

    /**
     * 生成需要执行方法的具体接口实现类
     *
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        logger.info("调用方法：{}#{}", method.getDeclaringClass().getName(), method.getName());
//        RpcRequest rpcRequest = RpcRequest.builder()
//                .requestId(UUID.randomUUID().toString())
//                .interfaceName(method.getDeclaringClass().getName())
//                .methodName(method.getName())
//                .parameters(args)
//                .paramTypes(method.getParameterTypes())
//                .heartBeat(false)
//                .build();
        RpcRequest rpcRequest = new RpcRequest(UUID.randomUUID().toString(), method.getDeclaringClass().getName(), method.getName(),
                args, method.getParameterTypes(), false);
        //RpcClient rpcClient = new SocketClient();
        //向服务器发送request对象，返回response对象

        RpcResponse rpcResponse = null;
        if (rpcClient instanceof NettyClient) {
            try {
                CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) rpcClient.sendRequest(rpcRequest);
                rpcResponse = completableFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("方法调用请求发送失败", e);
                return null;
            }
        }
        if (rpcClient instanceof SocketClient) {
            rpcResponse = (RpcResponse) rpcClient.sendRequest(rpcRequest);
        }
        return rpcResponse.getData();
    }
}
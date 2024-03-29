package com.prac.rpc.transport.socket.client;

import com.prac.rpc.entity.RpcRequest;
import com.prac.rpc.entity.RpcResponse;
import com.prac.rpc.enumeration.ResponseCode;
import com.prac.rpc.enumeration.RpcError;
import com.prac.rpc.exception.RpcException;
import com.prac.rpc.loadbalacer.LoadBalancer;
import com.prac.rpc.loadbalacer.RandomLoadBalancer;
import com.prac.rpc.registry.NacosServiceDiscovery;
import com.prac.rpc.registry.ServiceDiscovery;
import com.prac.rpc.serializer.CommonSerializer;
import com.prac.rpc.transport.RpcClient;
import com.prac.rpc.transport.socket.util.ObjectReader;
import com.prac.rpc.transport.socket.util.ObjectWriter;
import com.prac.rpc.util.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Socket方式远程调用的消费者（客户端）
 *
 * @author: Sapeurs
 * @date: 2021/7/14 15:24
 * @description: RpcClient的Socket实现类
 */
public class SocketClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final ServiceDiscovery serviceDiscovery;

    private final CommonSerializer serializer;

    public SocketClient(Integer serializer, LoadBalancer loadBalancer) {
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        this.serializer = CommonSerializer.getByCode(serializer);
    }

    public SocketClient(Integer serializer) {
        this(serializer, new RandomLoadBalancer());
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {

        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }

        //从Nacos获取提供对应服务的服务端地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());

        /**
         * Socket套接字实现TCP网络传输
         * try()中一般放置对资源的申请，若{}中出现异常，()中的资源会自动关闭
         */
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            //使用ObjectWriter通过Socket方式将Request请求序列化并且写入到输出流中
            ObjectWriter.writeObject(outputStream, rpcRequest, serializer);
            //使用ObjectReader通过Socket方式将输入流中的数据反序列化并返回
            RpcResponse rpcResponse = (RpcResponse) ObjectReader.readObject(inputStream);
            if (rpcResponse == null) {
                logger.error("服务调用失败，service:{}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            if (rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                logger.error("调用服务失败，service:{}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            return rpcResponse;
        } catch (IOException e) {
            logger.error("调用时发生错误：", e);
            throw new RpcException("服务调用失败：", e);
        }
    }
}
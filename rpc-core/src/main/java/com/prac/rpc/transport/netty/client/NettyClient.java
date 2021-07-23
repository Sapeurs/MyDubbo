package com.prac.rpc.transport.netty.client;

import com.prac.rpc.entity.RpcRequest;
import com.prac.rpc.entity.RpcResponse;
import com.prac.rpc.enumeration.RpcError;
import com.prac.rpc.exception.RpcException;
import com.prac.rpc.factroy.SingletonFactory;
import com.prac.rpc.loadbalacer.LoadBalancer;
import com.prac.rpc.loadbalacer.RandomLoadBalancer;
import com.prac.rpc.registry.NacosServiceDiscovery;
import com.prac.rpc.registry.ServiceDiscovery;
import com.prac.rpc.serializer.CommonSerializer;
import com.prac.rpc.transport.RpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * RpcClient的Netty实现类
 *
 * @author: Administrator
 * @date: 2021/7/14 21:46
 * @description: 相比于Socket的BIO，采用效率更高的NIO模式
 */
public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private static final EventLoopGroup group;
    private static final Bootstrap bootStrap;
    private final CommonSerializer serializer;

    private final ServiceDiscovery serviceDiscovery;

    private final UnprocessedRequests unprocessedRequests;

    public NettyClient(Integer serializer) {
        this(serializer, new RandomLoadBalancer());
    }

    public NettyClient(Integer serializer, LoadBalancer loadBalancer) {
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        this.serializer = CommonSerializer.getByCode(serializer);
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    static {
        group = new NioEventLoopGroup();
        bootStrap = new Bootstrap();
        bootStrap.group(group)
                .channel(NioSocketChannel.class);
    }

    @Override
    public CompletableFuture<RpcResponse> sendRequest(RpcRequest rpcRequest) {

        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        //AtomicReference<Object> result = new AtomicReference<>(null);
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        try {
            //现在是首先从Nacos中获取到服务的地址和端口，再创建Netty通道channel连接,
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            Channel channel = ChannelProvider.get(inetSocketAddress, serializer);
            //过去是直接使用传入的host和port直接构造channel
            //ChannelFuture future = bootStrap.connect(host, port).sync();
            //logger.info("客户端连接到服务器，地址：{}，端口号：{}", host, port);
            //Channel channel = future.channel();

            if (!channel.isActive()) {
                group.shutdownGracefully();
                return null;
            }
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            //向channel中写入rpcRequest并且监听
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    logger.info(String.format("客户端发送消息：%s", rpcRequest.toString()));
                } else {
                    future1.channel().close();
                    resultFuture.completeExceptionally(future1.cause());
                    logger.error("发送消息时有错误发生：", future1.cause());
                }
            });
            //channel.closeFuture().sync();
            //AttributeMap<AttributeKey,AttributeValue>是板顶在Channel上的，可以设置用来获取通道对象
//                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
            //get()阻塞获取value
//                RpcResponse rpcResponse = channel.attr(key).get();
            //return rpcResponse.getData();
            //return rpcResponse;
        } catch (InterruptedException e) {
            unprocessedRequests.remove(rpcRequest.getRequestId());
            logger.error("发送消息时发生错误：", e);
            Thread.currentThread().interrupt();
        }
        return resultFuture;
    }
}
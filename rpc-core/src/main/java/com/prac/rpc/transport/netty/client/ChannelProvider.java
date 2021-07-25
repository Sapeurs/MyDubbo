package com.prac.rpc.transport.netty.client;

import com.prac.rpc.serializer.CommonSerializer;
import com.prac.rpc.transport.codec.CommonDecoder;
import com.prac.rpc.transport.codec.CommonEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 用于获取Channel对象
 *
 * @author: Sapeurs
 * @date: 2021/7/19 18:28
 * @description:
 */
public class ChannelProvider {

    private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);
    private static EventLoopGroup eventLoopGroup;
    private static Bootstrap bootstrap = initializeBootstrap();

    /**
     * 所有客户端Channel都保存在该Map中
     */
    private static Map<String, Channel> channels = new ConcurrentHashMap<>();

    /**
     * 从Map中获取一个Channel或者新建一个
     * @param inetSocketAddress
     * @param serializer
     * @return
     * @throws InterruptedException
     */
    public static Channel get(InetSocketAddress inetSocketAddress, CommonSerializer serializer) throws InterruptedException {
        String key = inetSocketAddress.toString() + serializer.getCode();
        if (channels.containsKey(key)) {
            Channel channel = channels.get(key);
            if (channels != null && channel.isActive()) {
                return channel;
            } else {
                channels.remove(key);
            }
        }
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                /*
                自定义序列化编解码器
                 */
                //RpcResponse->ByteBuf
                ch.pipeline().addLast(new CommonEncoder(serializer))
                        /*
                        设定IdleStateHandler心跳检测，每5秒进行一次写检测，如果5秒内write()方法未被调用
                        则触发一次userEventTrigger()方法
                         */
                        //实现客户端每5秒向服务端发送一次消息
                        .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                        .addLast(new CommonDecoder())
                        .addLast(new NettyClientHandler());
            }
        });
        Channel channel;
        try {
            channel = connect(bootstrap, inetSocketAddress);
        } catch (ExecutionException e) {
            logger.error("连接客户端时发生错误：", e);
            return null;
        }
        channels.put(key, channel);
        return channel;
    }

    /**
     * Netty客户端创建通道连接
     *
     * @param bootstrap
     * @param inetSocketAddress
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private static Channel connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.info("客户端连接成功");
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }


    /**
     * 初始化并获得一个Bootstrap
     *
     * @return
     */
    private static Bootstrap initializeBootstrap() {
        eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                //设置连接的超时时间，超过这个时间还是建立不上的话代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                //开始TCP底层心跳机制，会主动探测空闲连接的有效性
                .option(ChannelOption.SO_KEEPALIVE, true)
                //关闭Nagle算法，减少延时，该算法的作用是尽可能的发送大数据块，减少网络传输
                .option(ChannelOption.TCP_NODELAY, true);
        return bootstrap;
    }

}
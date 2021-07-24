package com.prac.rpc.transport.netty.server;

import com.prac.rpc.hook.ShutdownHook;
import com.prac.rpc.provider.ServiceProviderImpl;
import com.prac.rpc.registry.NacosServiceRegistry;
import com.prac.rpc.serializer.CommonSerializer;
import com.prac.rpc.transport.AbstractRpcServer;
import com.prac.rpc.transport.codec.CommonDecoder;
import com.prac.rpc.transport.codec.CommonEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author: Sapeurs
 * @date: 2021/7/14 15:47
 * @description: RpcServer的Netty实现类，采用效率更高的NIO模式
 */
public class NettyServer extends AbstractRpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final CommonSerializer serializer;

    public NettyServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        this.serializer = CommonSerializer.getByCode(serializer);
        scanService();
    }

    @Override
    public void start() {
        /*
        boss 线程池的线程负责处理请求的 accept 事件，当接收到 accept 事件的请求时，
        把对应的 socket 封装到一个 NioSocketChannel 中，并交给 worker 线程池，
        其中 worker 线程池负责请求的 read 和 write 事件，由对应的Handler 处理。
         */
        //用于处理客户端新连接的主 "线程池"
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        //用于连接后处理I/O事件的从"线程池"
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        //注册钩子，在服务器关闭时注销服务
        ShutdownHook.getShutdownHook().addClearAllHook();

        try {
            //初始化Netty服务端启动器，作为服务端入口
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //Netty的责任链模式
            //将主从"线程池"初始化到启动器中
            serverBootstrap.group(bossGroup, workerGroup)
                    //设置服务端通道类型
                    .channel(NioServerSocketChannel.class)
                    //日志打印方式
                    .handler(new LoggingHandler(LogLevel.INFO))
                    //配置ServerChannel参数，服务端接收连接的最大队列长度，如果队列已满，客户端连接将被拒绝
                    .option(ChannelOption.SO_BACKLOG, 256)
                    //表示打开 TCP 的 keepAlive 设置, 用来保持长连接。
                    //.option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    //初始化Handler，设置Handler操作
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            //初始化管道
                            ChannelPipeline pipeline = ch.pipeline();
                            /*
                            往管道中添加Handler，入站Handler与出站Handler都必须按实际执行顺序添加，比如先解码再Server处理，那Decoder()就要放在前面
                            但入站和出站之间则互不影响，这里先添加出站Handler再添加入站
                             */
                            pipeline.addLast(new CommonEncoder(serializer));
                            /*
                            设定IdleStateHandler心跳检测每30秒进行一次读检测，如果30秒内ChannelRead()方法未被调用则触发一次userEventTrigger()方法
                             */
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new CommonDecoder());
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            //绑定端口，启动Netty，sync()代表阻塞主Server线程，以执行Netty线程，如果不阻塞，Netty就直接被下面shutdown了
            ChannelFuture future = serverBootstrap.bind(port).sync();
            //等确定通道关闭了，关闭future回到主Server线程
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("启动服务器时发生错误：", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
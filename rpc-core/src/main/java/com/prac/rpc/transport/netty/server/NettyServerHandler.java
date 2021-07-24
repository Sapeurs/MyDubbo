package com.prac.rpc.transport.netty.server;

import com.prac.rpc.entity.RpcRequest;
import com.prac.rpc.entity.RpcResponse;
import com.prac.rpc.factroy.SingletonFactory;
import com.prac.rpc.handler.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty中处理RpcRequest的Handler
 *
 * @author: Sapeurs
 * @date: 2021/7/14 21:14
 * @description: 用于接收RpcRequest，并且执行调用，将调用结果返回封装成RpcResponse发送出去
 * 处理方式和Socket基本一致
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private final RequestHandler requestHandler;

    public NettyServerHandler() {
        this.requestHandler = SingletonFactory.getInstance(RequestHandler.class);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("处理过程调用时发生错误：");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                logger.info("长时间未收到心跳包，断开连接...");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        try {
            if (msg.getHeartBeat()) {
                logger.info("接收到客户端心跳包...");
                return;
            }
            logger.info("服务器接收到请求：{}", msg);
            //String interfaceName = msg.getInterfaceName();
            //Object service = serviceRegistry.getService(interfaceName);
            Object result = requestHandler.handle(msg);
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                ctx.writeAndFlush(RpcResponse.success(result, msg.getRequestId()));
            } else {
                logger.error("通道不可写");
            }
            //ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result, msg.getRequestId()));
            //添加一个监听器到channelFuture来检测是否所有的数据包都发出，然后关闭通道
            //future.addListener(ChannelFutureListener.CLOSE);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
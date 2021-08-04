package com.prac.rpc.transport.socket.server;

import com.prac.rpc.entity.RpcRequest;
import com.prac.rpc.entity.RpcResponse;
import com.prac.rpc.handler.RequestHandler;
import com.prac.rpc.serializer.CommonSerializer;
import com.prac.rpc.transport.socket.util.ObjectReader;
import com.prac.rpc.transport.socket.util.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author: Sapeurs
 * @date: 2021/7/14 11:24
 * @description: 处理线程，从ServiceRegistry获得提供服务的requestHandler对象，
 * 然后交给RequestHandler处理
 */
public class SocketRequestHandlerThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SocketRequestHandlerThread.class);

    private Socket socket;
    private RequestHandler requestHandler;
    private CommonSerializer serializer;

    public SocketRequestHandlerThread(Socket socket, RequestHandler requestHandler, CommonSerializer serializer) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serializer = serializer;
    }

    @Override
    public void run() {

        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            RpcRequest rpcRequest = (RpcRequest) ObjectReader.readObject(inputStream);
            Object result = requestHandler.handle(rpcRequest);
            RpcResponse<Object> response = RpcResponse.success(result, rpcRequest.getRequestId());
            ObjectWriter.writeObject(outputStream, response, serializer);
            //获取流中的RpcRequest对象
            //RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            //String interfaceName = rpcRequest.getInterfaceName();
            //由接口全限定名获得提供服务的service对象
            //Object service = serviceProvider.getServiceProvider(interfaceName);
            //交给RequestHandler处理器处理
            //向流中写入处理结果RpcResponse
            //objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            //objectOutputStream.flush();
        } catch (IOException e) {
            logger.error("调用或发送时发生错误：", e);
        }
    }
}
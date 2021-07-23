package com.prac.rpc.transport.socket.util;

import com.prac.rpc.entity.RpcRequest;
import com.prac.rpc.entity.RpcResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * 工人线程，用于处理客户端传来的request请求
 *
 * @author: Administrator
 * @date: 2021/7/13 15:27
 * @description:
 */
@Data
@AllArgsConstructor
public class WorkerThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);

    private Socket socket;
    private Object service;

    @Override
    public void run() {

        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            //通过class.getMethod方法，传入方法名和方法参数类型即可获得Method对象
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            Object returnObject = method.invoke(service, rpcRequest.getParameters());
            objectOutputStream.writeObject(RpcResponse.success(returnObject, rpcRequest.getRequestId()));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("调用或发送时有错误发生：", e);
        }
    }
}
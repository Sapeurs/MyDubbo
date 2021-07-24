package com.prac.rpc.transport;

import com.prac.rpc.serializer.CommonSerializer;

/**
 * @author: Sapeurs
 * @date: 2021/7/13 15:03
 * @description: 服务端实现
 */
public interface RpcServer {

    int DEFAULT_SERIALIZER = CommonSerializer.DEFAULT_SERIALIZER;

    /**
     * 启动服务端
     */
    void start();

    /**
     * 向Nacos注册服务
     *
     * @param service
     * @param serviceName
     * @param <T>
     */
    <T> void publishService(Object service, String serviceName);
}
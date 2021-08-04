package com.prac.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 作为远程注册表(Nacos)使用，Nacos作为服务注册中心
 * @author: Sapeurs
 * @date: 2021/7/14 10:33
 * @description: 将服务注册到Nacos以供客户端发现
 */
public interface ServiceRegistry {

    /**
     * 将服务的名称和地址注册进服务中心
     *
     * @param serviceName
     * @param inetSocketAddress
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);


}
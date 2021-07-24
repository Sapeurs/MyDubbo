package com.prac.rpc.registry;

import java.net.InetSocketAddress;

/**
 * @author: Sapeurs
 * @date: 2021/7/14 10:33
 * @description: 新：作为远程注册表(Nacos)使用，Nacos作为服务注册中心
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
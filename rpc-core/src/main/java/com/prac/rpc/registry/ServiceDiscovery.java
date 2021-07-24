package com.prac.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 *
 * @author: Sapeurs
 * @date: 2021/7/21 16:20
 * @description:
 */
public interface ServiceDiscovery {

    /**
     * 根据服务名称从注册中心获取到一个服务提供者的地址
     *
     * @param serviceName 服务名称
     * @return 服务实体地址(IP,端口号)
     */
    InetSocketAddress lookupService(String serviceName);
}

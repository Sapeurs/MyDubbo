package com.prac.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.prac.rpc.enumeration.RpcError;
import com.prac.rpc.exception.RpcException;
import com.prac.rpc.util.NacosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 实现一个以Nacos作为注册中心的类
 *
 * @author: Sapeurs
 * @date: 2021/7/19 10:19
 * @description:
 */
public class NacosServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);

    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            //直接向 Nacos 注册服务
            //namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());

            //通过Nacos工具类中的注解注册服务
            NacosUtil.registerService(serviceName, inetSocketAddress);
        } catch (NacosException e) {
            logger.error("注册服务时发生错误：", e);
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILED);
        }
    }


}
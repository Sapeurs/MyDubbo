package com.prac.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.prac.rpc.enumeration.RpcError;
import com.prac.rpc.exception.RpcException;
import com.prac.rpc.loadbalacer.LoadBalancer;
import com.prac.rpc.loadbalacer.RandomLoadBalancer;
import com.prac.rpc.util.NacosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 服务发现
 *
 * @author: Administrator
 * @date: 2021/7/21 16:22
 * @description:
 */
public class NacosServiceDiscovery implements ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceDiscovery.class);

    //负载均衡策略
    private final LoadBalancer loadBalancer;

    public NacosServiceDiscovery(LoadBalancer loadBalancer) {
        //默认选择随机负载均衡策略
        if (loadBalancer == null) this.loadBalancer = new RandomLoadBalancer();
        else this.loadBalancer = loadBalancer;
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            //获得提供某个服务的所有提供者的列表
            List<Instance> instances = NacosUtil.getAllInstance(serviceName);
            if (instances.size() == 0) {
                logger.error("找不到对应的服务：" + serviceName);
                throw new RpcException(RpcError.SERVICE_NOT_FOUND);
            }
            Instance instance = loadBalancer.select(instances);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            logger.error("获取服务时发生错误：", e);
        }
        return null;
    }
}
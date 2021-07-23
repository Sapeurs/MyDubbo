package com.prac.rpc.provider;

import com.prac.rpc.enumeration.RpcError;
import com.prac.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Administrator
 * @date: 2021/7/14 10:37
 * @description: 默认注册表类，用来实现服务注册表
 */
public class ServiceProviderImpl implements ServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderImpl.class);

    /**
     * 保存服务名与提供服务的对象的对应关系
     * key:服务所实现接口的全限定名
     * value:服务对象
     * 改为static保证全局唯一的注册信息，并且创建RpcServer时无需传入
     */
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    /**
     * 保存当前已被注册的对象
     */
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * 向容器注册服务
     *
     * @param service 提供服务的service对象
     * @param <T>
     */
    @Override
    public synchronized <T> void addServiceProvider(T service, String serviceName) {
        //获得service实现接口的完整类名作为服务名
        //String serviceName = service.getClass().getCanonicalName();
        if (registeredService.contains(serviceName)) return;
        registeredService.add(serviceName);
        //获得所注册服务实现的接口，一个对象可能实现多个接口
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            //注册的服务未实现接口
            throw new RpcException(RpcError.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }
        for (Class<?> i : interfaces
        ) {
            serviceMap.put(i.getCanonicalName(), service);
        }
        logger.info("向接口：{} 注册服务：{}", interfaces, serviceName);
    }

    /**
     * 获取服务信息
     *
     * @param serviceName 服务名(service实现接口的完整类名)
     * @return 提供服务的service对象
     */
    @Override
    public synchronized Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        return service;
    }
}
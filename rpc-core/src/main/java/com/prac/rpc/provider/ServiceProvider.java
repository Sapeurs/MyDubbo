package com.prac.rpc.provider;

/**
 * @author: Administrator
 * @date: 2021/7/19 10:14
 * @description: 本地保存服务类的接口
 * 服务注册表接口，一个用来保存本地服务的容器
 */
public interface ServiceProvider {

    /**
     * 注册服务信息
     *
     * @param service
     * @param serviceName
     * @param <T>
     */
    <T> void addServiceProvider(T service, String serviceName);

    /**
     * 由服务名字返回服务的信息
     *
     * @param serviceName
     * @return
     */
    Object getServiceProvider(String serviceName);

}

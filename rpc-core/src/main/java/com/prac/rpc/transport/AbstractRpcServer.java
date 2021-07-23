package com.prac.rpc.transport;

import com.prac.rpc.annotation.Service;
import com.prac.rpc.annotation.ServiceScan;
import com.prac.rpc.enumeration.RpcError;
import com.prac.rpc.exception.RpcException;
import com.prac.rpc.provider.ServiceProvider;
import com.prac.rpc.registry.ServiceRegistry;
import com.prac.rpc.util.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * @author: Administrator
 * @date: 2021/7/21 9:44
 * @description:
 */
public abstract class AbstractRpcServer implements RpcServer {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String host;
    protected int port;

    protected ServiceRegistry serviceRegistry;
    protected ServiceProvider serviceProvider;

    /**
     * 扫描服务
     */
    public void scanService() {
        //从栈底获取main方法
        String mainClassName = ReflectUtil.getStackTrace();
        Class<?> startClass;
        try {
            startClass = Class.forName(mainClassName);
            if (!startClass.isAnnotationPresent(ServiceScan.class)) {
                logger.error("启动类缺少@ServiceScan注解");
                throw new RpcException(RpcError.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
        } catch (ClassNotFoundException e) {
            logger.error("出现未知错误");
            throw new RpcException(RpcError.UNKNOWN_ERROR);
        }
        String basePackage = startClass.getAnnotation(ServiceScan.class).value();
        if ("".equals(basePackage)) {
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
        }
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);
        for (Class<?> clazz :
                classSet) {
            if (clazz.isAnnotationPresent(Service.class)) {
                String serviceName = clazz.getAnnotation(Service.class).name();
                Object obj;
                try {
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("创建" + clazz + "时发生错误");
                    continue;
                }
                if ("".equals(serviceName)) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface :
                            interfaces) {
                        publishService(obj, oneInterface.getCanonicalName());//获取该类的规范名称
                    }
                } else {
                    publishService(obj, serviceName);
                }
            }
        }
    }


    /**
     * 将服务保存到服务端本地注册表，同时注册到Nacos注册中心
     *
     * @param service
     * @param serviceName
     * @param <T>
     */
    @Override
    public <T> void publishService(Object service, String serviceName) {
        serviceProvider.addServiceProvider(service, serviceName);
        serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
    }
}
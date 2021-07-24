package com.prac.rpc.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.prac.rpc.enumeration.RpcError;
import com.prac.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 管理Nacos连接等的工具类
 *
 * @author: Sapeurs
 * @date: 2021/7/20 10:45
 * @description:
 */
public class NacosUtil {


    private static final Logger logger = LoggerFactory.getLogger(NacosUtil.class);

    private static final NamingService nameService;
    //注册服务的集合
    private static final Set<String> serviceNames = new HashSet<>();
    private static InetSocketAddress inetSocketAddress;

    private static final String SERVER_ADDR = "127.0.0.1:8848";


    static {
        nameService = getNacosNameService();
    }


    /**
     * 向Nacos中注册一个服务
     *
     * @param serviceName 服务名
     * @param inetSocketAddress 服务所在地址
     */
    public static void registerService(String serviceName, InetSocketAddress inetSocketAddress) throws NacosException {
        nameService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        NacosUtil.inetSocketAddress = inetSocketAddress;
        //保存注册的用户名
        serviceNames.add(serviceName);
    }

    /**
     * 获得名字服务，即系统中所有对象、实体的“名字”到关联的数据之间的映射管理服务
     *
     * @return
     */
    public static NamingService getNacosNameService() {
        try {
            return NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            logger.error("连接到Nacos时发生错误：", e);
            throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }


    /**
     * 获取所有提供该服务的服务端地址
     *
     * @param serviceName
     * @return
     * @throws NacosException
     */
    public static List<Instance> getAllInstance(String serviceName) throws NacosException {
        return nameService.getAllInstances(serviceName);
    }


    /**
     * 向Nacos注销所有服务
     */
    public static void clearRegistry() {
        if (!serviceNames.isEmpty() && inetSocketAddress != null) {
            String host = inetSocketAddress.getHostName();
            int port = inetSocketAddress.getPort();
            //迭代所有服务名
            for (String serviceName : serviceNames) {
                try {
                    //注销
                    nameService.deregisterInstance(serviceName, host, port);
                } catch (NacosException e) {
                    logger.error("注销服务{}失败", serviceName, e);
                }
            }
        }
    }


}
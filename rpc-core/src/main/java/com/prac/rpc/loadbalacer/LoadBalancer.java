package com.prac.rpc.loadbalacer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 负载均衡策略算法接口
 *
 * @author: Sapeurs
 * @date: 2021/7/20 16:20
 * @description:
 */
public interface LoadBalancer {

    /**
     * 选择一个服务实例
     *
     * @param instances 服务实例
     * @return
     */
    Instance select(List<Instance> instances);

}

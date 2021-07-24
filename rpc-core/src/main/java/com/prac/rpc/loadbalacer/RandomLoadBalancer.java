package com.prac.rpc.loadbalacer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡算法
 *
 * @author: Sapeurs
 * @date: 2021/7/20 16:27
 * @description:
 */
public class RandomLoadBalancer implements LoadBalancer {
    @Override
    public Instance select(List<Instance> instances) {
        //从instances中随机选择一个
        return instances.get(new Random().nextInt(instances.size()));
    }
}
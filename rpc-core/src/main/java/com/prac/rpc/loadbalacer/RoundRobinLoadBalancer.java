package com.prac.rpc.loadbalacer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 转轮负载均衡算法
 *
 * @author: Administrator
 * @date: 2021/7/20 16:34
 * @description: 按照顺序依次选择第一个、第二个、第三个。。。
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    //表示挡圈宣导了第几个服务器
    private int index = 0;

    @Override
    public Instance select(List<Instance> instances) {
        if (index >= instances.size()) {
            index %= instances.size();
        }
        //每次选择后自增1
        return instances.get(index++);
    }
}
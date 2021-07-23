package com.prac.test.impl;

import com.prac.rpc.annotation.Service;
import com.rpc.api.HelloObject;
import com.rpc.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Administrator
 * @date: 2021/7/12 17:15
 * @description: 接口HelloService的实现类
 */
@Service
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(HelloObject object) {
        logger.info("接收到：{}", object.getMessage());
        return "这是调用的返回值，id = " + object.getId();
    }
}
package com.prac.test.impl;

import com.prac.rpc.annotation.Service;
import com.rpc.api.ByeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 接口ByeService的实现类
 *
 * @author: Administrator
 * @date: 2021/7/21 14:56
 * @description:
 */
@Service
public class ByeServiceImpl implements ByeService {

    private static final Logger logger = LoggerFactory.getLogger(ByeServiceImpl.class);

    @Override
    public String bye(String s) {
        return "Bye " + s;
    }
}
package com.prac.rpc.exception;

/**
 * 序列化异常类
 *
 * @author: Administrator
 * @date: 2021/7/15 15:37
 * @description:
 */
public class SerializerException extends RuntimeException {

    public SerializerException(String msg) {
        super(msg);
    }

}
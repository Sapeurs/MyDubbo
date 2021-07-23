package com.prac.rpc.exception;

import com.prac.rpc.enumeration.RpcError;

/**
 * @author: Administrator
 * @date: 2021/7/14 10:45
 * @description: 通用RPC异常类
 */
public class RpcException extends RuntimeException {

    public RpcException(RpcError error, String detail) {
        super(error.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcError error) {
        super(error.getMessage());
    }
}
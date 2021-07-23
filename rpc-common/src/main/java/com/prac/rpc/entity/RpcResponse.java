package com.prac.rpc.entity;

import com.prac.rpc.enumeration.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: Administrator
 * @date: 2021/7/13 10:03
 * @description: 服务器调用完方法后返回给客户端的信息，封装为RpcResponse对象
 * 类似于传输协议
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse<T> implements Serializable {

    /**
     * 响应对应的请求号
     */
    private String requestId;

    /**
     * 响应状态码
     */
    private Integer statusCode;

    /**
     * 响应状态补充信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应成功调用的方法
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setData(data);
        return response;
    }

    /**
     * 响应失败调用的方法
     *
     * @param code
     * @param <T>
     * @return
     */
    public static <T> RpcResponse<T> fail(ResponseCode code, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(code.getCode());
        response.setMessage(code.getMessage());
        return response;
    }
}
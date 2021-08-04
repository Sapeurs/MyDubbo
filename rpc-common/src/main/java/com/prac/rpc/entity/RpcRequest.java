package com.prac.rpc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: Sapeurs
 * @date: 2021/7/13 9:57
 * @description: 将接口名字、方法名字、方法的所有参数类型以及客户端调用时传递参数的实际值写到
 * RpcRequest对象中用于向服务端传输信息
 * 类似于传输协议
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {

    /**
     * 请求号
     */
    private String requestId;

    /**
     * 待调用的接口名称
     */
    private String interfaceName;

    /**
     * 待调用的方法名称
     */
    private String methodName;

    /**
     * 调用方法的参数
     */
    private Object[] parameters;

    /**
     * 调用方法的参数类型
     */
    private Class<?>[] paramTypes;

    /**
     * 是否是心跳包
     */
    private Boolean heartBeat;
}
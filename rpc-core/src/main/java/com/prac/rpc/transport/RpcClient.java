package com.prac.rpc.transport;

import com.prac.rpc.entity.RpcRequest;

/**
 * @author: Administrator
 * @date: 2021/7/13 11:12
 * @description: 将一个Request对象发送给客户端，并且接受返回的对象Response
 * <p>
 * 使用Java的序列化方式，通过Socket传输。创建一个Socket，
 * 获取ObjectOutputStream对象，然后把需要发送的对象传进去即可，接收时获取ObjectInputStream对象
 */
public interface RpcClient {

    Object sendRequest(RpcRequest rpcRequest);

}
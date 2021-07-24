package com.prac.rpc.transport.netty.client;

import com.prac.rpc.entity.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 未处理的请求，对所有Netty客户端请求进行统一管理
 *
 * @author: Sapeurs
 * @date: 2021/7/23 10:41
 * @description:
 */
public class UnprocessedRequests {

    private static Map<String, CompletableFuture<RpcResponse>> unprocessedRequests = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse> future) {
        unprocessedRequests.put(requestId, future);
    }

    public void remove(String requestId) {
        unprocessedRequests.remove(requestId);
    }

    public void complete(RpcResponse rpcResponse) {
        //请求完成了，把请求从未完成的请求中移除
        CompletableFuture<RpcResponse> future = unprocessedRequests.remove(rpcResponse.getRequestId());
        if (null != future) {
            //把响应对象放到future中
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }

}
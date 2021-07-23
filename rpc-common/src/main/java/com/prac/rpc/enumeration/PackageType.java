package com.prac.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: Administrator
 * @date: 2021/7/14 16:51
 * @description: 协议的第二个字段
 * 标识是调用请求还是调用响应
 */
@AllArgsConstructor
@Getter
public enum PackageType {

    REQUEST_PACK(0),
    RESPONSE_PACK(1);

    private final int code;

}

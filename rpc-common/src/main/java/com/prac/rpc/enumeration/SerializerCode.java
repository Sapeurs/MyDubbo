package com.prac.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: Sapeurs
 * @date: 2021/7/14 16:40
 * @description: 字节流中标注序列化和反序列化器
 */
@AllArgsConstructor
@Getter
public enum SerializerCode {

    KRYO(0),
    JSON(1),
    HESSIAN(2),
    PROTOBUF(3);

    private final int code;

}

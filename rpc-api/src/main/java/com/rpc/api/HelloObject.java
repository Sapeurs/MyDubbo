package com.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: Administrator
 * @date: 2021/7/12 17:11
 * @description: Service方法中需要传递的测试对象
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HelloObject implements Serializable {
    private Integer id;
    private String message;
}
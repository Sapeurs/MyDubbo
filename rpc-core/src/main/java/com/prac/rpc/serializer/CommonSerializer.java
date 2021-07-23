package com.prac.rpc.serializer;

/**
 * @author: Administrator
 * @date: 2021/7/14 16:03
 * @description: 序列化接口
 */
public interface CommonSerializer {

    Integer KRYO_SERIALIZER = 0;
    Integer JSON_SERIALIZER = 1;
    Integer HESSIAN_SERIALIZER = 2;
    Integer PROTOBUF_SERIALIZER = 3;


    Integer DEFAULT_SERIALIZER = KRYO_SERIALIZER;

    /**
     * 将对象序列化为byte数组
     *
     * @param obj
     * @return
     */
    byte[] serialize(Object obj);


    /**
     * 将byte数组反序列化为clazz类的对象
     *
     * @param bytes
     * @param clazz
     * @return
     */
    Object deserialize(byte[] bytes, Class<?> clazz);

    int getCode();

    static CommonSerializer getByCode(int code) {
        switch (code) {
            case 0:
                return new KryoSerializer();
            case 1:
                return new JsonSerializer();
            case 2:
                return new HessianSerializer();
            case 3:
                return new ProtostuffSerializer();
            default:
                return null;
        }
    }

}

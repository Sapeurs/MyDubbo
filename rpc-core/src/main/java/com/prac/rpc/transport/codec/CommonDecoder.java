package com.prac.rpc.transport.codec;

import com.prac.rpc.entity.RpcRequest;
import com.prac.rpc.entity.RpcResponse;
import com.prac.rpc.enumeration.PackageType;
import com.prac.rpc.enumeration.RpcError;
import com.prac.rpc.exception.RpcException;
import com.prac.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author: Administrator
 * @date: 2021/7/14 17:05
 * @description: 自定义协议解码器
 */
public class CommonDecoder extends ReplayingDecoder {

    private static final Logger logger = LoggerFactory.getLogger(CommonDecoder.class);
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magic = in.readInt();
        //判断协议包的正确性
        if (magic != MAGIC_NUMBER) {
            logger.error("不识别的协议包：{}", magic);
            throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
        }
        //判断是调用请求还是调用响应
        int packageCode = in.readInt();
        Class<?> packageClass;
        if (packageCode == PackageType.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageType.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            logger.error("不识别的数据包：{}", packageCode);
            throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
        }
        //判断序列化器类型
        int serializerCode = in.readInt();
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if (serializer == null) {
            logger.error("不识别的反序列化器：{}", serializerCode);
            throw new RpcException(RpcError.UNKNOWN_SERIALIZER);
        }

        //将收到的字节序列还原为实际对象
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        Object obj = serializer.deserialize(bytes, packageClass);
        out.add(obj);

    }
}
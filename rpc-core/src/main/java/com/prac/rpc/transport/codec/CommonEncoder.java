package com.prac.rpc.transport.codec;

import com.prac.rpc.entity.RpcRequest;
import com.prac.rpc.enumeration.PackageType;
import com.prac.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 自定义协议编码器
 * @author: Sapeurs
 * @date: 2021/7/14 15:57
 * @description:
 * <p>
 * 自定义的协议
 * +---------------+---------------+-----------------+-------------+
 * |  Magic Number |  Package Type | Serializer Type | Data Length |
 * |    4 bytes    |    4 bytes    |     4 bytes     |   4 bytes   |
 * +---------------+---------------+-----------------+-------------+
 * |                          Data Bytes                           |
 * |                   Length: ${Data Length}                      |
 * +---------------------------------------------------------------+
 */
public class CommonEncoder extends MessageToByteEncoder {

    //4字节的魔数，标识一个协议包
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    private final CommonSerializer commonSerializer;

    public CommonEncoder(CommonSerializer commonSerializer) {
        this.commonSerializer = commonSerializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        //写入魔数
        out.writeInt(MAGIC_NUMBER);
        //写入协议中标识类型字段
        if (msg instanceof RpcRequest) {
            out.writeInt(PackageType.REQUEST_PACK.getCode());
        } else {
            out.writeInt(PackageType.RESPONSE_PACK.getCode());
        }
        //写入协议中数据使用的序列化器类型
        out.writeInt(commonSerializer.getCode());
        //写入数据长度字段
        byte[] bytes = commonSerializer.serialize(msg);
        out.writeInt(bytes.length);
        //写入Data Bytes：经过序列化后的实际数据
        out.writeBytes(bytes);
    }
}
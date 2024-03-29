package com.prac.rpc.transport.socket.util;

import com.prac.rpc.entity.RpcRequest;
import com.prac.rpc.enumeration.PackageType;
import com.prac.rpc.serializer.CommonSerializer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Socket方式将数据序列化并写入输出流中【编码】
 *
 * @author: Sapeurs
 * @date: 2021/7/22 15:38
 * @description:
 */
public class ObjectWriter {

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    public static void writeObject(OutputStream out, Object object, CommonSerializer serializer) throws IOException {
        out.write(intToBytes(MAGIC_NUMBER));
        if (object instanceof RpcRequest) {
            out.write(intToBytes(PackageType.REQUEST_PACK.getCode()));
        } else {
            out.write(intToBytes(PackageType.RESPONSE_PACK.getCode()));
        }
        out.write(intToBytes(serializer.getCode()));
        byte[] bytes = serializer.serialize(object);
        out.write(intToBytes(bytes.length));
        out.write(bytes);
        out.flush();
    }


    /**
     * 将Int转换为字节数组
     *
     * @param value
     * @return
     */
    private static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

}
package coderead.netty.rpc;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import coderead.netty.ByteBufUtil;
import com.alibaba.dubbo.common.io.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.io.*;
import java.util.List;

/**
 * @author 鲁班大叔
 * @date 2020/7/305:26 PM
 */
public class RpcCodec extends ByteToMessageCodec {
    protected static final int HEADER_LENGTH = 16;
    protected static final short MAGIC = 0xdad;
    protected static final ByteBuf MAGIC_BUF = Unpooled.copyShort(MAGIC);
    protected static final byte FLAG_REQUEST = (byte) 0x80;//1000 0000
    protected static final byte FLAG_TWO_WAY = (byte) 0x40; //0100 0000
    protected static final byte FLAG_EVENT = (byte) 0x20;  //0010 0000
    protected static final int SERIALIZATION_MASK = 0x1f;  //0001 1111

    // 编码
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg instanceof Transfer) {
            doEncode((Transfer) msg, out);
        } else {
            throw new IllegalArgumentException();
        }
    }

    //解码
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) throws Exception {
        Transfer transfer = doDecode(in);
        if (transfer != null) {
            out.add(transfer);
        }
    }

    // 编码
    protected void doEncode(Transfer data, ByteBuf buf) {
        byte[] header = new byte[HEADER_LENGTH];
        Bytes.short2bytes(MAGIC, header);

        header[2] = data.serializableId;
        if (data.request) header[2] |= FLAG_REQUEST;
        if (data.twoWay) header[2] |= FLAG_TWO_WAY;
        if (data.heartbeat) header[2] |= FLAG_EVENT;
        if (!data.request) header[3] = data.status;

        Bytes.long2bytes(data.id, header, 4);// id 占8个字节
        int len = 0;
        byte[] body = new byte[0];
        if (!data.heartbeat) {
            body = serialize(data.serializableId, data.target);
            len = body.length;
        }
        Bytes.int2bytes(len, header, 12);
        buf.writeBytes(header);
        buf.writeBytes(body);
    }

    // 解码
    protected Transfer doDecode(ByteBuf in) {
        int index = ByteBufUtil.indexOf(in, MAGIC_BUF);
        if (index < 0) {
            return null; //需要更多的字节
        }
        if (!in.isReadable(index + HEADER_LENGTH)) {
            return null;//需要更多的字节
        }
        byte[] header = new byte[HEADER_LENGTH];
//      in.getBytes(index, header);
        ByteBuf slice = in.slice();
        slice.readBytes(header);
        int length = Bytes.bytes2int(header, 12);

        if (!in.isReadable(index + HEADER_LENGTH + length)) {
            return null;//需要更多的字节
        }
        Transfer transfer = new Transfer(Bytes.bytes2long(header, 4));
        transfer.heartbeat = (header[2] & FLAG_EVENT) != 0;
        transfer.request = (header[2] & FLAG_REQUEST) != 0;
        transfer.twoWay = (header[2] & FLAG_TWO_WAY) != 0;
        transfer.serializableId = (byte) (header[2] & SERIALIZATION_MASK);
        transfer.status = header[3];
        if (!transfer.heartbeat) {
            byte content[] = new byte[length];
//          in.getBytes(index + HEADER_LENGTH, bytes);
            slice.readBytes(content);
            transfer.target= deserialize(transfer.serializableId, content);
        }
        in.skipBytes(index + HEADER_LENGTH + length);

        return transfer;
    }

    // 序列化
    private byte[] serialize(byte serializableId, Object target) {

        if (serializableId == Transfer.SERIALIZABLE_JAVA) { //JAVA
            ByteArrayOutputStream out = null;
            try {
                out = new ByteArrayOutputStream();
                ObjectOutputStream stream = new ObjectOutputStream(out);
                stream.writeObject(target);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return out.toByteArray();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    // 反序列化
    private Object deserialize(byte serializableId, byte[] bytes) {
        if (serializableId == Transfer.SERIALIZABLE_JAVA) { //JAVA
            try {
                ObjectInputStream stream =
                        new ObjectInputStream(new ByteArrayInputStream(bytes));
                return stream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }
}

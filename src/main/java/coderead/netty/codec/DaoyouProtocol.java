package coderead.netty.codec;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import coderead.netty.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author 鲁班大叔
 * @date 2020/7/2611:40 AM
 */
public class DaoyouProtocol extends ByteToMessageCodec<String> {
    static int MAGIC=0xDADA;// 标识码
    static ByteBuf MAGIC_BUF= Unpooled.copyInt(MAGIC);
    //编码
    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        byte[] bytes = msg.getBytes();
        out.writeInt(MAGIC);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
    //解码
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int index = ByteBufUtil.indexOf(in, MAGIC_BUF);
        if(index<0){
           return; //需要更多的字节
        }
        if (!in.isReadable(index + 8)) {
            return;//需要更多的字节
        }
        int length = in.slice(index + 4, 4).readInt();

        if (!in.isReadable(index + 8+length)) {
            return;//需要更多的字节
        }

        in.skipBytes(index+8);
        ByteBuf buf = in.readRetainedSlice(length);
        String message = buf.toString(Charset.defaultCharset());
        out.add(message);
    }
}















package coderead.netty.codec;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

/**
 * @author 鲁班大叔
 * @date 2020/7/2511:29 PM
 */
public class ByteBufTest {
    // 声明
    // 读写
    // 回收已读空间
    @Test
    public void RwTest() {
        // 声明缓冲区
        ByteBuf buffer = Unpooled.buffer(5, 100);
        buffer=Unpooled.wrappedBuffer(new byte[]{1,2,3,4,5});
        buffer.writeByte((byte)1);// 读:0 写:1
        buffer.writeByte((byte)2);// 读:0 写:2
        buffer.writeByte((byte)3);// 读:0 写:2
        buffer.writeByte((byte)4);// 读:0 写:2
        buffer.writeByte((byte)5);// 读:0 写:2
        buffer.readByte();// 读:1 写:2
        buffer.readByte();// 读:2 写:2
        buffer.discardReadBytes(); // 自动回收已读取空间
        buffer.readByte();// 数组越界异常 IndexOutOfBoundsException
    }

    // 复制视图
    @Test
    public void copyTest() {
        ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4, 5});
        ByteBuf duplicate = buffer.duplicate();// 复制出一个视图 ， 独立的读写索引
        buffer.readByte();
        ByteBuf slice = buffer.slice();// 复制全部可读视图区域
        ByteBuf slice2 = buffer.readSlice(4);// 读索引加4
        ByteBuf copy = buffer.copy();// 完全复制一个新的缓冲区
    }

    @Test
    public void releaseTest() {
        ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4, 5});
        ByteBuf buffer2 = buffer.retainedSlice(1, 2);
        buffer.writeInt(1);// 4
        buffer.readInt();
        buffer.isReadable();
        buffer.readerIndex();   // 返回可读空间数
        buffer.isWritable();
        buffer.isWritable(4);
        buffer.release();// 引用数减1
        buffer.readByte();
        buffer.clear(); // 读：0 写：0
    }

}

package coderead.nio;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import java.nio.ByteBuffer;

/**
 * @author 鲁班大叔
 * @date 2020/7/1111:03 PM
 */
public class BufferUtil {

    public static String getMessage(ByteBuffer buffer) {
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes);
    }
}

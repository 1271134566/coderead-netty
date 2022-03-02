package coderead.netty.protobuf;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Test;

/**
 * @author 鲁班大叔
 * @date 2020/8/15 10:25 AM
 */
public class CodecTest {
    @Test
    public void test() throws InvalidProtocolBufferException {
        UserMessage.User.Builder builder = UserMessage.User.newBuilder();
        UserMessage.User user = builder.setId(99).build();
        // 序列化
        byte[] bytes = user.toByteArray();
        // 反序列化
        UserMessage.User user1 = UserMessage.User.newBuilder().mergeFrom(bytes).build();
        System.out.println(user1);
    }

}

package coderead.netty.rpc;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

/**
 * @author 鲁班大叔
 * @date 2020/7/309:38 PM
 */
public class RpcCodecTest {

    @Test
    public void invokerTest() {
        ByteBuf out = Unpooled.buffer(1024, 1024 * 1024 * 10);// 最大10M
        encodeRequest(out);// 1.编码 请求
        Transfer from = decodeRequest(out);// 2.解码 请求
        out.clear();
        encodeResponse(from, out); // 3.编码响应
        Transfer to = decodeResponse(out);// 4.解码响应
        assert from.id == to.id;
    }

    // 写入请求对象
    Request encodeRequest(ByteBuf out) {
        RpcCodec codec = new RpcCodec();
        Transfer transfer=new Transfer(RpcClient.getNextId());
        transfer.request=true;
        transfer.serializableId=Transfer.SERIALIZABLE_JAVA;
        Request request = new Request( "UserService", "getUser");
        request.setArgs(new Object[]{1, "name"});
        codec.doEncode(transfer, out); // 编码请求
        return request;
    }

    Transfer decodeRequest(ByteBuf out) {
        RpcCodec codec = new RpcCodec();
        return codec.doDecode(out);
    }

    Response encodeResponse(Transfer transfer, ByteBuf out) {
        Transfer to=new Transfer(transfer.id);
        to.request=false;
        to.serializableId=Transfer.SERIALIZABLE_JAVA;
        RpcCodec codec = new RpcCodec();
        Response response = new Response();
        response.setResult("返回结果");
        codec.doEncode(to, out);
        return response;
    }

    Transfer decodeResponse(ByteBuf out) {
        RpcCodec codec = new RpcCodec();
        return codec.doDecode(out);
    }


}

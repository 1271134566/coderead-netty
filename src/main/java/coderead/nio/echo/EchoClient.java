package coderead.nio.echo;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author 鲁班大叔
 * @date 2020/7/1810:32 AM
 */
public class EchoClient {

    @Test
    public void test() throws IOException, InterruptedException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT);
        // 异步操作
        boolean connect = channel.connect(new InetSocketAddress("127.0.0.1", 8080));
        while (true) {
            selector.select(100);
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isConnectable()) {
                    System.out.println("是否连接："+channel.isConnected());
                    channel.finishConnect();
                    //写数据
                    key.interestOps(SelectionKey.OP_WRITE);
                }else if(key.isWritable()){
                    // 心跳写入
                    channel.write(ByteBuffer.wrap("heartbeat".getBytes()));
                    key.interestOps(SelectionKey.OP_READ);
                }else if (key.isReadable()) {
                    ByteBuffer buffer = ByteBuffer.allocate(64);
                    channel.read(buffer);
                    buffer.flip();
                    System.out.println(new String(buffer.array(), 0, buffer.limit()));
                    key.interestOps(SelectionKey.OP_WRITE);
                    Thread.sleep(2000);
                }

            }
        }


    }
}

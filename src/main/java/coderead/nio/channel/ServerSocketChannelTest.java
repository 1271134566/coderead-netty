package coderead.nio.channel;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import com.sun.xml.internal.ws.util.ByteArrayBuffer;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author 鲁班大叔
 * @date 2020/7/1112:15 PM
 */
public class ServerSocketChannelTest {
    @Test
    public void test1() throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.bind(new InetSocketAddress(8080));
        // 1.建立连接
        // 2.通信
        while (true) {
            final SocketChannel socketChannel = channel.accept();
            handle(socketChannel);
        }
    }


    private void handle(final SocketChannel socketChannel) throws IOException {
        // 2.通信
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        while (true) {
            buffer.clear();
            socketChannel.read(buffer);
            // 从buffer 当中读出来
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String message = new String(bytes);
            System.out.println(message);
            // 写回去
            buffer.rewind();
            socketChannel.write(buffer);
            if (message.trim().equals("exit")) {
                break;
            }
        }
        socketChannel.close();
    }
}

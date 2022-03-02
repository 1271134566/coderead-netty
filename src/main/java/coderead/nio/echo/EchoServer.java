package coderead.nio.echo;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * 实现心路服务
 *
 * @author 鲁班大叔
 * @date 2020/7/129:43 AM
 */
public class EchoServer {
    // 需求：心跳服务，按服务
    @Test
    public void serverTest() throws IOException {
        ServerSocketChannel serverListener=ServerSocketChannel.open();
        serverListener.bind(new InetSocketAddress(8080));
        serverListener.configureBlocking(false);
        Selector selector = Selector.open();
        serverListener.register(selector,SelectionKey.OP_ACCEPT);
        try {
            dispatch(selector,serverListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }finally {
            System.out.println("关闭");
        }

    }

    private void dispatch(Selector selector , ServerSocketChannel serverListener) throws IOException {
        while (true) {
            int count = selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (!key.isValid()) {
                    continue;
                }else if(key.isAcceptable()){
                    SocketChannel socketChannel = serverListener.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }else if(key.isReadable()){
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer=ByteBuffer.allocate(64);
                    channel.read(buffer);
                    if (buffer.hasRemaining()&&buffer.get(0)==4) {// 传输结束
                        channel.close();
                        System.out.println("关闭管道："+channel);
                        break;
                    }
                    buffer.put(String.valueOf(System.currentTimeMillis()).getBytes());
                    buffer.flip();
                    channel.write(buffer);
                }
            }
        }


    }


}























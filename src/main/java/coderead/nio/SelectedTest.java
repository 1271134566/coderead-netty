package coderead.nio;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author 鲁班大叔
 * @date 2020/7/69:39 AM
 */
public class SelectedTest {

    // UDP 简单测试Selector 事件
    @Test
    public void udpTest1() throws IOException {
        DatagramChannel channel = DatagramChannel.open().bind(new InetSocketAddress(8080));
        channel.configureBlocking(false);

        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);

        while (true) {
            int select = selector.select(100);
            if (select > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isValid()) {
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        channel.receive(buffer);
                        buffer.flip();
                        byte[] dst = new byte[buffer.remaining()];
                        buffer.get(dst);
                        System.out.println(new String(dst));
                    }
                    iterator.remove();
                }
            }
        }
    }


    // TCP 事件
    @Test
    public void tcpSocketTest() throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.bind(new InetSocketAddress(8080));
        Selector selector = Selector.open();
        new Thread(() -> {
            try {
                dispatch(selector);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        },"selected-io").start();

        while (true) {
            SocketChannel socket = channel.accept();
            socket.configureBlocking(false);
            socket.register(selector,SelectionKey.OP_READ);
            System.out.println("已注册"+socket);
        }
    }
    private void dispatch( Selector selector) throws IOException, InterruptedException {
        while (true) {
            int count = selector.select(500);
            Thread.sleep(5);// 防止死锁 导致注册不上
            if (count==0) {
                continue;
            }
            //客户端断开 事件处理
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (!key.isValid()) {
                    continue;
                } else if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer dst=ByteBuffer.allocate(1024);
                    channel.read(dst);
                    System.out.println(BufferUtil.getMessage(dst));
                }
            }
        }
    }



    @Test
    public void wakeupTest() throws IOException, InterruptedException {
        Selector selector = Selector.open();
        /*selector.wakeup();
        selector.select();*/
        Thread thread = new Thread(() -> {
            try {
                int count = selector.select();
                System.out.println(count);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "selector-io");
        thread.start();
        new Thread(() -> selector.wakeup()).start();
        thread.join();
    }

}

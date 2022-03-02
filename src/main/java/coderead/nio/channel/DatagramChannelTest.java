package coderead.nio.channel;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */


import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * UDP 管道通信
 *
 * @author 鲁班大叔
 * @date 2020/7/1112:02 PM
 */
public class DatagramChannelTest {
    //nc -vuz 127.0.0.1 8080 测试UDP连接
    //nc -vu 127.0.0.1 8080 向udp 发送消息
    @Test
    public void test1() throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(8080));// 绑定端口
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        channel.receive(buffer); // 阻塞
        while (true) {
            buffer.clear(); //  清空还原
            SocketAddress client = channel.receive(buffer);// 阻塞
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            System.out.println(new String(bytes));
            buffer.rewind();
            channel.send(buffer,client); //回写消息
        }
    }
    @Test
    public void test2() throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        // 绑定端口
        channel.bind(new InetSocketAddress(8080));
        channel.configureBlocking(true);
        channel.receive(ByteBuffer.allocate(1024));
    }


    //通过选择器 实现UDP
    @Test
    public void selectedTest() throws IOException {
        // 1.打开选择器
        // 2.打开管道
        // 3.读取消息
        Selector selector = Selector.open();
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(8080));
        channel.configureBlocking(false);// 非阻塞
        channel.register(selector, SelectionKey.OP_READ);
        while (true) {
            int count = selector.select();// 遍历管道状态
            if (count > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    handle(key);
                    iterator.remove();
                }
            }
        }
    }

    public void handle(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        DatagramChannel channel = (DatagramChannel) key.channel();
        buffer.clear(); //  清空还原
        channel.receive(buffer); // 阻塞
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        System.out.println(new String(bytes));
    }

    @Test
    public void test4() throws IOException {
        Selector selector = Selector.open();
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(8080));
        channel.configureBlocking(false);// 非阻塞
        channel.register(selector, SelectionKey.OP_READ);
        channel.close();

    }

    @Test
    public void test5() throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.send(ByteBuffer.wrap("hello".getBytes()),new InetSocketAddress("127.0.0.1",8081));
    }
}



















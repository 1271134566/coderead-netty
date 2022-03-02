package coderead.netty.channel;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.AbstractNioChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * @author 鲁班大叔
 * @date 2020/7/2511:05 AM
 */
public class UnsafeTest {
    EventLoopGroup loopGroup=new NioEventLoopGroup(1);

    // 直接调用 unsafe 注册
    @Test
    public void registerTest() throws IOException {
        NioDatagramChannel channel=new NioDatagramChannel();
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
            @Override
            public void channelRegistered(ChannelHandlerContext ctx) {
                System.out.println("注册成功");
            }
        });
        AbstractNioChannel.NioUnsafe unsafe = channel.unsafe();
        unsafe.register(loopGroup.next(),channel.newPromise());
        System.in.read();
    }
    // bind
    @Test
    public void bindTest() throws IOException, InterruptedException {
        NioDatagramChannel channel=new NioDatagramChannel();
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
            @Override
            public void channelRegistered(ChannelHandlerContext ctx) {
                System.out.println("注册成功");
            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                System.out.println("绑定端口成功");
            }
        });
        channel.unsafe().register(loopGroup.next(),channel.newPromise());
        Thread.sleep(100);
        // 交给IO线程
        channel.eventLoop().submit(() -> channel.unsafe().bind(new InetSocketAddress(8080),channel.newPromise()));
        channel.bind(new InetSocketAddress(8080));
        System.in.read();
    }

    @Test
    public void writeTest() throws IOException {
        NioDatagramChannel channel=new NioDatagramChannel();
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                cause.printStackTrace();
            }
        });
        loopGroup.register(channel);
        // 必须激活
        channel.bind(new InetSocketAddress(8081));

        loopGroup.submit(() -> {
            // 写入消息到 unsafe 缓冲区
            channel   //不走pipeline
                .write("123"
                        , channel.newPromise());

            // 刷新消息到 java channel
            channel.unsafe().flush();
        });
        System.in.read();
    }

    @Test
    public void openUdpServer() throws IOException {
        NioDatagramChannel channel=new NioDatagramChannel();
        channel.pipeline().addLast(new SimpleChannelInboundHandler<Object>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println(msg);
                DatagramPacket p= (DatagramPacket) msg;
                System.out.println(p.content().toString(Charset.defaultCharset()));
            }
        });
        loopGroup.register(channel);
        channel.bind(new InetSocketAddress(8080));
        System.in.read();
    }
}















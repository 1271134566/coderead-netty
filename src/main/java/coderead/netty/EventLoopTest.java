package coderead.netty;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * @author 鲁班大叔
 * @date 2020/7/1910:49 AM
 */
public class EventLoopTest {

    @Test
    public void test() throws IOException {
        // 1.构建EventLoop
        NioEventLoopGroup group=new NioEventLoopGroup(1);
        group.execute(() -> System.out.println("hello event loop："+Thread.currentThread().getId()));// 立即执行
        group.submit(() -> System.out.println("submit:"+Thread.currentThread().getId()));
        System.in.read();
        group.shutdownGracefully();// 安全关闭
    }

    @Test
    public void test2() throws IOException {
        NioEventLoopGroup group=new NioEventLoopGroup(1);
        NioDatagramChannel channel=new NioDatagramChannel();
        ChannelFuture future = channel.bind(new InetSocketAddress(8080)); //异步操和
        group.register(channel);
        future.addListener(future1 -> System.out.println("完成绑定"));
        channel.pipeline().addLast(new SimpleChannelInboundHandler() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof DatagramPacket) {
                    DatagramPacket packet= (DatagramPacket) msg;
                    System.out.println(packet.content().toString(Charset.defaultCharset()));
                }
            }
        });
        System.in.read();
    }

    @Test
    public void test3() {
        // 并行执行
        UnorderedThreadPoolEventExecutor eventExecutors = new UnorderedThreadPoolEventExecutor(1);
        NioEventLoopGroup group = new NioEventLoopGroup(1, eventExecutors);
    }
}

package coderead.netty.codec;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author 鲁班大叔
 * @date 2020/7/259:50 PM
 */
public class DaoyouServer {
    private ServerBootstrap bootstrap;

    @Before
    public void init() {
        bootstrap = new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup(8));
        bootstrap.channel(NioServerSocketChannel.class);

    }

    @After
    public void start() throws InterruptedException {
        ChannelFuture future = bootstrap.bind(8080);
        System.out.println("启动成功");
        future.sync().channel().closeFuture().sync();
    }

    @Test
    public void test() {
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast(new DaoyouProtocol());
                ch.pipeline().addLast(new TrackHandler());
            }
        }) ;
    }
    private static class TrackHandler extends SimpleChannelInboundHandler<String> {
        int i = 0;
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            System.out.println(String.format("消息%s:%s", i++, msg));
            ctx.writeAndFlush("返回消息");
        }
    }
}

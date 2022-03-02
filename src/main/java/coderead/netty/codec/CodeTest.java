package coderead.netty.codec;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author 鲁班大叔
 * @date 2020/7/2610:53 AM
 */
public class CodeTest {
    private ServerBootstrap bootstrap;

    @Before
    public void init() {
        bootstrap = new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup(8));
        bootstrap.channel(NioServerSocketChannel.class);

    }


    @Test
    public void start() throws InterruptedException {
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                //固定大小
//                ch.pipeline().addLast(new FixedLengthFrameDecoder(5));
                // 换行
//                ch.pipeline().addLast(new LineBasedFrameDecoder(10));
                ByteBuf buf= Unpooled.wrappedBuffer(new byte[]{'$'});
//                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,true,buf));
                ch.pipeline().addLast(new TrackHandler());
                //2
            }
        });
        ChannelFuture future = bootstrap.bind(8080).sync();
        future.channel().closeFuture().sync();
    }

    public class TrackHandler extends SimpleChannelInboundHandler {
        int count = 0;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            String message = buf.toString(Charset.defaultCharset());
            System.out.println(String.format("消息%s：%s", ++count, message));
        }
    }
}

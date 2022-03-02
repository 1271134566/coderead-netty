package coderead.netty.pipeline;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author 鲁班大叔
 * @date 2020/7/2512:06 PM
 */
public class PipelineTest {
    @Test
    public void test() throws InterruptedException, IOException {
        NioDatagramChannel channel=new NioDatagramChannel();
        new NioEventLoopGroup().register(channel);
        Thread.sleep(100);
        channel.bind(new InetSocketAddress(8081));
        ChannelPipeline pipeline = channel.pipeline();
        // 入站事件
        pipeline.addLast(new ChannelInboundHandlerAdapter(){
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                String message = (String) msg;
                System.out.println("入站事件1："+msg);
                message+=" 已处理";
                ctx.fireChannelRead(message);
            }
        }); //入站处理
        pipeline.addLast(new ChannelOutboundHandlerAdapter(){
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                System.out.println(msg);
            }
        });
        pipeline.addFirst(new ChannelOutboundHandlerAdapter(){
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                DatagramPacket packet = new DatagramPacket(Unpooled.
                        wrappedBuffer(msg.toString().getBytes()),
                        new InetSocketAddress("127.0.0.1", 8080));
                ctx.write(packet);
            }
        });
        pipeline.addLast(new ChannelInboundHandlerAdapter(){
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println("入站事件2："+msg);
                ctx.fireChannelRead(108);
                ctx.writeAndFlush(msg);
            }
        });

      pipeline.fireChannelRead("hello luban");
//        pipeline.write("123");

//      pipeline.flush();
        System.in.read();
    }
}

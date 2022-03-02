package coderead.netty.channel;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * @author 鲁班大叔
 * @date 2020/7/165:07 PM
 */
public class ChannelTest {

    @Test
    public void udpTest1() throws InterruptedException, IOException {
        NioDatagramChannel datagram = new NioDatagramChannel();
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        // 注册到线程组
        boss.register(datagram);
        // 添加管道流处理器
        datagram.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                System.out.println(msg);
                DatagramPacket packet = (DatagramPacket) msg;
                ByteBuf hello_word = Unpooled.copiedBuffer("hello word", CharsetUtil.UTF_8);
                ctx.writeAndFlush(new DatagramPacket(hello_word, packet.sender()));// 返回客户端
            }
        });
        // 绑定端口，绑定后即启动服务
        datagram.bind(new InetSocketAddress(8080));
        System.in.read();
    }

    /**
     *  channel
     *  pipeline
     *  ChannelHandler
     *  ChannelHandlerContext
     *  nioEventLoop
     * @throws InterruptedException
     */
    // TCP 管道
    @Test
    public void tcpChannelTest() throws InterruptedException {
        NioServerSocketChannel channel=new NioServerSocketChannel();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        bossGroup.register(channel);
        channel.bind(new InetSocketAddress(8080));
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                active((NioSocketChannel) msg,workGroup);
            }
        });
        channel.closeFuture().sync();
    }
    // TCP接受连接
    public void active(NioSocketChannel socketChannel,NioEventLoopGroup group) {
        group.register(socketChannel);
        socketChannel.pipeline().addLast(new SimpleChannelInboundHandler(){
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println(msg.toString());
                ByteBuf byteBuf= (ByteBuf) msg;
                String s = byteBuf.toString(Charset.defaultCharset());
                ByteBuf response = Unpooled.copiedBuffer(s, Charset.defaultCharset());
                socketChannel.writeAndFlush(response);
            }
        });
        socketChannel.writeAndFlush(Unpooled.copiedBuffer("welcome netty server\r\n", Charset.defaultCharset()));
    }




}

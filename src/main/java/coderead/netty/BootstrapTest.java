package coderead.netty;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author 鲁班大叔
 * @date 2020/7/1911:52 AM
 */
public class BootstrapTest {

    // 编写一个Http 服务
    // http-->TCP
    public void open(int port) {
        //
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup(1);//
        EventLoopGroup work = new NioEventLoopGroup(8);//

        bootstrap.group(boss, work)
                .channel(NioServerSocketChannel.class)// 指定要打开的管道 自动进行进行注册==》NioServerSocketChannel ->

                .childHandler(new ChannelInitializer<Channel>() {//指定 子管道
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast("decode", new HttpRequestDecoder()); // 输入
                      //  ch.pipeline().addLast("aggregator",new HttpObjectAggregator(65536));
                        ch.pipeline().addLast("encode", new HttpResponseEncoder());// 输出流
                        ch.pipeline().addLast("servlet", new MyServlet());

                    }
                });
        // NioServerSocketChannel.bind==》EventLoop.runTasks ==>ServerSocketChannel.bind()
        ChannelFuture future = bootstrap.bind(port);
        future.addListener(future1 -> System.out.println("注册成功"));

    }
    // 请求头
    //  453 8192 8192 .... 2532

    private class MyServlet extends SimpleChannelInboundHandler {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            // request (请求头)
            // body     （请求体）
            if (msg instanceof FullHttpRequest) {
                FullHttpRequest request= (FullHttpRequest) msg;
                System.out.println("url"+request.uri());
                System.out.println(request.content().toString(Charset.defaultCharset()));

                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
                response.content().writeBytes("hello".getBytes());
                ChannelFuture future = ctx.writeAndFlush(response);
                future.addListener(ChannelFutureListener.CLOSE);
            }
           if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                System.out.println("当前请求：" + request.uri());
            }
            if (msg instanceof HttpContent) {
                // 写入文件流
                ByteBuf content = ((HttpContent) msg).content();
                OutputStream out = new FileOutputStream("/Users/tommy/temp/coderead-netty/target/test.txt", true);
                content.readBytes(out, content.readableBytes());
                out.close();
            }
            if (msg instanceof LastHttpContent) {
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
                response.content().writeBytes("上传完毕".getBytes());
                ChannelFuture future = ctx.writeAndFlush(response);
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new BootstrapTest().open(8080);
        System.in.read();
    }
}

package coderead.netty.webSocket;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * @author 鲁班大叔
 * @date 2020/8/111:49 AM
 */
public class DanmuServer {
        ChannelGroup channels;

    private ByteBuf indexPage;
    {
        try {
            initStaticPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void openServer(int port) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        ChannelFuture sync = bootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup(8))

                .channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast("decode", new HttpRequestDecoder()); // 输入
                        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
                        ch.pipeline().addLast("encode", new HttpResponseEncoder());// 输出流
                        ch.pipeline().addLast("http-servlet", new MyHttpServlet());//
                        ch.pipeline().addLast("ws-codec", new WebSocketServerProtocolHandler("/ws"));
                        ch.pipeline().addLast("ws-servlet", new MyWsServlet());

                    }
                }).bind(port).sync();
        channels=new DefaultChannelGroup(sync.channel().eventLoop());
        System.out.println("服务开启成功");
    }

    private void initStaticPage() throws Exception {
        URL location = DanmuServer.class.getProtectionDomain().getCodeSource().getLocation();
        String path = location.toURI() + "WebsocketDanMu.html";
        path = !path.contains("file:") ? path : path.substring(5);

        RandomAccessFile file = new RandomAccessFile(path, "r");//4
        ByteBuffer dst = ByteBuffer.allocate((int) file.length());
        file.getChannel().read(dst);
        dst.flip();
        indexPage = Unpooled.wrappedBuffer(dst);
        file.close();
    }

    private class MyHttpServlet extends SimpleChannelInboundHandler<FullHttpRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
            if (request.uri().equals("/")) {
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, indexPage.capacity());
                response.content().writeBytes(indexPage.duplicate());
                ctx.writeAndFlush(response);
            } else if (request.uri().equals("/ws")) {
                ctx.fireChannelRead(request.retain());// 转到webSocket 协议进行处理
            }
        }
    }

    private class MyWsServlet extends  SimpleChannelInboundHandler<TextWebSocketFrame> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
            // 接收客户端发送的消息
            System.out.println(msg.text());
            channels.writeAndFlush(new TextWebSocketFrame(msg.text()));
            if (msg.text().equals("add")) {
                channels.add(ctx.channel());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        DanmuServer server=new DanmuServer();
        server.openServer(8080);
        System.in.read();
    }
}

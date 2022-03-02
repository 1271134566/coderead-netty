package coderead.netty.redis;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.redis.*;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 鲁班大叔
 * @date 2020/8/110:53 AM
 */
public class Client {

    private Channel channel;

    /**
     * 1.打开管道
     * 1.1.初始化管道
     * 2.建立连接
     * <p>
     * 3.接收命令
     * 4.发送命令
     */

    public void openConnection(String host, int port) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(1))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast("decoder", new RedisDecoder());
                        ch.pipeline().addLast("bulk-aggregator",new RedisBulkStringAggregator());
                        ch.pipeline().addLast("array-aggregator",new RedisArrayAggregator());
                        ch.pipeline().addLast("encoder", new RedisEncoder());
                        ch.pipeline().addLast("dispatch", new MyRedisHandler());
                    }
                });
        channel = bootstrap.connect(host, port).sync().channel();
        System.out.println("连接成功");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client();
        client.openConnection("127.0.0.1",6379);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String s = in.readLine();
            System.out.print(">");
            client.channel.writeAndFlush(s);
        }
    }

    // 同时处理 写入 和读取
    private class MyRedisHandler extends ChannelDuplexHandler {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (!(msg instanceof String)) {
                ctx.write(msg);
                return;
            }
            // 封装消息
            String cmd = (String) msg;
            String[] commands = ((String) msg).split("\\s+");
            List<RedisMessage> children = new ArrayList<>(commands.length);
            for (String cmdString : commands) {
                children.add(new FullBulkStringRedisMessage(Unpooled.wrappedBuffer(cmdString.getBytes())));
            }
            RedisMessage request = new ArrayRedisMessage(children);
            ctx.write(request, promise);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            printAggregatedRedisResponse((RedisMessage) msg);
        }
        private  void printAggregatedRedisResponse(RedisMessage msg) {
            if (msg instanceof SimpleStringRedisMessage) {
                System.out.println(((SimpleStringRedisMessage) msg).content());
            } else if (msg instanceof ErrorRedisMessage) {
                System.out.println(((ErrorRedisMessage) msg).content());
            } else if (msg instanceof IntegerRedisMessage) {
                System.out.println(((IntegerRedisMessage) msg).value());
            } else if (msg instanceof FullBulkStringRedisMessage) {
                System.out.println(getString((FullBulkStringRedisMessage) msg));
            } else if (msg instanceof ArrayRedisMessage) {
                for (RedisMessage child : ((ArrayRedisMessage) msg).children()) {
                    printAggregatedRedisResponse(child);
                }
            } else {
                throw new CodecException("unknown message type: " + msg);
            }
        }

        private  String getString(FullBulkStringRedisMessage msg) {
            if (msg.isNull()) {
                return "(null)";
            }
            return msg.content().toString(CharsetUtil.UTF_8);
        }
    }
}











